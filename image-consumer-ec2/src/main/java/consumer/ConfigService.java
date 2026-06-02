package consumer;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.regions.Region;

public class ConfigService {
    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private String getEnv(String key) { return dotenv.get(key) != null ? dotenv.get(key) : System.getenv(key); }

    public String getSqsQueueUrl() { return getEnv("SQS_QUEUE_URL"); }

    public String getS3OutputBucketName() {
        return getEnv("S3_OUTPUT_BUCKET_NAME");
    }

    public String getSesSenderEmail() {
        return getEnv("SES_SENDER_EMAIL");
    }

    public Region getRegion() {
        String region = getEnv("APP_REGION");
        return Region.of(region != null ? region : "us-east-1");
    }
}
