package api.poja.io.service.pojaConfHandler;

import api.poja.io.endpoint.event.model.PojaConfUploaded;
import api.poja.io.model.PojaVersion;
import java.util.function.Consumer;

public interface PojaConfUploadedHandler extends Consumer<PojaConfUploaded> {
  void accept(PojaConfUploaded pojaConfUploaded);

  boolean supports(PojaVersion pojaVersion);
}
