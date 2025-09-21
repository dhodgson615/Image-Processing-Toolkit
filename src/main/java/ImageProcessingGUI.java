import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Java Swing GUI for the Image Processing Toolkit.
 * Provides a user-friendly interface with sliders and buttons for real-time image effects.
 */
public class ImageProcessingGUI extends JFrame {
    
    // GUI Components
    private JLabel originalImageLabel;
    private JLabel processedImageLabel;
    private JScrollPane originalScrollPane;
    private JScrollPane processedScrollPane;
    
    // Control Components
    private JCheckBox useBinaryThresholdCheck;
    private JCheckBox adjustBlackPixelsCheck;
    private JCheckBox useMultipleThresholdsCheck;
    private JCheckBox applyContrastCheck;
    private JCheckBox invertColorsCheck;
    
    private JSlider binaryThresholdSlider;
    private JSlider whiteThresholdSlider;
    private JSlider blackThresholdSlider;
    private JSlider contrastThresholdSlider;
    private JSlider multiplierSlider;
    
    private JLabel binaryThresholdValue;
    private JLabel whiteThresholdValue;
    private JLabel blackThresholdValue;
    private JLabel contrastThresholdValue;
    private JLabel multiplierValue;
    
    private JLabel statusLabel;
    
    // Data
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private Main.ImageConfig config;
    
    private final int MAX_DISPLAY_SIZE = 400; // Maximum size for display
    
    public ImageProcessingGUI() {
        config = new Main.ImageConfig();
        initializeGUI();
        updateProcessedImage();
    }
    
    private void initializeGUI() {
        setTitle("Image Processing Toolkit");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create menu bar
        createMenuBar();
        
        // Create main panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Control panel on the left
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.WEST);
        
