package api.poja.io.endpoint.rest.mapper;

import static java.lang.Boolean.TRUE;

import api.poja.io.endpoint.rest.model.FunctionMonitoringResource;
import api.poja.io.endpoint.rest.model.GroupedMonitoringResources;
import api.poja.io.repository.model.ComputeStackResource;
import api.poja.io.service.ComputeStackResourceService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ComputeStackResourceMapper {
  private final ComputeStackResourceService computeStackResourceService;

  public GroupedMonitoringResources toRest(List<ComputeStackResource> domain) {
    List<FunctionMonitoringResource> frontalFunctionLogs = new ArrayList<>();
    List<FunctionMonitoringResource> workerFunction1Logs = new ArrayList<>();
    List<FunctionMonitoringResource> workerFunction2Logs = new ArrayList<>();
    for (ComputeStackResource stackResource : domain) {
      if (stackResource.getFrontalFunctionName() != null) {
        frontalFunctionLogs.add(frontalFunctionFrom(stackResource));
      }
      if (stackResource.getWorker1FunctionName() != null) {
        workerFunction1Logs.add(workerFunction1From(stackResource));
      }
      if (stackResource.getWorker2FunctionName() != null) {
        workerFunction2Logs.add(workerFunction2From(stackResource));
      }
    }
    return new GroupedMonitoringResources()
        .frontalFunctionMonitoringResources(distinctList(frontalFunctionLogs))
        .workerFunction1MonitoringResources(distinctList(workerFunction1Logs))
        .workerFunction2MonitoringResources(distinctList(workerFunction2Logs));
  }

  private List<FunctionMonitoringResource> distinctList(
      List<FunctionMonitoringResource> resources) {
    return resources.stream().filter((distinctByKey(FunctionMonitoringResource::getName))).toList();
  }

  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), TRUE) == null;
  }

  private FunctionMonitoringResource frontalFunctionFrom(ComputeStackResource stackResource) {
    String functionName = stackResource.getFrontalFunctionName();
    return new FunctionMonitoringResource()
        .monitoringUri(computeStackResourceService.getFunctionDashboardUrl(functionName))
        .name(functionName)
        .creationDatetime(stackResource.getCreationDatetime());
  }

  private FunctionMonitoringResource workerFunction1From(ComputeStackResource stackResource) {
    String functionName = stackResource.getWorker1FunctionName();
    return new FunctionMonitoringResource()
        .monitoringUri(computeStackResourceService.getFunctionDashboardUrl(functionName))
        .name(functionName)
        .creationDatetime(stackResource.getCreationDatetime());
  }

  private FunctionMonitoringResource workerFunction2From(ComputeStackResource stackResource) {
    String functionName = stackResource.getWorker2FunctionName();
    return new FunctionMonitoringResource()
        .monitoringUri(computeStackResourceService.getFunctionDashboardUrl(functionName))
        .name(functionName)
        .creationDatetime(stackResource.getCreationDatetime());
  }
}
