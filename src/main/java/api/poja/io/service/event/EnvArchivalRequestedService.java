package api.poja.io.service.event;

import static api.poja.io.endpoint.rest.model.StackType.COMPUTE;
import static java.util.stream.Collectors.groupingBy;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.EnvArchivalRequested;
import api.poja.io.endpoint.event.model.PojaEvent;
import api.poja.io.endpoint.event.model.StackCloudPermissionRemovalRequested;
import api.poja.io.endpoint.event.model.StackDeletionRequested;
import api.poja.io.endpoint.rest.model.StackType;
import api.poja.io.repository.model.Stack;
import api.poja.io.service.ApplicationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnvArchivalRequestedService implements Consumer<EnvArchivalRequested> {
  private EventProducer<PojaEvent> eventProducer;
  @Deprecated private final ApplicationService applicationService;

  @Override
  public void accept(EnvArchivalRequested envArchivalRequested) {
    String orgId = applicationService.getById(envArchivalRequested.getAppId()).getOrgId();
    List<Stack> stacks = envArchivalRequested.getStacks();
    List<PojaEvent> events = getEvents(orgId, stacks);
    eventProducer.accept(events);
  }

  private static List<PojaEvent> getEvents(String orgId, List<Stack> stacks) {
    var res = new ArrayList<>(getStackDeletionEvents(stacks));
    Map<StackType, List<Stack>> stackMap = stacks.stream().collect(groupingBy(Stack::getType));
    var computeStacks = stackMap.getOrDefault(COMPUTE, List.of());
    if (computeStacks.isEmpty()) {
      return res;
    }
    res.add(
        StackCloudPermissionRemovalRequested.builder()
            .orgId(orgId)
            .computeStacks(computeStacks)
            .build());
    return res;
  }

  private static List<PojaEvent> getStackDeletionEvents(List<Stack> stacks) {
    return stacks.stream().map(EnvArchivalRequestedService::toStackDeletionRequestedEvent).toList();
  }

  private static PojaEvent toStackDeletionRequestedEvent(Stack stack) {
    return new StackDeletionRequested(stack);
  }
}
