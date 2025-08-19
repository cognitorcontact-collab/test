package api.poja.io.service.event;

import static java.util.UUID.randomUUID;

import api.poja.io.aws.iam.IamComponent;
import api.poja.io.endpoint.event.model.OrganizationUpserted;
import api.poja.io.repository.model.ConsoleUserGroup;
import api.poja.io.repository.model.Organization;
import api.poja.io.service.ConsoleUserGroupService;
import api.poja.io.service.ConsoleUserService;
import api.poja.io.service.organization.OrganizationService;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrganizationUpsertedService implements Consumer<OrganizationUpserted> {
  private final ConsoleUserGroupService consoleUserGroupService;
  private final ConsoleUserService consoleUserService;
  private final OrganizationService organizationService;
  private final IamComponent iamComponent;
  public static final String POJA_USER_GROUP_NAME_PREFIX = "poja-group-";

  @Override
  public void accept(OrganizationUpserted organizationUpserted) {
    var org = organizationUpserted.getOrganization();

    var consoleCredentials = consoleUserService.createConsoleUser(org.getId(), org.getName());
    createUserGroup(org, consoleCredentials.username());
  }

  private void createUserGroup(Organization org, String consoleUsername) {
    var savedGroup =
        consoleUserGroupService.createNewByOrg(
            org.getId(),
            org.getOwnerId(),
            consoleUsername,
            ConsoleUserGroup.builder()
                .name(POJA_USER_GROUP_NAME_PREFIX + randomUUID().toString().substring(0, 8))
                .available(true)
                .archived(false)
                .orgId(org.getId())
                .build());
    var policyDocumentName = org.getName() + "-group-logPolicies";
    organizationService.updateConsoleInformations(
        org.getId(), savedGroup.getName(), policyDocumentName);
  }
}
