package consumer;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

public class SesService implements AutoCloseable {
    private final SesV2Client client;
    private final String senderEmail;

    public SesService(Region region, String senderEmail) {
        this.client = SesV2Client.builder().region(region).build();
        this.senderEmail = senderEmail;
    }

    public void sendSuccessEmail(String recipientEmail, String fileName, String presignedUrl) {
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            System.err.println("[SES] E-mail do destinatário não encontrado nos metadados.");
            return;
        }

        String subject = "Sua imagem foi processada com sucesso!";
        String bodyText = "Olá!\n\nA sua imagem " + fileName + " foi processada e já está disponível.\n\n" +
                "Você pode acessá-la pelo link abaixo (válido por 1 hora):\n" + presignedUrl + "\n\n" +
                "Obrigado!";

        SendEmailRequest request = SendEmailRequest.builder()
                .fromEmailAddress(senderEmail)
                .destination(Destination.builder().toAddresses(recipientEmail).build())
                .content(EmailContent.builder()
                        .simple(Message.builder()
                                .subject(Content.builder().data(subject).build())
                                .body(Body.builder()
                                        .text(Content.builder().data(bodyText).build())
                                        .build())
                                .build())
                        .build())
                .build();

        client.sendEmail(request);
        System.out.println("[SES] E-mail de notificação enviado para: " + recipientEmail);
    }

    @Override
    public void close() {
        client.close();
    }
}
