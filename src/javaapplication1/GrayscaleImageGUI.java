import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

public class GrayscaleImageGUI extends JFrame {
    private DefaultListModel<File> imageListModel;
    private JList<File> imageList;
    private JButton selectImagesButton, selectDestButton, convertButton;
    private JComboBox<String> modeCombo;
    private JLabel threadLabel, destLabel;
    private JSpinner threadSpinner;
    private JCheckBox grayscaleCheck, brightnessCheck, histogramCheck;
    private JSlider brightnessSlider;
    private JTable timeTable;
    private DefaultTableModel timeModel;
    private File destDirectory;

    public GrayscaleImageGUI() {
        setTitle("Batch Image Processor");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top controls panel
        JPanel topPanel = new JPanel();
        selectImagesButton = new JButton("Select Images...");
        selectDestButton = new JButton("Select Destination...");
        modeCombo = new JComboBox<>(new String[]{"Blocking", "Non-blocking", "Single-threaded"});
        threadLabel = new JLabel("Use Threads:");
        threadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Runtime.getRuntime().availableProcessors(), 1));
        convertButton = new JButton("Start Processing");
        destLabel = new JLabel("Destination: not set");

        topPanel.add(selectImagesButton);
        topPanel.add(selectDestButton);
        topPanel.add(new JLabel("Mode:"));
        topPanel.add(modeCombo);
        topPanel.add(threadLabel);
        topPanel.add(threadSpinner);
        topPanel.add(convertButton);
        topPanel.add(destLabel);
        add(topPanel, BorderLayout.NORTH);

        modeCombo.addItemListener(e -> {
            boolean single = "Single-threaded".equals(modeCombo.getSelectedItem());
            threadLabel.setVisible(!single);
            threadSpinner.setVisible(!single);
            if (single) threadSpinner.setValue(1);
        });

        // Center
        JPanel centerPanel = new JPanel(new BorderLayout());
        imageListModel = new DefaultListModel<>();
        imageList = new JList<>(imageListModel);
        centerPanel.add(new JScrollPane(imageList), BorderLayout.CENTER);

        JPanel optionsPanel = new JPanel();
        grayscaleCheck = new JCheckBox("Grayscale");
        brightnessCheck = new JCheckBox("Brightness");
        histogramCheck = new JCheckBox("Histogram");
        brightnessSlider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
        brightnessSlider.setMajorTickSpacing(50);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPaintLabels(true);
        optionsPanel.add(grayscaleCheck);
        optionsPanel.add(brightnessCheck);
        optionsPanel.add(brightnessSlider);
        optionsPanel.add(histogramCheck);
        centerPanel.add(optionsPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom
        timeModel = new DefaultTableModel(new Object[][]{}, new String[]{"Mode", "Time (ms)"});
        timeTable = new JTable(timeModel);
        JScrollPane timeScroll = new JScrollPane(timeTable);
        int height = timeTable.getRowHeight() * 2 + timeTable.getTableHeader().getPreferredSize().height;
        timeScroll.setPreferredSize(new Dimension(0, height));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Timing Results"));
        bottomPanel.add(timeScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        selectImagesButton.addActionListener(e -> chooseImages());
        selectDestButton.addActionListener(e -> chooseDestination());
        convertButton.addActionListener(e -> processBatch());
    }

    private void chooseImages() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (File f : chooser.getSelectedFiles()) {
                imageListModel.addElement(f);
            }
        }
    }

    private void chooseDestination() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destDirectory = chooser.getSelectedFile();
            destLabel.setText("Destination: " + destDirectory.getAbsolutePath());
        }
    }

    private void processBatch() {
        if (imageListModel.isEmpty() || destDirectory == null) {
            JOptionPane.showMessageDialog(this, "Select images and destination first.");
            return;
        }

        String mode = (String) modeCombo.getSelectedItem();
        int threads = (Integer) threadSpinner.getValue();
        boolean doGray = grayscaleCheck.isSelected();
        boolean doBright = brightnessCheck.isSelected();
        boolean doHist = histogramCheck.isSelected();

        timeModel.setRowCount(0);
        long start = System.nanoTime();

        for (int i = 0; i < imageListModel.size(); i++) {
            try {
                File in = imageListModel.get(i);
                BufferedImage src = ImageIO.read(in);
                BufferedImage temp = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = temp.createGraphics();
                g.drawImage(src, 0, 0, null);
                g.dispose();

                // Grayscale
                if (doGray) {
                    temp = applyGrayscale(temp, threads);
                }

                if (doBright) {
                    adjustBrightness(temp, brightnessSlider.getValue());
                }

                // Save
                String name = in.getName().replaceAll("\\.\\w+$", "");
                File outF = new File(destDirectory, name + "_processed.png");
                ImageIO.write(temp, "png", outF);

                if (doHist) {
                    showHistogram(temp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double elapsedMs = (System.nanoTime() - start) / 1e6;
        timeModel.addRow(new Object[]{mode + " (" + threads + " threads)", String.format("%.2f", elapsedMs)});
    }

    private BufferedImage applyGrayscale(BufferedImage img, int threads) throws InterruptedException {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        int chunk = height / threads;

        for (int i = 0; i < threads; i++) {
            final int startY = i * chunk;
            final int endY = (i == threads - 1) ? height : startY + chunk;

            executor.submit(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = img.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        int gray = (r + g + b) / 3;
                        int grayRGB = (gray << 16) | (gray << 8) | gray;
                        result.setRGB(x, y, grayRGB);
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        return result;
    }

    private void adjustBrightness(BufferedImage img, int val) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) + val));
                int g = Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) + val));
                int b = Math.min(255, Math.max(0, (rgb & 0xFF) + val));
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
    }

    private void showHistogram(BufferedImage img) {
        int[] hist = new int[256];
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int r = (img.getRGB(x, y) >> 16) & 0xFF;
                hist[r]++;
            }
        }

        int max = 0;
        for (int i : hist) if (i > max) max = i;

        int width = 256, height = 150;
        BufferedImage histImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = histImg.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);

        for (int i = 0; i < hist.length; i++) {
            int value = (int) ((hist[i] / (double) max) * height);
            g.drawLine(i, height, i, height - value);
        }

        g.dispose();

        JFrame histFrame = new JFrame("Histogram");
        histFrame.setSize(width + 20, height + 40);
        histFrame.add(new JLabel(new ImageIcon(histImg)));
        histFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GrayscaleImageGUI().setVisible(true));
    }
}
