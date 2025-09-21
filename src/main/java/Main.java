import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.lang.System.out;
import static javax.imageio.ImageIO.read;
import static javax.imageio.ImageIO.write;

/**
 * Image processing application that applies various thresholds and adjustments to an image.
 * It supports binary thresholding, multiple thresholds, contrast adjustment, and color inversion.
 */
public class Main {

    /**
     * Configuration class for image processing parameters.
     * It allows customization of threshold values and processing options.
     */
    static class ImageConfig {
        boolean useBinaryThreshold         = true,
                adjustBlackPixelsNeighbors = false,
                useMultipleThresholds      = false,
                applyContrast              = false,
                invertColors               = false;

        double  binaryThreshold   = 0.53,
                whiteThreshold    = 0.9,
                blackThreshold    = 0.7,
                contrastThreshold = 0.0,
                multiplier        = 0.0;
    }

    /**
     * Main method to run the image processing application.
     * It reads an input image, processes it according to the configuration,
     * and saves the output image.
     *
     * @param args command line arguments: [inputFile] [outputFile] [outputFormat]
     *             If no arguments provided, defaults to "img.png" input and auto-numbered PNG output
     *             Use "--gui" as first argument to launch the GUI interface
     */
    public static void main(String[] args) {
        // Check if GUI mode is requested
        if (args.length > 0 && "--gui".equals(args[0])) {
            // Launch GUI
            SwingUtilities.invokeLater(() -> {
                try {
                    new ImageProcessingGUI().setVisible(true);
                } catch (Exception e) {
                    System.err.println("Error launching GUI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            return;
        }
        
        // Original command-line functionality
        try {
            ImageConfig config = new ImageConfig();
            
            // Determine input file
            String inputFile = args.length > 0 ? args[0] : "img.png";
            
            // Validate input file exists and format is supported
            File input = new File(inputFile);
            if (!input.exists()) {
                System.err.println("Error: Input file '" + inputFile + "' not found.");
                return;
            }
            
            String inputFormat = getFileExtension(inputFile);
            if (!isFormatSupported(inputFormat, true)) {
                System.err.println("Error: Input format '" + inputFormat + "' is not supported.");
                System.err.println("Supported formats: " + String.join(", ", getSupportedFormats(true)));
                return;
            }
            
            BufferedImage inputImage = read(input);
            BufferedImage processedImage = processImage(inputImage, config);
            
            // Determine output file and format
            if (args.length >= 2) {
                String outputFile = args[1];
                String outputFormat = args.length >= 3 ? args[2] : getFileExtension(outputFile);
                
                if (!isFormatSupported(outputFormat, false)) {
                    System.err.println("Error: Output format '" + outputFormat + "' is not supported.");
                    System.err.println("Supported formats: " + String.join(", ", getSupportedFormats(false)));
                    return;
                }
                
                saveImage(processedImage, outputFile, outputFormat);
            } else {
                // Use legacy behavior for backward compatibility
                saveImage(processedImage);
            }
        }
        catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Processes the input image based on the provided configuration.
     * It applies thresholding, adjusts black pixel neighbors, and inverts colors if specified.
     *
     * @param image  the input image to process
     * @param config the configuration for processing
     *               
     * @return the processed image
     */
    private static BufferedImage processImage(BufferedImage image, ImageConfig config) {
        int width = image.getWidth(),
            height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, TYPE_INT_RGB);

        applyThreshold(image, result, config);

        if (config.useBinaryThreshold && config.adjustBlackPixelsNeighbors) {
             adjustBlackPixelsNeighbors(result);
        }

        if (config.invertColors) {
            invertColors(result);
        }

        return result;
    }

    /**
     * Applies thresholding to the source image and writes the result to the target image.
     * It supports binary thresholding and multiple thresholds based on the configuration.
     *
     * @param source the source image to process
     * @param target the target image to write the processed pixels
     * @param config the configuration for thresholding
     */
    private static void applyThreshold(BufferedImage source, BufferedImage target, ImageConfig config) {
        int width = source.getWidth(),
            height = source.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(source.getRGB(x, y));
                double magnitude = calculateMagnitude(pixelColor);

                if (config.useBinaryThreshold) {
                    int outputColor = (magnitude > config.binaryThreshold) ? 0xFFFFFF : 0x000000;
                    target.setRGB(x, y, outputColor);
                } else if (config.useMultipleThresholds) {
                    applyMultipleThresholds(pixelColor, magnitude, target, x, y, config);
                } else {
                    target.setRGB(x, y, pixelColor.getRGB());
                }
            }
        }
    }

    /**
     * Calculates the magnitude of a color based on its RGB components.
     * The magnitude is normalized to a range of 0 to 1.
     *
     * @param color the color to calculate the magnitude for
     *              
     * @return the normalized magnitude of the color
     */
    private static double calculateMagnitude(Color color) {
        int r2 = color.getRed() * color.getRed(),
            g2 = color.getGreen() * color.getGreen(),
            b2 = color.getBlue() * color.getBlue();

        return sqrt(r2 + g2 + b2) / sqrt(3 * 255 * 255);
    }

    /**
     * Applies multiple thresholds to a pixel color and modifies the target image accordingly.
     * It adjusts the pixel color based on contrast, white, and black thresholds.
     *
     * @param pixelColor the color of the pixel to process
     * @param magnitude  the normalized magnitude of the pixel color
     * @param target     the target image to write the processed pixel
     * @param x          the x-coordinate of the pixel in the image
     * @param y          the y-coordinate of the pixel in the image
     * @param config     the configuration for processing
     */
    private static void applyMultipleThresholds(Color pixelColor, double magnitude, BufferedImage target, int x, int y, ImageConfig config) {
        if (config.applyContrast && magnitude < config.contrastThreshold) {
            int newRed = (int) min(255, pixelColor.getRed() * config.multiplier);
            int newGreen = (int) min(255, pixelColor.getGreen() * config.multiplier);
            int newBlue = (int) min(255, pixelColor.getBlue() * config.multiplier);
            target.setRGB(x, y, new Color(newRed, newGreen, newBlue).getRGB());
        } else if (magnitude > config.whiteThreshold) {
            target.setRGB(x, y, 0xFFFFFF);
        } else if (magnitude < config.blackThreshold) {
            target.setRGB(x, y, 0x000000);
        } else {
            target.setRGB(x, y, pixelColor.getRGB());
        }
    }

    /**
     * Adjusts the neighbors of black pixels in the image.
     * If a black pixel is found, its neighboring white pixels are set to black.
     *
     * @param image the image to process
     */
    private static void adjustBlackPixelsNeighbors(BufferedImage image) {
        int width = image.getWidth(),
            height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((image.getRGB(x, y) & 0x00FFFFFF) == 0) {
                    checkAndAdjustNeighbors(image, x, y);
                }
            }
        }
    }

