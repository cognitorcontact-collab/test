package api.poja.io.service.event;

import api.poja.io.endpoint.event.model.PaymentAttemptTriggered;
import api.poja.io.service.PaymentService;
import java.time.YearMonth;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentAttemptTriggeredService implements Consumer<PaymentAttemptTriggered> {
  private final PaymentService paymentService;

  @Override
  public void accept(PaymentAttemptTriggered paymentAttemptTriggered) {
    paymentService.initiatePaymentAttempts(YearMonth.now().minusMonths(1));
  }
}
