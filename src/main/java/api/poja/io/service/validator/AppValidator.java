package api.poja.io.service.validator;

import api.poja.io.model.exception.BadRequestException;
import api.poja.io.model.exception.NotImplementedException;
import api.poja.io.repository.jpa.ApplicationRepository;
import api.poja.io.repository.model.Application;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AppValidator implements Consumer<Application> {
  public static final int DOMAIN_APP_NAME_MAX_LENGTH = 11;
  private final ApplicationRepository repository;

  @Override
  public void accept(Application app) {
    String appName = app.getName();
    String id = app.getId();
    boolean existsById = repository.existsById(id);
    if (!existsById
        && repository.existsByNameAndUserIdAndArchived(appName, app.getUserId(), false)) {
      throw new BadRequestException("Application with name=" + appName + " already exists");
    }
    if (app.getGithubRepositoryId() != null) {
      boolean existsByRepoId = repository.existsByGithubRepositoryId(app.getGithubRepositoryId());
      if (!existsById && existsByRepoId) {
        throw new NotImplementedException(
            "Multiple import on single repository has not been implemented yet. Github Repository"
                + " named repoName="
                + app.getGithubRepositoryName()
                + " has already been imported by another user.");
      }
    }
    if (existsById) {
      // optional is already checked since it exists by id
      var persisted = repository.findById(id).get();
      if (persisted.isArchived()) {
        throw new BadRequestException("archived app cannot be updated");
      }
      app.setArchivedAt(persisted.getArchivedAt());
      app.setPreviousGithubRepositoryName(persisted.getGithubRepositoryName());
    } else {
      if (!isAValidAppName(app.getName(), getAppNameRegexPattern(DOMAIN_APP_NAME_MAX_LENGTH))) {
        throw new BadRequestException(
            "app_name must not have more than "
                + DOMAIN_APP_NAME_MAX_LENGTH
                + " characters and contain only lowercase letters,"
                + " numbers and hyphen (-).");
      }
    }
  }

  public static boolean isAValidAppName(String appName, Pattern regexPattern) {
    return regexPattern.matcher(appName).matches();
  }

  public static Pattern getAppNameRegexPattern(int maxLength) {
    String regex = String.format("^[a-z0-9-]{1,%d}$", maxLength);
    return Pattern.compile(regex);
  }
}
