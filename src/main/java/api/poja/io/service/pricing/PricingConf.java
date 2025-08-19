package api.poja.io.service.pricing;

import static api.poja.io.model.Money.Currency.DOLLAR;

import api.poja.io.model.Money;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record PricingConf(@Value("${pricing.free.tier}") BigDecimal freeTier) {
  public Money freeTierAsMoney() {
    return new Money(freeTier, DOLLAR);
  }
}
