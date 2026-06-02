package consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.concurrent.TimeUnit;

public class ImageConsumer {
    private final ConfigService config;
    private final ImageProcessor processor;
    private final ObjectMapper mapper;

    public ImageConsumer() {
        this.config = new ConfigService();
        this.processor = new ImageProcessor();
        this.mapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        new ImageConsumer().start();
    }

    public void start() {
        System.out.println("=== Consumidor Iniciado (Fluxo S3 + SES) ===");
        System.out.println("[Config] Fila: " + config.getSqsQueueUrl());
        System.out.println("[Config] S3 Saída: " + config.getS3OutputBucketName());
        System.out.println("[Config] Application Region: " + config.getRegion());
        System.out.println("[Config] Sender Email: " + config.getSesSenderEmail());

        try (
             SqsService sqs = new SqsService(config.getRegion(), config.getSqsQueueUrl());
             S3Service s3 = new S3Service(config.getRegion());
             SesService ses = new SesService(config.getRegion(), config.getSesSenderEmail())
        ) {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    for (Message msg : sqs.receiveMessages()) {
                        processMessage(msg, sqs, s3, ses);
                    }
                } catch (Exception e) {
                    System.err.println("[Erro] Falha no ciclo: " + e.getMessage());
                    handleRetryDelay();
                }
            }
        }
    }

    private void processMessage(Message msg, SqsService sqs, S3Service s3, SesService ses) throws Exception {
        System.out.println("\n[SQS] Novo evento de S3 detectado!");

        JsonNode root = mapper.readTree(msg.body());

        JsonNode records = root.get("Records");
        if (records == null || !records.isArray() || records.isEmpty()) {
            System.out.println("[SQS] Mensagem ignorada: Não contém evento S3 válido.");
            sqs.deleteMessage(msg.receiptHandle());
            return;
        }

        JsonNode s3Event = records.get(0).get("s3");

        if (s3Event == null || s3Event.get("bucket") == null || s3Event.get("object") == null) {
            System.out.println("[SQS] Formato de evento S3 inesperado.");
            sqs.deleteMessage(msg.receiptHandle());
            return;
        }

        String bucketName = s3Event.get("bucket").get("name").asText();
        String objectKey = s3Event.get("object").get("key").asText();

        if (objectKey.contains("-processed")) {
            System.out.println("[SQS] Arquivo já processado detectado (" + objectKey + "). Ignorando para evitar loop.");
            sqs.deleteMessage(msg.receiptHandle());
            return;
        }

        System.out.println("[S3] Baixando arquivo original: " + objectKey);
        ResponseBytes<GetObjectResponse> s3Response = s3.downloadImage(bucketName, objectKey);
        
        String userEmail = s3Response.response().metadata().get("user-email");
        System.out.println("[S3] Metadado encontrado - E-mail: " + userEmail);

        System.out.println("[Processamento] Redimensionando imagem...");
        byte[] processedImage = processor.process(s3Response.asByteArray());

        String outputKey = objectKey.replace("-original", "-processed");

        if (outputKey.equals(objectKey)) {
            outputKey = objectKey + "-processed";
        }

        System.out.println("[S3] Enviando versão processada: " + outputKey);
        s3.uploadImage(config.getS3OutputBucketName(), outputKey, processedImage);

        System.out.println("[S3] Gerando link temporário...");
        String presignedUrl = s3.generatePresignedUrl(config.getS3OutputBucketName(), outputKey);

        System.out.println("[SES] Disparando notificação...");
        ses.sendSuccessEmail(userEmail, outputKey, presignedUrl);

        sqs.deleteMessage(msg.receiptHandle());
        System.out.println("[Sucesso] Ciclo finalizado para: " + outputKey);
    }

    private void handleRetryDelay() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
