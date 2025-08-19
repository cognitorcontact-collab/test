package api.poja.io.service.pojaConfHandler;

import api.poja.io.endpoint.event.model.PojaConfUploaded;
import api.poja.io.model.PojaVersion;
import api.poja.io.model.pojaConf.PojaConf;
import api.poja.io.service.appEnvConfigurer.AppEnvConfigurerService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractPojaConfUploadedHandler implements PojaConfUploadedHandler {
  private final List<PojaVersion> supportedPojaVersions;
  private final AppEnvConfigurerService appEnvConfigurerService;

  protected AbstractPojaConfUploadedHandler(
      List<PojaVersion> supportedPojaVersions, AppEnvConfigurerService appEnvConfigurerService) {
    this.supportedPojaVersions = supportedPojaVersions;
    this.appEnvConfigurerService = appEnvConfigurerService;
  }

  @Override
  public boolean supports(PojaVersion pojaVersion) {
    return supportedPojaVersions.contains(pojaVersion);
  }

  protected abstract void applyConf(PojaConfUploaded pojaConfUploaded, PojaConf pojaConf);

  @Override
  public final void accept(PojaConfUploaded pojaConfUploaded) {
    PojaVersion pojaVersion = pojaConfUploaded.getPojaVersion();
    if (!this.supports(pojaVersion)) {
      log.error(
          "expected Poja version {} does not match poja version {}",
          this.supportedPojaVersions,
          pojaVersion);
      return;
    }
    var domain =
        appEnvConfigurerService.readConfigAsDomain(
            pojaConfUploaded.getOrgId(),
            pojaConfUploaded.getAppId(),
            pojaConfUploaded.getEnvironmentId(),
            pojaConfUploaded.getFilename());
    applyConf(pojaConfUploaded, domain);
  }
}
