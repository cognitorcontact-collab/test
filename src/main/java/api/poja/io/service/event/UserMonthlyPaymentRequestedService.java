package api.poja.io.service.event;

import static api.poja.io.model.Money.Currency.CENTS;
import static api.poja.io.model.Money.Currency.DOLLAR;
import static api.poja.io.model.Money.ZERO;
import static java.lang.Math.min;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;

import api.poja.io.endpoint.event.model.UserMonthlyPaymentRequested;
import api.poja.io.model.Money;
import api.poja.io.model.exception.ApiException;
import api.poja.io.repository.model.UserBillingDiscount;
import api.poja.io.repository.model.UserPaymentRequest;
import api.poja.io.repository.model.enums.InvoiceStatus;
import api.poja.io.repository.model.enums.PaymentRequestPeriod;
import api.poja.io.service.UserBillingDiscountService;
import api.poja.io.service.UserPaymentRequestService;
import api.poja.io.service.pricing.PricingConf;
import api.poja.io.service.stripe.StripeService;
import com.stripe.model.Invoice;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserMonthlyPaymentRequestedService implements Consumer<UserMonthlyPaymentRequested> {
  private final PricingConf pricingConf;
  private final StripeService stripeService;
  private final UserPaymentRequestService userPaymentRequestService;
  private final UserBillingDiscountService userBillingDiscountService;

  @Override
  public void accept(UserMonthlyPaymentRequested userMonthlyPaymentRequested) {
    var invoice = stripeService.createInvoice(userMonthlyPaymentRequested.getCustomerId());
    var invoiceId = invoice.getId();
    userMonthlyPaymentRequested.getItemsToPay().stream()
        .filter(e -> !ZERO.equals(e.amount()))
        .forEach(e -> stripeCreateInvoiceItem(invoiceId, e));
    Money computedDueAmount = userMonthlyPaymentRequested.computeDueAmount();
    long monthlyDiscount =
        applyMonthlyDiscount(
            invoice.getId(), userMonthlyPaymentRequested.getCustomerId(), computedDueAmount);
    long extraDiscount =
        applyExtraDiscounts(
            userMonthlyPaymentRequested.getUserId(),
            invoiceId,
            userMonthlyPaymentRequested.getCustomerId(),
            new Money(computedDueAmount.amount().add(BigDecimal.valueOf(monthlyDiscount)), CENTS),
            userMonthlyPaymentRequested.getYearMonth());
    long totalDiscount = monthlyDiscount + extraDiscount;
    var finalizedInvoice = stripeService.finalizeInvoice(invoiceId);
    Long dueAmount = finalizedInvoice.getAmountDue();
    if (dueAmount != null && dueAmount == 0) {
      updateInvoicePaymentDetails(userMonthlyPaymentRequested, finalizedInvoice, totalDiscount);
      return;
    }
    try {
      var paidInvoice = stripeService.payInvoice(invoiceId);
      updateInvoicePaymentDetails(userMonthlyPaymentRequested, paidInvoice, totalDiscount);
    } catch (ApiException stripeException) {
      var paymentFailedInvoice = stripeService.retrieveInvoice(invoiceId);
      updateInvoicePaymentDetails(userMonthlyPaymentRequested, paymentFailedInvoice, totalDiscount);
    }
  }

  private void updateInvoicePaymentDetails(
      UserMonthlyPaymentRequested userMonthlyPaymentRequested,
      Invoice invoice,
      long appliedDiscount) {
    UserPaymentRequest paymentRequest =
        userPaymentRequestService.getById(userMonthlyPaymentRequested.getPaymentRequestId());
    InvoiceStatus invoicePaymentStatus = stripeService.getPaymentStatus(invoice);
    userPaymentRequestService.save(
        paymentRequest.toBuilder()
            .discountAmount(appliedDiscount)
            .invoiceId(invoice.getId())
            .invoiceUrl(invoice.getInvoicePdf())
            .invoiceStatus(invoicePaymentStatus)
            .build());
  }

  private void stripeCreateInvoiceItem(
      String invoiceId, UserMonthlyPaymentRequested.ItemToPay domainItemToPay) {
    stripeService.createInvoiceItem(
        invoiceId,
        domainItemToPay.customerId(),
        domainItemToPay.amount(),
        domainItemToPay.description());
  }

  private long stripeFreeTierAmount() {
    return pricingConf.freeTierAsMoney().convertCurrency(CENTS).amount().longValue();
  }

  private long applyMonthlyDiscount(String invoiceId, String customerId, Money dueAmount) {
    long discountAmount =
        -min(
            Math.abs(stripeFreeTierAmount()),
            dueAmount.convertCurrency(CENTS).amount().longValue());
    stripeCreateInvoiceItem(
        invoiceId,
        new UserMonthlyPaymentRequested.ItemToPay(
            "monthly free plan",
            customerId,
            "Poja monthly discount (maxed at 2$ per month)",
            new Money(BigDecimal.valueOf(discountAmount), CENTS)));
    return discountAmount;
  }

  private long applyExtraDiscounts(
      String userId, String invoiceId, String customerId, Money dueAmount, YearMonth yearMonth) {

    int year = yearMonth.getYear();
    var period = PaymentRequestPeriod.valueOf(yearMonth.getMonth().name());

    List<UserBillingDiscount> discounts =
        userBillingDiscountService.findAllByUserIdAndYearAndMonth(userId, year, period);

    if (discounts.isEmpty()) {
      return 0;
    }

    var totalDiscountAmount =
        new Money(
            discounts.stream()
                .map(UserBillingDiscount::getAmountInUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .max(BigDecimal.ZERO),
            DOLLAR);

    if (totalDiscountAmount.amount().compareTo(BigDecimal.ZERO) == 0) {
      return 0;
    }

    long discountToApply =
        -min(
            Math.abs(totalDiscountAmount.convertCurrency(CENTS).amount().longValue()),
            dueAmount.convertCurrency(CENTS).amount().longValue());

    var description =
        String.format(
            "Extra discount%s (%s)",
            discounts.size() > 1 ? "s" : "",
            discounts.stream().map(UserBillingDiscount::getDescription).collect(joining(", ")));

    stripeCreateInvoiceItem(
        invoiceId,
        new UserMonthlyPaymentRequested.ItemToPay(
            randomUUID().toString(),
            customerId,
            description,
            new Money(BigDecimal.valueOf(discountToApply), CENTS)));

    return discountToApply;
  }
}