        // Image display panel in the center
        JPanel imagePanel = createImagePanel();
        mainPanel.add(imagePanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Status bar at the bottom
        statusLabel = new JLabel("No image loaded. Use File > Open to load an image.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
        
        // Set window properties
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        openItem.addActionListener(e -> openImage());
        
        JMenuItem saveItem = new JMenuItem("Save...");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveItem.addActionListener(e -> saveImage());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(300, 0));
        
        // Effect toggles
        JPanel effectsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        effectsPanel.setBorder(new TitledBorder("Effects"));
        
        useBinaryThresholdCheck = new JCheckBox("Use Binary Threshold", config.useBinaryThreshold);
        useBinaryThresholdCheck.addActionListener(e -> {
            config.useBinaryThreshold = useBinaryThresholdCheck.isSelected();
            updateProcessedImage();
        });
        
        adjustBlackPixelsCheck = new JCheckBox("Adjust Black Pixel Neighbors", config.adjustBlackPixelsNeighbors);
        adjustBlackPixelsCheck.addActionListener(e -> {
            config.adjustBlackPixelsNeighbors = adjustBlackPixelsCheck.isSelected();
            updateProcessedImage();
        });
        
        useMultipleThresholdsCheck = new JCheckBox("Use Multiple Thresholds", config.useMultipleThresholds);
        useMultipleThresholdsCheck.addActionListener(e -> {
            config.useMultipleThresholds = useMultipleThresholdsCheck.isSelected();
            updateProcessedImage();
        });
        
        applyContrastCheck = new JCheckBox("Apply Contrast", config.applyContrast);
        applyContrastCheck.addActionListener(e -> {
            config.applyContrast = applyContrastCheck.isSelected();
            updateProcessedImage();
        });
        
        invertColorsCheck = new JCheckBox("Invert Colors", config.invertColors);
        invertColorsCheck.addActionListener(e -> {
            config.invertColors = invertColorsCheck.isSelected();
            updateProcessedImage();
        });
        
        effectsPanel.add(useBinaryThresholdCheck);
        effectsPanel.add(adjustBlackPixelsCheck);
        effectsPanel.add(useMultipleThresholdsCheck);
        effectsPanel.add(applyContrastCheck);
        effectsPanel.add(invertColorsCheck);
        
        controlPanel.add(effectsPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        
        // Threshold controls
        JPanel thresholdPanel = new JPanel(new GridBagLayout());
        thresholdPanel.setBorder(new TitledBorder("Threshold Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Binary threshold
        gbc.gridx = 0; gbc.gridy = 0;
        thresholdPanel.add(new JLabel("Binary Threshold:"), gbc);
        gbc.gridx = 1;
        binaryThresholdSlider = new JSlider(0, 100, (int)(config.binaryThreshold * 100));
        binaryThresholdSlider.addChangeListener(e -> {
            config.binaryThreshold = binaryThresholdSlider.getValue() / 100.0;
            binaryThresholdValue.setText(String.format("%.2f", config.binaryThreshold));
            updateProcessedImage();
        });
        thresholdPanel.add(binaryThresholdSlider, gbc);
        gbc.gridx = 2;
        binaryThresholdValue = new JLabel(String.format("%.2f", config.binaryThreshold));
        thresholdPanel.add(binaryThresholdValue, gbc);
        
        // White threshold
        gbc.gridx = 0; gbc.gridy = 1;
        thresholdPanel.add(new JLabel("White Threshold:"), gbc);
        gbc.gridx = 1;
        whiteThresholdSlider = new JSlider(0, 100, (int)(config.whiteThreshold * 100));
        whiteThresholdSlider.addChangeListener(e -> {
            config.whiteThreshold = whiteThresholdSlider.getValue() / 100.0;
            whiteThresholdValue.setText(String.format("%.2f", config.whiteThreshold));
            updateProcessedImage();
        });
        thresholdPanel.add(whiteThresholdSlider, gbc);
        gbc.gridx = 2;
        whiteThresholdValue = new JLabel(String.format("%.2f", config.whiteThreshold));
        thresholdPanel.add(whiteThresholdValue, gbc);
        
        // Black threshold
        gbc.gridx = 0; gbc.gridy = 2;
        thresholdPanel.add(new JLabel("Black Threshold:"), gbc);
        gbc.gridx = 1;
        blackThresholdSlider = new JSlider(0, 100, (int)(config.blackThreshold * 100));
        blackThresholdSlider.addChangeListener(e -> {
            config.blackThreshold = blackThresholdSlider.getValue() / 100.0;
            blackThresholdValue.setText(String.format("%.2f", config.blackThreshold));
            updateProcessedImage();
        });
        thresholdPanel.add(blackThresholdSlider, gbc);
        gbc.gridx = 2;
        blackThresholdValue = new JLabel(String.format("%.2f", config.blackThreshold));
        thresholdPanel.add(blackThresholdValue, gbc);
        
        // Contrast threshold
        gbc.gridx = 0; gbc.gridy = 3;
        thresholdPanel.add(new JLabel("Contrast Threshold:"), gbc);
        gbc.gridx = 1;
        contrastThresholdSlider = new JSlider(0, 100, (int)(config.contrastThreshold * 100));
        contrastThresholdSlider.addChangeListener(e -> {
            config.contrastThreshold = contrastThresholdSlider.getValue() / 100.0;
            contrastThresholdValue.setText(String.format("%.2f", config.contrastThreshold));
            updateProcessedImage();
        });
        thresholdPanel.add(contrastThresholdSlider, gbc);
        gbc.gridx = 2;
        contrastThresholdValue = new JLabel(String.format("%.2f", config.contrastThreshold));
        thresholdPanel.add(contrastThresholdValue, gbc);
        
        // Multiplier
        gbc.gridx = 0; gbc.gridy = 4;
        thresholdPanel.add(new JLabel("Multiplier:"), gbc);
        gbc.gridx = 1;
        multiplierSlider = new JSlider(0, 300, (int)(config.multiplier * 100));
        multiplierSlider.addChangeListener(e -> {
            config.multiplier = multiplierSlider.getValue() / 100.0;
            multiplierValue.setText(String.format("%.2f", config.multiplier));
            updateProcessedImage();
        });
        thresholdPanel.add(multiplierSlider, gbc);
        gbc.gridx = 2;
        multiplierValue = new JLabel(String.format("%.2f", config.multiplier));
        thresholdPanel.add(multiplierValue, gbc);
        
        controlPanel.add(thresholdPanel);
        
        return controlPanel;
    }
    
    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Original image panel
        JPanel originalPanel = new JPanel(new BorderLayout());
        originalPanel.setBorder(new TitledBorder("Original Image"));
        originalImageLabel = new JLabel("No image loaded", JLabel.CENTER);
        originalImageLabel.setBackground(Color.LIGHT_GRAY);
        originalImageLabel.setOpaque(true);
        originalScrollPane = new JScrollPane(originalImageLabel);
        originalScrollPane.setPreferredSize(new Dimension(MAX_DISPLAY_SIZE, MAX_DISPLAY_SIZE));
        originalPanel.add(originalScrollPane, BorderLayout.CENTER);
        
        // Processed image panel
        JPanel processedPanel = new JPanel(new BorderLayout());
        processedPanel.setBorder(new TitledBorder("Processed Image"));
        processedImageLabel = new JLabel("No image loaded", JLabel.CENTER);
        processedImageLabel.setBackground(Color.LIGHT_GRAY);
        processedImageLabel.setOpaque(true);
        processedScrollPane = new JScrollPane(processedImageLabel);
        processedScrollPane.setPreferredSize(new Dimension(MAX_DISPLAY_SIZE, MAX_DISPLAY_SIZE));
        processedPanel.add(processedScrollPane, BorderLayout.CENTER);
        
        imagePanel.add(originalPanel);
        imagePanel.add(processedPanel);
        
        return imagePanel;
    }
    
    private void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Image File");
        
        // Add file filters for supported formats
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
            "Image Files", "png", "jpg", "jpeg", "bmp", "gif", "tiff", "wbmp");
        fileChooser.setFileFilter(imageFilter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile);
                if (originalImage != null) {
                    displayOriginalImage();
                    updateProcessedImage();
                    statusLabel.setText("Loaded: " + selectedFile.getName() + 
                        " (" + originalImage.getWidth() + "x" + originalImage.getHeight() + ")");
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to load image file.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveImage() {
        if (processedImage == null) {
            JOptionPane.showMessageDialog(this, "No processed image to save.", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Processed Image");
        
        // Add file filters for supported formats
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG Files", "png");
        FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG Files", "jpg", "jpeg");
        FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter("BMP Files", "bmp");
        
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.addChoosableFileFilter(jpgFilter);
        fileChooser.addChoosableFileFilter(bmpFilter);
        fileChooser.setFileFilter(pngFilter);
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Get file extension from filter
            String format = "png";
            if (fileChooser.getFileFilter() == jpgFilter) {
                format = "jpg";
            } else if (fileChooser.getFileFilter() == bmpFilter) {
                format = "bmp";
            }
            
            // Add extension if not present
            String fileName = selectedFile.getName();
            if (!fileName.toLowerCase().endsWith("." + format)) {
                selectedFile = new File(selectedFile.getParent(), fileName + "." + format);
            }
            
            try {
                ImageIO.write(processedImage, format, selectedFile);
                statusLabel.setText("Saved: " + selectedFile.getName());
                JOptionPane.showMessageDialog(this, "Image saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void displayOriginalImage() {
        if (originalImage != null) {
            ImageIcon icon = new ImageIcon(scaleImageForDisplay(originalImage));
            originalImageLabel.setIcon(icon);
            originalImageLabel.setText("");
        }
    }
    
    private void updateProcessedImage() {
        if (originalImage != null) {
            try {
                // Use reflection to access the private processImage method
                java.lang.reflect.Method processImageMethod = Main.class.getDeclaredMethod(
                    "processImage", BufferedImage.class, Main.ImageConfig.class);
                processImageMethod.setAccessible(true);
                
                processedImage = (BufferedImage) processImageMethod.invoke(null, originalImage, config);
                
                ImageIcon icon = new ImageIcon(scaleImageForDisplay(processedImage));
                processedImageLabel.setIcon(icon);
                processedImageLabel.setText("");
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error processing image: " + e.getMessage());
            }
        }
    }
    
    private BufferedImage scaleImageForDisplay(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Calculate scaling to fit within MAX_DISPLAY_SIZE while maintaining aspect ratio
        double scale = Math.min((double) MAX_DISPLAY_SIZE / width, (double) MAX_DISPLAY_SIZE / height);
        
        if (scale >= 1.0) {
            return image; // No scaling needed
        }
        
        int scaledWidth = (int) (width * scale);
        int scaledHeight = (int) (height * scale);
        
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        return scaledImage;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ImageProcessingGUI().setVisible(true);
        });
    }
}