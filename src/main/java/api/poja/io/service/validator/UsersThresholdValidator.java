package api.poja.io.service.validator;

import api.poja.io.model.exception.ServiceUnavailableException;
import api.poja.io.repository.UserRepository;
import java.util.function.LongConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UsersThresholdValidator implements LongConsumer {

  private final long usersThreshold;
  private final UserRepository userRepository;

  public UsersThresholdValidator(
      @Value("${max.users}") long maxUsersNb, UserRepository userRepository) {
    this.usersThreshold = maxUsersNb;
    this.userRepository = userRepository;
  }

  @Override
  public void accept(long usersToCreate) {
    checkThreshold(usersToCreate);
  }

  private void checkThreshold(long usersToCreate) {
    long usersCount = userRepository.countAll();
    if (usersCount > usersThreshold || usersCount + usersToCreate > usersThreshold) {
      throw new ServiceUnavailableException(
          "Cannot add "
              + usersToCreate
              + " users: this would exceed the maximum limit of "
              + usersThreshold
              + " users (current users: "
              + usersCount
              + ").");
    }
  }
}
