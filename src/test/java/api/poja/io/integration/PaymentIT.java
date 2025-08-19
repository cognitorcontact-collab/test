package api.poja.io.integration;

import static api.poja.io.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.poja.io.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.poja.io.integration.conf.utils.TestMocks.paymentMethod;
import static api.poja.io.integration.conf.utils.TestUtils.setUpGithub;
import static api.poja.io.integration.conf.utils.TestUtils.setUpStripe;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.poja.io.conf.MockedThirdParties;
import api.poja.io.endpoint.rest.api.PaymentApi;
import api.poja.io.endpoint.rest.client.ApiClient;
import api.poja.io.endpoint.rest.client.ApiException;
import api.poja.io.endpoint.rest.model.PaymentMethod;
import api.poja.io.integration.conf.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
public class PaymentIT extends MockedThirdParties {
  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() {
    setUpGithub(githubComponentMock);
    setUpStripe(stripeServiceMock);
  }

  @Test
  void get_payment_methods_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    PaymentApi api = new PaymentApi(joeDoeClient);

    var paymentMethodsResponse = api.getPaymentMethods(JOE_DOE_ID);
    PaymentMethod paymentMethod = requireNonNull(paymentMethodsResponse.getData()).getFirst();
    com.stripe.model.PaymentMethod.Card card = paymentMethod().getCard();

    assertEquals(paymentMethod().getId(), paymentMethod.getId());
    assertEquals(card.getBrand(), paymentMethod.getBrand());
    assertEquals(card.getLast4(), paymentMethod.getLast4());
    assertEquals(paymentMethod().getType(), paymentMethod.getType());
  }
}