    /**
     * Inverts the colors of the image.
     * Each pixel's color is inverted by subtracting its RGB components from 255.
     *
     * @param image the image to invert colors for
     */
    private static void invertColors(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                Color inverted = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
                image.setRGB(x, y, inverted.getRGB());
            }
        }
    }

    /**
     * Checks the neighbors of a black pixel and adjusts their colors if they are white.
     * It sets neighboring white pixels to black.
     *
     * @param image the image to process
     * @param x     the x-coordinate of the black pixel
     * @param y     the y-coordinate of the black pixel
     */
    private static void checkAndAdjustNeighbors(BufferedImage image, int x, int y) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1}, {0, -1},
                {0, 1}, {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] dir : directions) {
            int dx = x + dir[0];
            int dy = y + dir[1];
            if (isValidPosition(dx, dy, w, h) && isWhitePixel(image, dx, dy)) {
                image.setRGB(dx, dy, new Color(1, 1, 1).getRGB());
            }
        }
    }

    /**
     * Checks if the given coordinates are within the bounds of the image.
     *
     * @param x      the x-coordinate to check
     * @param y      the y-coordinate to check
     * @param width  the width of the image
     * @param height the height of the image
     *               
     * @return true if the position is valid, false otherwise
     */
    private static boolean isValidPosition(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Checks if a pixel at the given coordinates is white.
     * A pixel is considered white if its RGB value is (255, 255, 255).
     *
     * @param image the image to check the pixel in
     * @param x     the x-coordinate of the pixel
     * @param y     the y-coordinate of the pixel
     *              
     * @return true if the pixel is white, false otherwise
     */
    private static boolean isWhitePixel(BufferedImage image, int x, int y) {
        return (image.getRGB(x, y) & 0x00FFFFFF) == 0x00FFFFFF;
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename the filename to extract extension from
     * @return the file extension (without the dot) or empty string if none found
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Checks if a given format is supported for reading or writing.
     *
     * @param format the format to check (e.g., "png", "jpg")
     * @param forReading true to check read support, false for write support
     * @return true if the format is supported, false otherwise
     */
    private static boolean isFormatSupported(String format, boolean forReading) {
        String[] supportedFormats = forReading ? 
            javax.imageio.ImageIO.getReaderFormatNames() : 
            javax.imageio.ImageIO.getWriterFormatNames();
            
        for (String supportedFormat : supportedFormats) {
            if (supportedFormat.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all supported image formats.
     *
     * @param forReading true to get read formats, false for write formats
     * @return array of supported format names
     */
    private static String[] getSupportedFormats(boolean forReading) {
        return forReading ? 
            javax.imageio.ImageIO.getReaderFormatNames() : 
            javax.imageio.ImageIO.getWriterFormatNames();
    }

    /**
     * Saves the processed image to a specified file with the given format.
     * 
     * @param image the image to save
     * @param filename the output filename
     * @param format the output format (e.g., "png", "jpg")
     * @throws IOException if an error occurs during file writing
     */
    private static void saveImage(BufferedImage image, String filename, String format) throws IOException {
        File outputFile = new File(filename);
        
        // Ensure parent directories exist
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        write(image, format, outputFile);
        out.printf("Saved processed image to: %s (format: %s)%n", outputFile.getName(), format.toUpperCase());
    }

    /**
     * Saves the processed image to a file.
     * The output file is named "outputX.png" where X is a number that increments if files already exist.
     *
     * @param image the image to save
     *              
     * @throws IOException if an error occurs during file writing
     */
    private static void saveImage(BufferedImage image) throws IOException {
        int fileNumber = 1;
        File outputFile;

        while ((outputFile = new File("output" + fileNumber + ".png")).exists()) {
            fileNumber++;
        }

        write(image, "png", outputFile);
        out.printf("Saved processed image to: %s%n", outputFile.getName());
    }
}
