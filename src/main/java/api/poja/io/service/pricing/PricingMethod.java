package api.poja.io.service.pricing;

import lombok.Getter;

@Getter
public enum PricingMethod {
  TEN_MICRO("10µ"),
  TWENTY_MICRO("20µ");
  private final String name;

  PricingMethod(String name) {
    this.name = name;
  }
}
