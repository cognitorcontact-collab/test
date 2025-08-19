package api.poja.io.service.pricing.calculator.impl;

import static api.poja.io.service.pricing.PricingMethod.TEN_MICRO;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component("tenMicroPricingCalculator")
class TenMicroPricingCalculator extends AbstractPricingCalculator {
  public static final BigDecimal TEN_MICRO_USD = new BigDecimal("0.00001");

  protected TenMicroPricingCalculator() {
    super(TEN_MICRO);
  }

  @Override
  public BigDecimal computePrice(BigDecimal totalMemoryDurationMinutes) {
    return TEN_MICRO_USD.multiply(totalMemoryDurationMinutes);
  }
}
