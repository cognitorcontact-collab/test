package api.poja.io.service;

import static api.poja.io.repository.model.enums.InvoiceStatus.PAID;
import static api.poja.io.repository.model.enums.InvoiceStatus.UNKNOWN;
import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.UserSubscriptionInvoicePaymentRequested;
import api.poja.io.endpoint.rest.model.PayInvoice;
import api.poja.io.model.OfferDto;
import api.poja.io.model.exception.BadRequestException;
import api.poja.io.model.exception.NotFoundException;
import api.poja.io.repository.jpa.ApplicationRepository;
import api.poja.io.repository.jpa.UserSubscriptionRepository;
import api.poja.io.repository.model.Invoice;
import api.poja.io.repository.model.UserSubscription;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserSubscriptionService {
  private final UserSubscriptionRepository repository;
  private final OfferService offerService;
  private final ApplicationRepository applicationRepository;
  private final EventProducer<UserSubscriptionInvoicePaymentRequested> eventProducer;
  private final InvoiceService invoiceService;
  private final UserService userService;

  public List<UserSubscription> findAllToRenew(YearMonth yearMonth) {
    return repository.findAllToRenew(yearMonth.getYear() * 100L + yearMonth.getMonthValue());
  }

  public Optional<UserSubscription> findUnpaidOfYearMonth(String userId, YearMonth yearMonth) {
    return repository.findUnpaidOfYearMonth(
        userId, yearMonth.getYear() * 100L + yearMonth.getMonthValue());
  }

  public List<UserSubscription> getSubscriptionsByUserIdAndActive(String userId, Boolean active) {
    if (active == null) {
      return repository.findAllByUserIdOrderBySubscriptionBeginDatetimeDesc(userId);
    }
    if (active) {
      return repository.findAllActiveByUserId(userId, now());
    }
    return repository.findAllNotActiveByUserId(userId, now());
  }

  public Optional<UserSubscription> findActiveSubscriptionByUserId(String userId) {
    return repository.findActiveByUserId(userId, now());
  }

  public UserSubscription getByUserIdAndId(String userId, String id) {
    var sub =
        repository
            .findByUserIdAndId(userId, id)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Subscription with id="
                            + id
                            + " and userId="
                            + userId
                            + " was not found."));
    Invoice invoice = sub.getInvoice();
    if (!invoice.hasReachedFinalStatus()) {
      return sub.toBuilder().invoice(invoiceService.refreshInvoice(invoice)).build();
    }
    return sub;
  }

  public UserSubscription subscribe(
      String userId, String offerId, Instant subscriptionBegin, boolean isManualSubscription) {
    if (repository.existsActiveOrUndergoingPaymentByUserId(userId, subscriptionBegin)) {
      throw new BadRequestException("User.Id=" + userId + " is already subscribed to an offer.");
    }
    OfferDto offerDto = offerService.getById(offerId);
    if (!offerDto.canBeSubscribed()) {
      throw new BadRequestException(
          "Offer.Id=" + offerId + " cannot be subscribed anymore, limit was reached.");
    }
    Invoice invoice =
        invoiceService.save(
            Invoice.builder()
                .userId(userId)
                .amountInUsd(offerDto.priceInUsd())
                .status(UNKNOWN)
                .build());
    var endOfMonth =
        YearMonth.from(subscriptionBegin.atZone(UTC))
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(UTC)
            .toInstant();
    var saved =
        repository.save(
            UserSubscription.builder()
                .userId(userId)
                .offer(offerDto.getOffer())
                .subscriptionBeginDatetime(subscriptionBegin)
                .subscriptionEndDatetime(endOfMonth)
                .invoice(invoice)
                .willRenew(true)
                .build());
    userService.updateUserLatestSubscriptionId(userId, saved.getId());

    eventProducer.accept(
        List.of(
            new UserSubscriptionInvoicePaymentRequested(
                saved.getId(),
                saved.getInvoice().getId(),
                saved.getUserId(),
                subscriptionBegin,
                isManualSubscription)));
    return saved;
  }

  @Transactional
  public UserSubscription unsubscribe(String userId, String subscriptionId) {
    var sub = getByUserIdAndId(userId, subscriptionId);
    checkUnsubscription(sub);
    Invoice invoice = sub.getInvoice();
    if (!PAID.equals(invoice.getStatus())) {
      invoiceService.voidInvoice(invoice);
    }
    repository.updateSubscriptionEndFields(userId, subscriptionId, now());
    userService.updateUserLatestSubscriptionId(userId, null);
    return getByUserIdAndId(userId, subscriptionId);
  }

  private void checkUnsubscription(UserSubscription subscription) {
    Instant now = now();
    if (subscription.getSubscriptionEndDatetime() != null
        && now.isAfter(subscription.getSubscriptionBeginDatetime())
        && now.isAfter(subscription.getSubscriptionEndDatetime())) {
      throw new BadRequestException(
          "subscription.id="
              + subscription.getId()
              + " for offer.id="
              + subscription.getOffer().getId()
              + " already ended");
    }
    SubscriptionUsage basicOfferSubscriptionUsage = getBasicOfferSubscriptionUsage(subscription);
    if (!basicOfferSubscriptionUsage.compliesToOffer()) {
      throw new BadRequestException(
          "need to delete "
              + (basicOfferSubscriptionUsage.offer().maxApps()
                  - basicOfferSubscriptionUsage.currentNbOfApps())
              + " apps to unsubscribe.");
    }
  }

  public SubscriptionUsage getBasicOfferSubscriptionUsage(UserSubscription userSubscription) {
    String userId = userSubscription.getUserId();
    var offer = offerService.getBasicOfferForUser(userId);
    var currentApps = applicationRepository.countAllFromOrgsOwnedByUserByCriteria(userId, false);
    return new SubscriptionUsage(offer, userSubscription, currentApps);
  }

  // TODO: could be async
  public UserSubscription paySubscription(
      String userId, String subscriptionId, PayInvoice payInvoice) {
    var sub = getByUserIdAndId(userId, subscriptionId);
    Invoice invoice = sub.getInvoice();
    if (!invoice.canBePaid()) {
      throw new BadRequestException(
          "payment with status=" + invoice.getStatus() + " cannot be paid.");
    }
    var updatedInvoice = invoiceService.payInvoice(userId, payInvoice);
    return sub.toBuilder().invoice(updatedInvoice).build();
  }

  @Transactional
  public void updateWillRenew(String subscriptionId, boolean willRenew) {
    repository.updateWillRenew(subscriptionId, willRenew);
  }

  public record SubscriptionUsage(
      OfferDto offer, UserSubscription userSubscription, long currentNbOfApps) {
    public boolean compliesToOffer() {
      return currentNbOfApps <= offer.maxApps();
    }
  }
}
