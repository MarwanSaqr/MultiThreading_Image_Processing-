import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GrayscaleImageGUI extends JFrame {
    private JLabel imageLabel;
    private BufferedImage selectedImage;
    private JComboBox<String> modeCombo;
    private JButton convertButton;
    private JCheckBox grayscaleCheck, brightnessCheck, histogramCheck;
    private JSlider brightnessSlider;

    public GrayscaleImageGUI() {
        setTitle("Image Processing GUI");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JButton selectImageButton = new JButton("Select Image");
        modeCombo = new JComboBox<>(new String[]{ "Blocking", "Non-blocking", "Single-threaded" });
        convertButton = new JButton("Process Image");

        grayscaleCheck = new JCheckBox("Grayscale");
        brightnessCheck = new JCheckBox("Brightness");
        histogramCheck = new JCheckBox("Histogram");

        brightnessSlider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
        brightnessSlider.setMajorTickSpacing(50);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPaintLabels(true);

        JPanel topPanel = new JPanel();
        topPanel.add(selectImageButton);
        topPanel.add(modeCombo);
        topPanel.add(grayscaleCheck);
        topPanel.add(brightnessCheck);
        topPanel.add(brightnessSlider);
        topPanel.add(histogramCheck);
        topPanel.add(convertButton);

        imageLabel = new JLabel("No image selected", JLabel.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        selectImageButton.addActionListener(e -> selectImage());
        convertButton.addActionListener(e -> convertImage());
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                selectedImage = ImageIO.read(file);
                imageLabel.setIcon(new ImageIcon(selectedImage.getScaledInstance(600, -1, Image.SCALE_SMOOTH)));
                imageLabel.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading image.");
            }
        }
    }

    private void convertImage() {
        if (selectedImage == null) {
            JOptionPane.showMessageDialog(this, "Please select an image first.");
            return;
        }

        String mode = (String) modeCombo.getSelectedItem();
        boolean doGray = grayscaleCheck.isSelected();
        boolean doBright = brightnessCheck.isSelected();
        boolean doHist = histogramCheck.isSelected();
        int brightVal = brightnessSlider.getValue();

        BufferedImage temp = new BufferedImage(selectedImage.getWidth(), selectedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = temp.createGraphics();
        g2d.drawImage(selectedImage, 0, 0, null);
        g2d.dispose();

        // Process based on mode
        if (doGray) {
            temp = GrayscaleSequential.process(temp);
            if ("Blocking".equals(mode)) temp = GrayscaleBlocking.process(temp);
            else if ("Non-blocking".equals(mode)) temp = GrayscaleNonBlocking.process(temp);
        }
        if (doBright) {
            adjustBrightness(temp, brightVal);
        }

        imageLabel.setIcon(new ImageIcon(temp.getScaledInstance(600, -1, Image.SCALE_SMOOTH)));
        imageLabel.setText("");

        if (doHist) {
            showHistogram(temp);
        }
    }

    private void adjustBrightness(BufferedImage img, int value) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = clamp(((rgb >> 16) & 0xFF) + value);
                int g = clamp(((rgb >> 8) & 0xFF) + value);
                int b = clamp((rgb & 0xFF) + value);
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
    }

    private int clamp(int val) {
        return Math.min(255, Math.max(0, val));
    }

    private void showHistogram(BufferedImage img) {
        int[] hist = new int[256];
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int gray = ( (rgb >> 16) & 0xFF );
                hist[gray]++;
            }
        }
        int max = 0;
        for (int v : hist) max = Math.max(max, v);
        int w = 256, h = 150;
        BufferedImage histImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = histImg.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        for (int i = 0; i < 256; i++) {
            int bar = (int)(((double)hist[i] / max) * h);
            g.drawLine(i, h, i, h - bar);
        }
        g.dispose();
        JFrame frame = new JFrame("Histogram");
        frame.setSize(w + 20, h + 40);
        frame.add(new JLabel(new ImageIcon(histImg)));
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GrayscaleImageGUI gui = new GrayscaleImageGUI();
            gui.setVisible(true);
        });
    }
}
