package api.poja.io.service;

import static api.poja.io.repository.model.enums.InvoiceStatus.PAID;
import static java.lang.Boolean.TRUE;
import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.PojaEvent;
import api.poja.io.endpoint.event.model.RefreshUserStatusRequested;
import api.poja.io.endpoint.event.model.UserMonthlyPaymentRequestSaveRequested;
import api.poja.io.endpoint.rest.model.PaymentCustomer;
import api.poja.io.endpoint.rest.model.PaymentMethodsAction;
import api.poja.io.model.User;
import api.poja.io.repository.model.PaymentRequest;
import api.poja.io.repository.model.UserPaymentRequest;
import api.poja.io.repository.model.enums.InvoiceStatus;
import api.poja.io.repository.model.enums.PaymentRequestPeriod;
import api.poja.io.service.stripe.StripeService;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentMethod;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {
  private final EventProducer<PojaEvent> eventProducer;
  private final StripeService stripeService;
  private final UserService userService;
  private final PaymentRequestService paymentRequestService;
  private final UserPaymentRequestService userPaymentRequestService;

  public PaymentMethod managePaymentMethod(String userId, PaymentMethodsAction pmAction) {
    User user = userService.getUserById(userId);
    String stripeId = user.getStripeId();
    String pmId = pmAction.getPaymentMethodId();
    PaymentMethodsAction.ActionEnum action = pmAction.getAction();
    var paymentMethod = getPaymentMethod(pmAction, action, stripeId, pmId);
    eventProducer.accept(
        List.of(
            RefreshUserStatusRequested.builder()
                .pricingCalculationRequestStartTime(LocalDate.now().atStartOfDay(UTC).toInstant())
                .pricingCalculationRequestEndTime(now())
                .userId(userId)
                .build()));
    return paymentMethod;
  }

  private PaymentMethod getPaymentMethod(
      PaymentMethodsAction pmAction,
      PaymentMethodsAction.ActionEnum action,
      String stripeId,
      String pmId) {
    return switch (action) {
      case ATTACH:
        PaymentMethod paymentMethod = stripeService.attachPaymentMethod(stripeId, pmId);
        if (TRUE.equals(pmAction.getSetDefault())) {
          stripeService.setDefaultPaymentMethod(stripeId, pmId);
        }
        yield paymentMethod;
      case DETACH:
        yield stripeService.detachPaymentMethod(pmId);
    };
  }

  public List<PaymentMethod> getPaymentMethods(String userId) {
    User user = userService.getUserById(userId);
    return stripeService.getPaymentMethods(user.getStripeId());
  }

  public Customer getCustomer(String userId) {
    User user = userService.getUserById(userId);
    return stripeService.retrieveCustomer(user.getStripeId());
  }

  public Customer updateCustomer(PaymentCustomer customer) {
    return stripeService.updateCustomer(
        customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone());
  }

  public void initiatePaymentAttempts(YearMonth lastYearMonth) {
    Year lastYearMonthYear = Year.of(lastYearMonth.getYear());
    var period = PaymentRequestPeriod.valueOf(lastYearMonth.getMonth().name());
    if (paymentRequestService.existsByYearAndPeriod(lastYearMonthYear, period)) {
      log.error(
          "payment request with year = {} and period = {} already exists.",
          lastYearMonthYear,
          period);
      return;
    }
    var savedPaymentRequest =
        paymentRequestService.save(
            PaymentRequest.builder()
                .year(Year.now().getValue())
                .requestInstant(now())
                .period(period)
                .build());
    List<User> users = userService.findAllToBill(lastYearMonth);
    var events =
        users.stream()
            .map(
                user ->
                    toUserMonthlyPaymentRequestSaveRequested(
                        user, savedPaymentRequest.getId(), period, lastYearMonthYear))
            .toList();
    eventProducer.accept(events);
  }

  private PojaEvent toUserMonthlyPaymentRequestSaveRequested(
      User user, String paymentRequestId, PaymentRequestPeriod period, Year year) {
    return UserMonthlyPaymentRequestSaveRequested.builder()
        .paymentRequestId(paymentRequestId)
        .userId(user.getId())
        .customerId(user.getStripeId())
        .period(period)
        .year(year)
        .build();
  }

  public UserPaymentRequest payInvoiceManually(
      String userId, String paymentId, String invoiceId, String paymentMethodId) {
    Invoice invoice = stripeService.payInvoice(invoiceId, paymentMethodId);
    InvoiceStatus paymentStatus = stripeService.getPaymentStatus(invoice);
    var payment = userPaymentRequestService.getById(paymentId);
    payment.setInvoiceStatus(paymentStatus);
    var savedPayment = userPaymentRequestService.save(payment);
    if (PAID.equals(paymentStatus))
      eventProducer.accept(
          List.of(
              RefreshUserStatusRequested.builder()
                  .pricingCalculationRequestEndTime(now())
                  .pricingCalculationRequestStartTime(LocalDate.now().atStartOfDay(UTC).toInstant())
                  .userId(userId)
                  .build()));
    return savedPayment;
  }
}
