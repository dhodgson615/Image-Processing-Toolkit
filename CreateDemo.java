import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CreateDemo {
    public static void main(String[] args) throws Exception {
        // Create a demo image with a pattern
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Create a colorful pattern
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                int r = (x * 255 / 200);
                int green = (y * 255 / 200);
                int b = ((x + y) * 255 / 400);
                g2d.setColor(new Color(r, green, b));
                g2d.fillRect(x, y, 1, 1);
            }
        }
        g2d.dispose();
        
        ImageIO.write(image, "png", new File("demo.png"));
        System.out.println("Demo image created: demo.png");
    }
}