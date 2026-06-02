package consumer;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

public class SqsService implements AutoCloseable {
    private final SqsClient client;
    private final String queueUrl;

    public SqsService(Region region, String queueUrl) {
        this.client = SqsClient.builder().region(region).build();
        this.queueUrl = queueUrl;
    }

    public List<Message> receiveMessages() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .waitTimeSeconds(20)
                .build();
        return client.receiveMessage(request).messages();
    }

    public void deleteMessage(String receiptHandle) {
        client.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build());
    }

    @Override
    public void close() {
        client.close();
    }
}
