
import java.awt.image.BufferedImage;

public class GrayscaleNonBlocking {

    public static BufferedImage process(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        BufferedImage part1 = new BufferedImage(width, height / 2, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage part2 = new BufferedImage(width, height - height / 2, BufferedImage.TYPE_BYTE_GRAY);

        Thread t1 = new Thread(() -> fillGray(img, part1, 0));
        Thread t2 = new Thread(() -> fillGray(img, part2, height / 2));

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Combine parts
        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width; x++) {
                output.setRGB(x, y, part1.getRGB(x, y));
            }
        }
        for (int y = height / 2; y < height; y++) {
            for (int x = 0; x < width; x++) {
                output.setRGB(x, y, part2.getRGB(x, y - height / 2));
            }
        }

        return output;
    }

    private static void fillGray(BufferedImage original, BufferedImage part, int offsetY) {
        for (int y = 0; y < part.getHeight(); y++) {
            for (int x = 0; x < part.getWidth(); x++) {
                int rgb = original.getRGB(x, y + offsetY);
                int gray = getGray(rgb);
                part.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
    }

    private static int getGray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }
}
