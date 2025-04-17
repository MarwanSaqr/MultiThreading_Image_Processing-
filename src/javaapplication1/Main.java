import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select mode:");
        System.out.println("1 - Blocking");
        System.out.println("2 - Non-blocking");
        System.out.println("3 - Single-threaded");

        int choice = scanner.nextInt();
        scanner.close();

        String inputPath = "input.jpg";
        String outputPath = "";

        try {
            BufferedImage image = ImageIO.read(new File(inputPath));
            BufferedImage result = null;

            switch (choice) {
                case 1:
                    result = GrayscaleBlocking.process(image);
                    outputPath = "output_blocking.jpg";
                    break;
                case 2:
                    result = GrayscaleNonBlocking.process(image);
                    outputPath = "output_nonblocking.jpg";
                    break;
                case 3:
                    result = GrayscaleSequential.process(image);
                    outputPath = "output_sequential.jpg";
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            ImageIO.write(result, "jpg", new File(outputPath));
            System.out.println("Image saved to " + outputPath);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
