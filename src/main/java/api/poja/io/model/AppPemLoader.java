package api.poja.io.model;

import api.poja.io.file.bucket.BucketComponent;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppPemLoader {
  private final BucketComponent bucketComponent;
  private final String appPemBucketKey;
  private static final String UTF_8 = "UTF-8";
  private String rs256privateKey;

  public AppPemLoader(
      BucketComponent bucketComponent, @Value("${app.pem.bucket.key}") String appPemBucketKey) {
    this.bucketComponent = bucketComponent;
    this.appPemBucketKey = appPemBucketKey;
  }

  @PostConstruct
  public void init() throws IOException {
    File appPem = bucketComponent.download(appPemBucketKey);
    this.rs256privateKey = FileUtils.readFileToString(appPem, UTF_8);
  }
}
