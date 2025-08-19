package api.poja.io.endpoint.event.model;

import api.poja.io.model.PojaVersion;
import java.time.Duration;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public final class PojaConfUploaded extends PojaEvent {
  /**
   * constructor of PojaConfUploaded
   *
   * @param pojaVersion: cli_version
   * @param environmentId: environment configured with the conf
   * @param orgId: poja conf owner userid
   * @param filename: refers to the s3 key without the prefixes (orgId, environmentId, ...)
   * @param appId: configuredAppId
   */
  public PojaConfUploaded(
      PojaVersion pojaVersion,
      String environmentId,
      String orgId,
      String filename,
      String appId,
      String appEnvDeplId,
      String envDeplConfId) {
    this.pojaVersion = pojaVersion;
    this.environmentId = environmentId;
    this.orgId = orgId;
    this.filename = filename;
    this.appId = appId;
    this.appEnvDeplId = appEnvDeplId;
    this.envDeplConfId = envDeplConfId;
  }

  private final PojaVersion pojaVersion;
  private final String environmentId;
  private final String orgId;
  private final String filename;
  private final String appId;
  private final String appEnvDeplId;
  private final String envDeplConfId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(40);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
