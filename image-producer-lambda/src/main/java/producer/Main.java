package producer;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

public class Main {
    public static void main(String[] args) {
        System.out.println("Simulating Lambda execution (Strict Payload 2.0)...");

        ImageProducer producer = new ImageProducer();

        // Usando a classe oficial do Formato 2.0
        APIGatewayV2HTTPEvent mockRequest = APIGatewayV2HTTPEvent.builder()
                .withBody("{\"user-email\": \"dev@test.com\"}")
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("POST")
                                .build())
                        .build())
                .build();

        APIGatewayV2HTTPResponse response = producer.handleRequest(mockRequest, null);
        
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
    }
}
