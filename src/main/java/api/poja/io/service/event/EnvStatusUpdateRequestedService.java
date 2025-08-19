package api.poja.io.service.event;

import static api.poja.io.endpoint.rest.model.Environment.StatusEnum.ACTIVE;
import static api.poja.io.endpoint.rest.model.Environment.StatusEnum.SUSPENDED;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.EnvStatusUpdateRequested;
import api.poja.io.endpoint.event.model.LambdaFunctionStatusUpdateRequested;
import api.poja.io.repository.model.ComputeStackResource;
import api.poja.io.repository.model.Environment;
import api.poja.io.service.ComputeStackResourceService;
import api.poja.io.service.EnvironmentService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnvStatusUpdateRequestedService implements Consumer<EnvStatusUpdateRequested> {
  private final EnvironmentService environmentService;
  private final EventProducer<LambdaFunctionStatusUpdateRequested> eventProducer;
  private final ComputeStackResourceService computeStackResourceService;

  @Override
  public void accept(EnvStatusUpdateRequested envStatusUpdateRequested) {
    Environment environment = environmentService.getById(envStatusUpdateRequested.getEnvId());
    switch (envStatusUpdateRequested.getStatus()) {
      case SUSPEND -> {
        if (SUSPENDED.equals(environment.getStatus())) {
          return;
        }
        environmentService.updateEnvStatus(envStatusUpdateRequested.getEnvId(), SUSPENDED);
        var optionalLatestComputeResource =
            computeStackResourceService.findLatestByEnvironmentId(
                envStatusUpdateRequested.getEnvId());
        if (optionalLatestComputeResource.isPresent()) {
          List<String> functionNames = getFunctionNames(optionalLatestComputeResource.get());
          eventProducer.accept(
              functionNames.stream()
                  .map(
                      fn ->
                          toLambdaStatusUpdateEvent(
                              fn, LambdaFunctionStatusUpdateRequested.StatusAlteration.SUSPEND))
                  .toList());
        }
      }
      case ACTIVATE -> {
        if (ACTIVE.equals(environment.getStatus())) {
          return;
        }
        environmentService.updateEnvStatus(envStatusUpdateRequested.getEnvId(), ACTIVE);
        var optionalLatestComputeResource =
            computeStackResourceService.findLatestByEnvironmentId(
                envStatusUpdateRequested.getEnvId());
        if (optionalLatestComputeResource.isPresent()) {
          List<String> functionNames = getFunctionNames(optionalLatestComputeResource.get());
          eventProducer.accept(
              functionNames.stream()
                  .map(
                      fn ->
                          toLambdaStatusUpdateEvent(
                              fn, LambdaFunctionStatusUpdateRequested.StatusAlteration.ACTIVATE))
                  .toList());
        }
      }
    }
  }

  private static List<String> getFunctionNames(ComputeStackResource computeStackResource) {
    List<String> functionNames = new ArrayList<>();
    if (computeStackResource.getWorker1FunctionName() != null) {
      functionNames.add(computeStackResource.getWorker1FunctionName());
    }
    if (computeStackResource.getWorker2FunctionName() != null) {
      functionNames.add(computeStackResource.getWorker2FunctionName());
    }
    if (computeStackResource.getFrontalFunctionName() != null) {
      functionNames.add(computeStackResource.getFrontalFunctionName());
    }
    return functionNames;
  }

  private static LambdaFunctionStatusUpdateRequested toLambdaStatusUpdateEvent(
      String functionName, LambdaFunctionStatusUpdateRequested.StatusAlteration statusAlteration) {
    return LambdaFunctionStatusUpdateRequested.builder()
        .functionName(functionName)
        .status(statusAlteration)
        .build();
  }
}
