package api.poja.io.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.YearMonth;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Data
/*
 event to renew all subscriptions of given yearMonth for nextMonth
*/
public class UsersSubscriptionRenewalRequested extends PojaEvent {
  @JsonProperty("year_month")
  YearMonth yearMonth;

  public UsersSubscriptionRenewalRequested() {
    this.yearMonth = YearMonth.now();
  }

  @JsonCreator
  public UsersSubscriptionRenewalRequested(YearMonth yearMonth) {
    this.yearMonth = yearMonth == null ? YearMonth.now() : yearMonth;
  }

  @JsonIgnore
  public YearMonth getCurrentMonth() {
    return yearMonth;
  }

  @JsonIgnore
  public YearMonth getPreviousMonth() {
    return yearMonth.minusMonths(1);
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(30);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
