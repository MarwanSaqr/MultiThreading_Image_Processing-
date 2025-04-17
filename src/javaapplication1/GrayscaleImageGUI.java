import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GrayscaleImageGUI extends JFrame {
    private JLabel imageLabel;
    private BufferedImage selectedImage;
    private JComboBox<String> modeCombo;
    private JButton convertButton;

    public GrayscaleImageGUI() {
        setTitle("Grayscale Image Converter");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JButton selectImageButton = new JButton("Select Image");
        modeCombo = new JComboBox<>(new String[]{"Blocking", "Non-blocking", "Single-threaded"});
        convertButton = new JButton("Convert to Grayscale");

        JPanel topPanel = new JPanel();
        topPanel.add(selectImageButton);
        topPanel.add(modeCombo);
        topPanel.add(convertButton);

        imageLabel = new JLabel("No image selected", JLabel.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(imageLabel, BorderLayout.CENTER);

        selectImageButton.addActionListener(e -> selectImage());
        convertButton.addActionListener(e -> convertImage());
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                selectedImage = ImageIO.read(file);
                imageLabel.setText("Image loaded: " + file.getName());
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
        BufferedImage result = null;

        switch (mode) {
            case "Blocking":
                result = GrayscaleBlocking.process(selectedImage);
                break;
            case "Non-blocking":
                result = GrayscaleNonBlocking.process(selectedImage);
                break;
            case "Single-threaded":
                result = GrayscaleSequential.process(selectedImage);
                break;
        }

        try {
            File output = new File("output_gui.jpg");
            ImageIO.write(result, "jpg", output);
            JOptionPane.showMessageDialog(this, "Image saved as output_gui.jpg");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving image.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GrayscaleImageGUI().setVisible(true));
    }
}
