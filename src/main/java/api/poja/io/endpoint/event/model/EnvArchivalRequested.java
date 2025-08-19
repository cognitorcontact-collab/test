package api.poja.io.endpoint.event.model;

import api.poja.io.repository.model.Stack;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
public class EnvArchivalRequested extends PojaEvent {
  private String appId;
  private List<Stack> stacks;
  private String envId;
  private Instant requestedAt;
  private boolean deleteCloudPermissions;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(10);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
