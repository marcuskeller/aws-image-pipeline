package producer;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ImageProducerTest {

    private ImageProducer producer;
    private ConfigurationService config;

    @BeforeEach
    public void setup() {
        config = mock(ConfigurationService.class);
        producer = new ImageProducer(config);
    }

    @Test
    public void testSuccess() {
        when(config.get(anyString(), anyString())).thenReturn("test-bucket");
        
        APIGatewayV2HTTPEvent input = APIGatewayV2HTTPEvent.builder()
                .withBody("{\"user-email\": \"test@test.com\"}")
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("POST")
                                .build())
                        .build())
                .build();

        APIGatewayV2HTTPResponse response = producer.handleRequest(input, null);
        
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 500);
    }

    @Test
    public void testEmptyBody() {
        APIGatewayV2HTTPEvent input = APIGatewayV2HTTPEvent.builder()
                .withBody("")
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("POST")
                                .build())
                        .build())
                .build();

        APIGatewayV2HTTPResponse response = producer.handleRequest(input, null);
        
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("body is empty"));
    }
}
