package api.poja.io.service.event;

import api.poja.io.endpoint.event.model.PojaConfUploaded;
import api.poja.io.service.pojaConfHandler.PojaConfUploadedHandler;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PojaConfUploadedService implements Consumer<PojaConfUploaded> {
  public static final String POJA_BOT_USERNAME = "poja[bot]";
  @Deprecated public static final String JCLOUDIFY_BOT_USERNAME = "jcloudify[bot]";
  private final PojaConfUploadedHandler pojaConfUploadedHandler;

  @Override
  public void accept(PojaConfUploaded pojaConfUploaded) {
    try {
      Objects.requireNonNull(handlePojaConfUploaded(pojaConfUploaded)).call();
      log.info("Success at PojaConfUploaded");
    } catch (Exception e) {
      log.info("Failure at PojaConfUploaded");
      log.error("Error Message: {}", e.getMessage());
      log.error("Stacktrace: {}", (Object) e.getStackTrace());
      log.error("Error Cause: {}", e.getCause().toString());
      throw new RuntimeException(e);
    }
  }

  private Callable<Void> handlePojaConfUploaded(PojaConfUploaded pojaConfUploaded) {
    return () -> {
      pojaConfUploadedHandler.accept(pojaConfUploaded);
      return null;
    };
  }
}
