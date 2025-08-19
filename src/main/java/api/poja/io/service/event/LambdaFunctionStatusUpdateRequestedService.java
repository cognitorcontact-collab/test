package api.poja.io.service.event;

import api.poja.io.aws.lambda.LambdaComponent;
import api.poja.io.endpoint.event.model.LambdaFunctionStatusUpdateRequested;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LambdaFunctionStatusUpdateRequestedService
    implements Consumer<LambdaFunctionStatusUpdateRequested> {
  private final LambdaComponent lambdaComponent;

  @Override
  public void accept(LambdaFunctionStatusUpdateRequested lambdaFunctionStatusUpdateRequested) {
    switch (lambdaFunctionStatusUpdateRequested.getStatus()) {
      case SUSPEND ->
          lambdaComponent.disableFunction(lambdaFunctionStatusUpdateRequested.getFunctionName());
      case ACTIVATE ->
          lambdaComponent.enableFunction(lambdaFunctionStatusUpdateRequested.getFunctionName());
    }
  }
}
