package api.poja.io.service.event;

import static api.poja.io.repository.model.enums.PaymentRequestPeriod.DECEMBER;
import static api.poja.io.repository.model.enums.PaymentRequestPeriod.JANUARY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import api.poja.io.conf.MockedThirdParties;
import api.poja.io.endpoint.event.model.UserMonthlyPaymentRequestSaveRequested;
import api.poja.io.service.UserPaymentRequestService;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

class UserMonthlyPaymentRequestSaveRequestedServiceTest extends MockedThirdParties {
  @Autowired private UserMonthlyPaymentRequestSaveRequestedService subject;
  @SpyBean UserPaymentRequestService userPaymentRequestServiceSpy;

  @Test
  void accept_already_existing_ko() {
    subject.accept(
        UserMonthlyPaymentRequestSaveRequested.builder()
            .paymentRequestId("payment_request_1_id")
            .userId("joe-doe-id")
            .customerId("customer_id")
            .year(Year.of(2024))
            .period(DECEMBER)
            .build());
    verify(userPaymentRequestServiceSpy, never()).save(any());
    verify(eventProducerMock, never()).accept(anyList());
  }

  @Test
  void accept_new_ok() {
    subject.accept(
        UserMonthlyPaymentRequestSaveRequested.builder()
            .paymentRequestId("payment_request_1_id")
            .userId("joe-doe-id")
            .customerId("customer_id")
            .year(Year.of(2025))
            .period(JANUARY)
            .build());
    verify(userPaymentRequestServiceSpy, times(1)).save(any());
    verify(eventProducerMock, times(1)).accept(anyList());
  }
}
