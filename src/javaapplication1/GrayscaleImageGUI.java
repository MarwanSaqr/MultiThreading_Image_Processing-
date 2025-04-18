import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class GrayscaleImageGUI extends JFrame {
    private JLabel imageLabel;
    private BufferedImage selectedImage;
    private JComboBox<String> modeCombo;
    private JButton convertButton;
    private JCheckBox grayscaleCheck, brightnessCheck, histogramCheck;
    private JSlider brightnessSlider;

    private Map<String, Long> timingMap = new LinkedHashMap<>();
    private JTable timeTable = new JTable();

    public GrayscaleImageGUI() {
        setTitle("Image Processing GUI");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top controls
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

        // Image display with scrollbars
        imageLabel = new JLabel("No image selected");
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane imageScroll = new JScrollPane(imageLabel);
        add(imageScroll, BorderLayout.CENTER);

        // Bottom panel: timing table limited to 2 rows visible
        timeTable.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[] { "Mode", "Time (ms)" }
        ));
        JScrollPane timeScroll = new JScrollPane(timeTable);
        int rowHeight = timeTable.getRowHeight();
        JTableHeader header = timeTable.getTableHeader();
        int headerHeight = header.getPreferredSize().height;
        timeScroll.setPreferredSize(new Dimension(0, rowHeight * 2 + headerHeight));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Timing Results"));
        bottomPanel.add(timeScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

        selectImageButton.addActionListener(e -> selectImage());
        convertButton.addActionListener(e -> convertImage());
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                selectedImage = ImageIO.read(fileChooser.getSelectedFile());
                // Display full-size image to enable scrolling
                imageLabel.setIcon(new ImageIcon(selectedImage));
                imageLabel.setPreferredSize(new Dimension(
                    selectedImage.getWidth(), selectedImage.getHeight()));
                imageLabel.revalidate();
                imageLabel.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage());
            }
        }
    }

    private void convertImage() {
        if (selectedImage == null) {
            JOptionPane.showMessageDialog(this, "Please select an image first.");
            return;
        }
        boolean doGray   = grayscaleCheck.isSelected();
        boolean doBright = brightnessCheck.isSelected();
        boolean doHist   = histogramCheck.isSelected();
        int brightVal    = brightnessSlider.getValue();
        String mode      = (String) modeCombo.getSelectedItem();

        BufferedImage working = new BufferedImage(
            selectedImage.getWidth(), selectedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = working.createGraphics();
        g.drawImage(selectedImage, 0, 0, null);
        g.dispose();

        long start = System.currentTimeMillis();

        if ("Non-blocking".equals(mode)) {
            new SwingWorker<BufferedImage, Void>() {
                protected BufferedImage doInBackground() {
                    return processImage(working, doGray, doBright, brightVal);
                }
                protected void done() {
                    try {
                        BufferedImage result = get();
                        long duration = System.currentTimeMillis() - start;
                        finishUp(result, doHist, mode, duration);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            GrayscaleImageGUI.this,
                            "Error during processing: " + ex.getMessage()
                        );
                    }
                }
            }.execute();
        } else {
            BufferedImage result = processImage(working, doGray, doBright, brightVal);
            long duration = System.currentTimeMillis() - start;
            finishUp(result, doHist, mode, duration);
        }
    }

    private BufferedImage processImage(BufferedImage img, boolean doGray, boolean doBright, int brightVal) {
        BufferedImage temp = img;
        if (doGray) {
            switch ((String)modeCombo.getSelectedItem()) {
                case "Blocking":
                    temp = GrayscaleBlocking.process(temp);
                    break;
                case "Non-blocking":
                    temp = GrayscaleNonBlocking.process(temp);
                    break;
                default:
                    temp = GrayscaleSequential.process(temp);
            }
        }
        if (doBright) adjustBrightness(temp, brightVal);
        return temp;
    }

    private void finishUp(BufferedImage img, boolean doHist, String mode, long duration) {
        imageLabel.setIcon(new ImageIcon(img));
        imageLabel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        imageLabel.revalidate();

        if (doHist) showHistogram(img);

        timingMap.put(mode, duration);
        updateTable();
    }

    private void updateTable() {
        DefaultTableModel model = (DefaultTableModel) timeTable.getModel();
        model.setRowCount(0);
        timingMap.forEach((m, t) -> model.addRow(new Object[]{ m, t }));
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
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                hist[(img.getRGB(x, y) >> 16) & 0xFF]++;

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
