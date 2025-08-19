package api.poja.io.service.pojaConfHandler;

import api.poja.io.endpoint.event.model.PojaConfUploaded;
import api.poja.io.model.PojaVersion;
import api.poja.io.model.exception.NotImplementedException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Slf4j
public class PojaConfUploadedHandlerFacade implements PojaConfUploadedHandler {
  private final PojaConfUploadedHandler pojaUploadedHandler;

  public PojaConfUploadedHandlerFacade(
      @Qualifier("pojaUploadedHandler") PojaConfUploadedHandler pojaUploadedHandler) {
    this.pojaUploadedHandler = pojaUploadedHandler;
  }

  @Transactional
  @Override
  public void accept(PojaConfUploaded pojaConfUploaded) {
    getPojaConfUploadedHandler(pojaConfUploaded.getPojaVersion()).accept(pojaConfUploaded);
  }

  @Override
  public boolean supports(PojaVersion pojaVersion) {
    throw new UnsupportedOperationException("method unused");
  }

  private PojaConfUploadedHandler getPojaConfUploadedHandler(PojaVersion pojaVersion) {
    if (pojaUploadedHandler.supports(pojaVersion)) {
      return pojaUploadedHandler;
    }
    ;
    throw new NotImplementedException("not implemented");
  }
}
