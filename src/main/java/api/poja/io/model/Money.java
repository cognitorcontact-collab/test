package api.poja.io.model;

import static api.poja.io.model.Money.Currency.MICRO_DOLLAR;
import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_EVEN;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;

public record Money(
    @JsonProperty("amount") BigDecimal amount, @JsonProperty("currenty") Currency currency)
    implements Comparable<Money> {
  public static final Money ZERO = new Money(BigDecimal.ZERO, MICRO_DOLLAR);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Money other = (Money) o;

    // Convert both to microdollars for a fair comparison
    BigDecimal thisMicro = this.currency.toMicroDollars(this.amount);
    BigDecimal otherMicro = other.currency.toMicroDollars(other.amount);

    return thisMicro.compareTo(otherMicro) == 0;
  }

  @Override
  public int hashCode() {
    // Normalize to microdollars for consistent hash code
    BigDecimal microValue = currency.toMicroDollars(amount);
    return 31 * microValue.hashCode();
  }

  @Override
  public int compareTo(Money o) {
    BigDecimal thisMicro = this.currency.toMicroDollars(this.amount);
    BigDecimal otherMicro = o.currency.toMicroDollars(o.amount);
    return thisMicro.compareTo(otherMicro);
  }

  @Getter
  public enum Currency {
    DOLLAR(BigDecimal.valueOf(1_000_000)),
    CENTS(BigDecimal.valueOf(10_000)),
    MICRO_DOLLAR(ONE);

    private final BigDecimal valueInMicroDollar;

    Currency(BigDecimal valueInMicroDollar) {
      this.valueInMicroDollar = valueInMicroDollar;
    }

    public BigDecimal toMicroDollars(BigDecimal amount) {
      return amount.multiply(valueInMicroDollar);
    }

    public BigDecimal fromMicroDollars(BigDecimal microAmount) {
      return microAmount.divide(valueInMicroDollar, HALF_EVEN);
    }
  }

  public Money convertCurrency(Currency destinationCurrency) {
    if (destinationCurrency.equals(currency)) {
      return this;
    }
    BigDecimal amountInMicroDollars = currency.toMicroDollars(amount);
    BigDecimal convertedAmount = destinationCurrency.fromMicroDollars(amountInMicroDollars);
    return new Money(convertedAmount, destinationCurrency);
  }

  public Money add(Money money) {
    return new Money(amount.add(money.convertCurrency(currency).amount), currency);
  }
}
