package api.poja.io.service.pricing.calculator.impl;

import static api.poja.io.service.pricing.PricingMethod.TWENTY_MICRO;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component("twentyMicroPricingCalculator")
class TwentyMicroPricingCalculator extends AbstractPricingCalculator {
  public static final BigDecimal TWENTY_MICRO_USD = new BigDecimal("0.00002");

  protected TwentyMicroPricingCalculator() {
    super(TWENTY_MICRO);
  }

  @Override
  public BigDecimal computePrice(BigDecimal totalMemoryDurationMinutes) {
    return TWENTY_MICRO_USD.multiply(totalMemoryDurationMinutes);
  }
}
