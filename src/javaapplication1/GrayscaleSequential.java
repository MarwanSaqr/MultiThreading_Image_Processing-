
import java.awt.image.BufferedImage;

public class GrayscaleSequential {

    public static BufferedImage process(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int gray = getGray(rgb);
                output.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
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
