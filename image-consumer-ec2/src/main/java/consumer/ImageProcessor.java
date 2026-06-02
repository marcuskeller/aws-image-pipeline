package consumer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageProcessor {
    private static final int LARGURA_ALVO = 800;

    public byte[] process(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage original = ImageIO.read(bais);
            if (original == null) throw new IOException("Formato de imagem inválido");

            BufferedImage resized = resize(original);
            
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(resized, "jpg", baos);
                return baos.toByteArray();
            }
        }
    }

    private BufferedImage resize(BufferedImage img) {
        int novaAltura = (int) (img.getHeight() * ((double) LARGURA_ALVO / img.getWidth()));
        Image temp = img.getScaledInstance(LARGURA_ALVO, novaAltura, Image.SCALE_SMOOTH);
        BufferedImage redimensionada = new BufferedImage(LARGURA_ALVO, novaAltura, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = redimensionada.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();
        
        return redimensionada;
    }
}
