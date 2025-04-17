
import java.awt.image.BufferedImage;

public class GrayscaleBlocking {

    public static BufferedImage process(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        Thread t1 = new Thread(() -> {
            for (int y = 0; y < height / 2; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = img.getRGB(x, y);
                    int gray = getGray(rgb);
                    output.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for (int y = height / 2; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = img.getRGB(x, y);
                    int gray = getGray(rgb);
                    output.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
                }
            }
        });

        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return output;
    }

    private static int getGray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }
}
