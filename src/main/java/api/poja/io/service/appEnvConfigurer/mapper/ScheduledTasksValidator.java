package api.poja.io.service.appEnvConfigurer.mapper;

import static api.poja.io.endpoint.rest.mapper.ComputeStackResourceMapper.distinctByKey;

import api.poja.io.model.exception.BadRequestException;
import api.poja.io.model.pojaConf.conf2.PojaConf2.ScheduledTask;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasksValidator implements Consumer<List<ScheduledTask>> {
  @Override
  public void accept(List<ScheduledTask> scheduledTasks) {
    StringBuilder exceptionMessageBuilder = new StringBuilder();
    var exceptionMessages = getExceptionErrorMessages(scheduledTasks);
    if (!exceptionMessages.isEmpty()) {
      exceptionMessageBuilder.append(String.join(" ", exceptionMessages));
    }
    var exceptionMessage = exceptionMessageBuilder.toString();
    if (!exceptionMessage.isEmpty()) {
      throw new BadRequestException(exceptionMessage);
    }
  }

  public Set<String> getExceptionErrorMessages(List<ScheduledTask> scheduledTasks) {
    Set<String> messages = new HashSet<>();
    var uniqueScheduledTasks =
        scheduledTasks.stream().filter(distinctByKey(ScheduledTask::name)).toList();
    if (uniqueScheduledTasks.size() < scheduledTasks.size()) {
      messages.add("scheduled tasks contains duplicate tasks by name.");
    }
    scheduledTasks.forEach(
        task -> {
          if (task.className() == null || task.className().isEmpty()) {
            messages.add("task.name is mandatory.");
          }
          if (task.className().contains(".")) {
            messages.add("task.name must not contains \".\".");
          }
          if (task.description() == null || task.description().isEmpty()) {
            messages.add("task.description is mandatory.");
          }
          if (task.eventStackSource() == null) {
            messages.add("task.event_stack_source is mandatory.");
          }
          if (task.scheduleExpression() == null || task.scheduleExpression().isEmpty()) {
            messages.add("task.schedule_expression is mandatory.");
          }
        });
    return messages;
  }
}
