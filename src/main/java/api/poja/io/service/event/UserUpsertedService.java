package api.poja.io.service.event;

import static java.util.UUID.randomUUID;

import api.poja.io.endpoint.event.model.UserUpserted;
import api.poja.io.endpoint.rest.model.Organization;
import api.poja.io.model.User;
import api.poja.io.service.UserService;
import api.poja.io.service.organization.OrganizationService;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserUpsertedService implements Consumer<UserUpserted> {
  private final OrganizationService organizationService;
  private final UserService userService;

  @Override
  public void accept(UserUpserted userUpserted) {
    User user = userUpserted.getUser();
    var createdOrg =
        organizationService
            .crupdateOrgs(
                user.getId(),
                List.of(
                    new Organization()
                        .id(randomUUID().toString())
                        .name("org-" + user.getUsername())))
            .getFirst();
    userService.updateMainOrgId(user.getId(), createdOrg.id());
  }
}
