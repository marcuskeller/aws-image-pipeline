package producer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class ImageProducer implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final ConfigurationService config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageProducer() {
        this.config = new ConfigurationService();
    }

    public ImageProducer(ConfigurationService config) {
        this.config = config;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent input, final Context context) {
        try {

            String userEmail = parseUserEmail(input.getBody());
            String uploadUrl = generatePresignedUrl(userEmail);

            return buildResponse(200, "{\"uploadUrl\": \"" + uploadUrl + "\"}");

        } catch (IllegalArgumentException e) {
            return buildResponse(400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return buildResponse(500, "{\"error\": \"Internal error: " + e.getMessage() + "\"}");
        }
    }

    private String parseUserEmail(String body) throws Exception {
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Request body is empty");
        }
        Map<String, String> data = objectMapper.readValue(body, new TypeReference<>() {});
        String email = data.get("user-email");
        
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Field 'user-email' is missing");
        }
        return email;
    }

    private String generatePresignedUrl(String userEmail) {
        String bucketName = config.get("S3_BUCKET_NAME", "S3_BUCKET_NAME");
        Region region = Region.of(config.get("APP_REGION", "us-east-1"));

        try (S3Presigner presigner = S3Presigner.builder().region(region).build()) {
            String s3Key = UUID.randomUUID() + "-original";

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .metadata(Map.of("user-email", userEmail))
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            return presigner.presignPutObject(presignRequest).url().toString();
        }
    }

    private APIGatewayV2HTTPResponse buildResponse(int statusCode, String body) {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(statusCode)
                .withBody(body)
                .build();
    }
}
