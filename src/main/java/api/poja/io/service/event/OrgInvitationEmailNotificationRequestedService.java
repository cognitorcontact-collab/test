package api.poja.io.service.event;

import static java.util.Collections.emptyList;

import api.poja.io.endpoint.event.model.OrgInvitationEmailNotificationRequested;
import api.poja.io.mail.Email;
import api.poja.io.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrgInvitationEmailNotificationRequestedService
    implements Consumer<OrgInvitationEmailNotificationRequested> {
  private final Mailer mailer;
  private final String orgAppInvitationBaseUrl;

  private static final String SUBJECT = "POJA invitation organization";
  private static final String HTML_BODY =
      """
     <div style="text-align: center; font-family: sans-serif;">
          <p>To join <b>%s</b> organization</p>
          <div style="display: inline-flex; align-items: center; background-color: rgb(18, 18, 18); color: white; padding: 10px 16px; border-radius: 4px; text-decoration: none; font-weight: 500; cursor: pointer;">
            <a href="%s/organizations/%s/invitations?userId=%s"
               style="color: white; text-decoration: none; font-size: 16px; cursor: pointer;">
              Accept
            </a>
          </div>
     </div>
""";

  public OrgInvitationEmailNotificationRequestedService(
      Mailer mailer, @Value("${org.app.invitation.base.url}") String orgAppInvitationBaseUrl) {
    this.mailer = mailer;
    this.orgAppInvitationBaseUrl = orgAppInvitationBaseUrl;
  }

  @Override
  @SneakyThrows
  public void accept(OrgInvitationEmailNotificationRequested orgInvitation) {
    var to = new InternetAddress(orgInvitation.getEmail());

    var htmlBody =
        HTML_BODY.formatted(
            orgInvitation.getInviterOrg(),
            orgAppInvitationBaseUrl,
            orgInvitation.getInviterOrg(),
            orgInvitation.getInvitedUser());

    mailer.accept(new Email(to, emptyList(), emptyList(), SUBJECT, htmlBody, emptyList()));
  }
}
