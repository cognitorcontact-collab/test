package api.poja.io.service.workflows;

import static api.poja.io.endpoint.rest.model.ExecutionType.ASYNCHRONOUS;

import api.poja.io.endpoint.rest.model.DeploymentStateEnum;
import api.poja.io.model.exception.NotFoundException;
import api.poja.io.repository.jpa.DeploymentStateRepository;
import api.poja.io.repository.model.DeploymentState;
import api.poja.io.repository.model.workflows.IllegalStateTransitionException;
import api.poja.io.service.AppEnvironmentDeploymentService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DeploymentStateService {
  private final DeploymentStateRepository repository;
  private final AppEnvironmentDeploymentService appEnvironmentDeploymentService;

  public List<DeploymentState> getSortedDeploymentStatesByDeploymentId(
      String orgId, String appId, String deploymentId) {
    Sort sortByTimestampAsc = Sort.by("timestamp").ascending();
    return repository.findAllByAppEnvDeploymentId(deploymentId, sortByTimestampAsc);
  }

  public Optional<DeploymentState> getOptionalLatestDeploymentStateByDeploymentId(
      String appEnvDeploymentId) {
    return repository.findTopByAppEnvDeploymentIdOrderByTimestampDesc(appEnvDeploymentId);
  }

  public DeploymentState getLatestDeploymentStateByDeploymentId(String appEnvDeploymentId) {
    return getOptionalLatestDeploymentStateByDeploymentId(appEnvDeploymentId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "deployment state not found for appEnvDeploymentId: " + appEnvDeploymentId));
  }

  public void save(String appId, String appEnvDeploymentId, DeploymentStateEnum status) {
    var appEnvDepl = appEnvironmentDeploymentService.getByAppIdAndId(appId, appEnvDeploymentId);
    DeploymentState toBeAdded =
        DeploymentState.builder()
            .timestamp(Instant.now())
            .appEnvDeploymentId(appEnvDeploymentId)
            .progressionStatus(status)
            .executionType(ASYNCHRONOUS)
            .build();
    try {
      appEnvDepl.addState(toBeAdded);
    } catch (IllegalStateTransitionException e) {
      throw new RuntimeException(e);
    }
    Optional<DeploymentState> latestState = appEnvDepl.getLatestState();
    if (latestState.isPresent()) {
      repository.save(latestState.get());
    } else {
      log.error(
          "An error occurred when saving deployment state: latestState is empty for {}",
          appEnvDeploymentId);
    }
  }
}
