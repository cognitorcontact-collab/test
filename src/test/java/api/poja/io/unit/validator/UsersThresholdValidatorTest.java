package api.poja.io.unit.validator;

import static api.poja.io.integration.conf.utils.TestUtils.assertThrowsDomainServiceUnavailableException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import api.poja.io.repository.UserRepository;
import api.poja.io.service.validator.UsersThresholdValidator;
import org.junit.jupiter.api.Test;

public class UsersThresholdValidatorTest {
  private static final long USERS_THRESHOLD = 2L;
  private static final long CURRENT_USERS_NB = 1L;

  private UsersThresholdValidator thresholdValidator() {
    var userRepository = mock(UserRepository.class);
    when(userRepository.countAll()).thenReturn(CURRENT_USERS_NB);
    return new UsersThresholdValidator(USERS_THRESHOLD, userRepository);
  }

  @Test
  void validate_threshold_ok() {
    long usersToCreate = 1L;

    assertDoesNotThrow(() -> thresholdValidator().accept(usersToCreate));
  }

  @Test
  void validate_threshold_ko() {
    long usersToCreate = 5L;

    assertThrowsDomainServiceUnavailableException(
        "Cannot add "
            + usersToCreate
            + " users: this would exceed the maximum limit of "
            + USERS_THRESHOLD
            + " users (current users: "
            + CURRENT_USERS_NB
            + ").",
        () -> thresholdValidator().accept(usersToCreate));
  }
}
