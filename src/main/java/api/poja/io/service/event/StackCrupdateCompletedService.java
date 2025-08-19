package api.poja.io.service.event;

import api.poja.io.aws.cloudformation.CloudformationComponent;
import api.poja.io.endpoint.event.model.StackCrupdateCompleted;
import api.poja.io.repository.model.Stack;
import api.poja.io.service.stackCrupdateCompleted.ComputeStackCrupdateCompletedService;
import api.poja.io.service.stackCrupdateCompleted.EventStackCrupdateCompletedService;
import api.poja.io.service.stackCrupdateCompleted.StorageBucketStackCrupdateCompletedService;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudformation.model.StackResource;

@Slf4j
@Service
@AllArgsConstructor
public class StackCrupdateCompletedService implements Consumer<StackCrupdateCompleted> {
  private final CloudformationComponent cloudformationComponent;
  private final ComputeStackCrupdateCompletedService computeStackCrupdateCompletedService;
  private final StorageBucketStackCrupdateCompletedService
      storageBucketStackCrupdateCompletedService;
  private final EventStackCrupdateCompletedService eventStackCrupdateCompletedService;

  @Override
  public void accept(StackCrupdateCompleted stackCrupdateCompleted) {
    switch (stackCrupdateCompleted.getCrupdatedStack().getType()) {
      case COMPUTE_PERMISSION -> {
        log.info("not handled stack. do nothing");
      }
      case EVENT -> {
        eventStackCrupdateCompletedService.accept(
            stackCrupdateCompleted, getStackResources(stackCrupdateCompleted));
      }
      case STORAGE_BUCKET -> {
        storageBucketStackCrupdateCompletedService.accept(
            stackCrupdateCompleted, getStackResources(stackCrupdateCompleted));
      }
      case COMPUTE -> {
        computeStackCrupdateCompletedService.accept(
            stackCrupdateCompleted, getStackResources(stackCrupdateCompleted));
      }
    }
  }

  private List<StackResource> getStackResources(StackCrupdateCompleted stackCrupdateCompleted) {
    Stack crupdatedStack = stackCrupdateCompleted.getCrupdatedStack();
    // check if stack still exists because we do not want to get stack by cf stack id
    // we do not want that because next operation will add resources, if stack is already deleted,
    // we do not want to operate on already delete resources
    var stack = cloudformationComponent.findStackByName(crupdatedStack.getName());
    if (stack.isEmpty()) {
      return List.of();
    }
    return cloudformationComponent.getStackResources(crupdatedStack.getName());
  }

  public static String getPhysicalResourceId(
      List<StackResource> stackResources, String targetLogicalResourceId) {
    return stackResources.stream()
        .filter(stackResource -> stackResource.logicalResourceId().equals(targetLogicalResourceId))
        .map(StackResource::physicalResourceId)
        .findFirst()
        .orElse(null);
  }
}
