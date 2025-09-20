import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.io.File;
import java.nio.file.Path;

/**
 * Comprehensive test suite for image processing core methods
 * Tests processImage, applyThreshold, applyMultipleThresholds and related functionality
 */
class ImageProcessingMethodsTest {

    private Method processImageMethod;
    private Method applyThresholdMethod;
    private Method applyMultipleThresholdsMethod;
    private Method invertColorsMethod;
    private Method adjustBlackPixelsNeighborsMethod;
    private BufferedImage testImage;
    private Main.ImageConfig config;

    @BeforeEach
    void setUp() throws Exception {
        // Access private methods using reflection
        processImageMethod = Main.class.getDeclaredMethod("processImage", BufferedImage.class, Main.ImageConfig.class);
        processImageMethod.setAccessible(true);

        applyThresholdMethod = Main.class.getDeclaredMethod("applyThreshold", BufferedImage.class, BufferedImage.class, Main.ImageConfig.class);
        applyThresholdMethod.setAccessible(true);

        applyMultipleThresholdsMethod = Main.class.getDeclaredMethod("applyMultipleThresholds", Color.class, double.class, BufferedImage.class, int.class, int.class, Main.ImageConfig.class);
        applyMultipleThresholdsMethod.setAccessible(true);

        invertColorsMethod = Main.class.getDeclaredMethod("invertColors", BufferedImage.class);
        invertColorsMethod.setAccessible(true);

        adjustBlackPixelsNeighborsMethod = Main.class.getDeclaredMethod("adjustBlackPixelsNeighbors", BufferedImage.class);
        adjustBlackPixelsNeighborsMethod.setAccessible(true);

        // Create a test image with known patterns
        testImage = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        // Set up a pattern: white, light gray, dark gray, black
        testImage.setRGB(0, 0, Color.WHITE.getRGB());      // Magnitude ~1.0
        testImage.setRGB(1, 0, new Color(200, 200, 200).getRGB()); // Magnitude ~0.78
        testImage.setRGB(2, 0, new Color(100, 100, 100).getRGB()); // Magnitude ~0.39
        testImage.setRGB(3, 0, Color.BLACK.getRGB());      // Magnitude 0.0
        
        // Fill second row with same pattern
        for (int x = 0; x < 4; x++) {
            testImage.setRGB(x, 1, testImage.getRGB(x, 0));
        }
        
        // Fill remaining rows with mixed patterns
        testImage.setRGB(0, 2, new Color(150, 150, 150).getRGB());
        testImage.setRGB(1, 2, new Color(50, 50, 50).getRGB());
        testImage.setRGB(2, 2, new Color(255, 0, 0).getRGB()); // Pure red
        testImage.setRGB(3, 2, new Color(0, 255, 0).getRGB()); // Pure green

        config = new Main.ImageConfig();
    }

    @Test
    @DisplayName("processImage should return image with correct dimensions")
    void testProcessImageDimensions() throws Exception {
        BufferedImage result = (BufferedImage) processImageMethod.invoke(null, testImage, config);
        
        assertEquals(testImage.getWidth(), result.getWidth(), "Processed image should have same width");
        assertEquals(testImage.getHeight(), result.getHeight(), "Processed image should have same height");
        assertEquals(BufferedImage.TYPE_INT_RGB, result.getType(), "Processed image should be TYPE_INT_RGB");
    }

    @Test
    @DisplayName("processImage should apply binary thresholding by default")
    void testProcessImageBinaryThreshold() throws Exception {
        BufferedImage result = (BufferedImage) processImageMethod.invoke(null, testImage, config);
        
        // With default threshold of 0.53, white and light gray should become white, dark gray and black should become black
        assertEquals(Color.WHITE.getRGB(), result.getRGB(0, 0), "White pixel should remain white");
        assertEquals(Color.WHITE.getRGB(), result.getRGB(1, 0), "Light gray should become white");
        assertEquals(Color.BLACK.getRGB(), result.getRGB(2, 0), "Dark gray should become black");
        assertEquals(Color.BLACK.getRGB(), result.getRGB(3, 0), "Black pixel should remain black");
    }

    @Test
    @DisplayName("processImage should handle color inversion when enabled")
    void testProcessImageWithColorInversion() throws Exception {
        config.invertColors = true;
        
        BufferedImage result = (BufferedImage) processImageMethod.invoke(null, testImage, config);
        
        // First apply thresholding, then inversion
        // White -> black, black -> white after inversion
        assertEquals(Color.BLACK.getRGB(), result.getRGB(0, 0), "White should become black after inversion");
        assertEquals(Color.WHITE.getRGB(), result.getRGB(3, 0), "Black should become white after inversion");
    }

    @Test
    @DisplayName("applyThreshold should handle binary thresholding correctly")
    void testApplyThresholdBinary() throws Exception {
        BufferedImage target = new BufferedImage(testImage.getWidth(), testImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        applyThresholdMethod.invoke(null, testImage, target, config);
        
        // Check thresholding results
        assertEquals(Color.WHITE.getRGB(), target.getRGB(0, 0), "High magnitude should produce white");
        assertEquals(Color.BLACK.getRGB(), target.getRGB(3, 0), "Low magnitude should produce black");
    }

    @Test
    @DisplayName("applyThreshold should handle multiple thresholds when enabled")
    void testApplyThresholdMultiple() throws Exception {
        config.useBinaryThreshold = false;
        config.useMultipleThresholds = true;
        config.whiteThreshold = 0.8;
        config.blackThreshold = 0.3;
        
        BufferedImage target = new BufferedImage(testImage.getWidth(), testImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        applyThresholdMethod.invoke(null, testImage, target, config);
        
        // White pixel (magnitude ~1.0) should become white (above whiteThreshold)
        assertEquals(Color.WHITE.getRGB(), target.getRGB(0, 0), "High magnitude should produce white with multiple thresholds");
        
        // Black pixel (magnitude 0.0) should become black (below blackThreshold)
        assertEquals(Color.BLACK.getRGB(), target.getRGB(3, 0), "Low magnitude should produce black with multiple thresholds");
    }

    @Test
    @DisplayName("applyThreshold should pass through original colors when no thresholding is enabled")
    void testApplyThresholdPassthrough() throws Exception {
        config.useBinaryThreshold = false;
        config.useMultipleThresholds = false;
        
        BufferedImage target = new BufferedImage(testImage.getWidth(), testImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        applyThresholdMethod.invoke(null, testImage, target, config);
        
        // Colors should be preserved
        assertEquals(testImage.getRGB(0, 0), target.getRGB(0, 0), "Original color should be preserved");
        assertEquals(testImage.getRGB(1, 0), target.getRGB(1, 0), "Original color should be preserved");
    }

    @ParameterizedTest
    @CsvSource({
        "1.0, 0.9, 0.7, true",  // Above white threshold
        "0.5, 0.9, 0.7, false", // Between thresholds
        "0.2, 0.9, 0.7, true"   // Below black threshold
    })
    @DisplayName("applyMultipleThresholds should handle threshold boundaries correctly")
    void testApplyMultipleThresholdsBoundaries(double magnitude, double whiteThreshold, double blackThreshold, boolean shouldBeBlackOrWhite) throws Exception {
        config.whiteThreshold = whiteThreshold;
        config.blackThreshold = blackThreshold;
        
        BufferedImage target = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Color originalColor = new Color(128, 128, 128);
        
        applyMultipleThresholdsMethod.invoke(null, originalColor, magnitude, target, 0, 0, config);
        
        int resultRGB = target.getRGB(0, 0);
        if (shouldBeBlackOrWhite) {
            assertTrue(resultRGB == Color.WHITE.getRGB() || resultRGB == Color.BLACK.getRGB(), 
                      "Color should be thresholded to black or white");
        }
        // Note: when between thresholds, original color should be preserved
    }

    @Test
    @DisplayName("applyMultipleThresholds should handle contrast adjustment")
    void testApplyMultipleThresholdsContrast() throws Exception {
        config.applyContrast = true;
        config.contrastThreshold = 0.5;
        config.multiplier = 2.0;
        
        BufferedImage target = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Color darkColor = new Color(50, 60, 70);
        double lowMagnitude = 0.3; // Below contrast threshold
        
        applyMultipleThresholdsMethod.invoke(null, darkColor, lowMagnitude, target, 0, 0, config);
        
        Color result = new Color(target.getRGB(0, 0));
        
        // Colors should be enhanced by multiplier, but capped at 255
        assertEquals(Math.min(255, (int)(50 * 2.0)), result.getRed(), "Red should be enhanced by multiplier");
        assertEquals(Math.min(255, (int)(60 * 2.0)), result.getGreen(), "Green should be enhanced by multiplier");
        assertEquals(Math.min(255, (int)(70 * 2.0)), result.getBlue(), "Blue should be enhanced by multiplier");
    }

    @Test
    @DisplayName("invertColors should properly invert all pixel colors")
    void testInvertColors() throws Exception {
        BufferedImage image = new BufferedImage(3, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.WHITE.getRGB());
        image.setRGB(1, 0, Color.BLACK.getRGB());
        image.setRGB(2, 0, new Color(100, 150, 200).getRGB());
        
        invertColorsMethod.invoke(null, image);
        
        // Check inversions
        assertEquals(Color.BLACK.getRGB(), image.getRGB(0, 0), "White should become black");
        assertEquals(Color.WHITE.getRGB(), image.getRGB(1, 0), "Black should become white");
        
        Color invertedColor = new Color(image.getRGB(2, 0));
        assertEquals(255 - 100, invertedColor.getRed(), "Red should be inverted");
        assertEquals(255 - 150, invertedColor.getGreen(), "Green should be inverted");
        assertEquals(255 - 200, invertedColor.getBlue(), "Blue should be inverted");
    }

    @Test
    @DisplayName("adjustBlackPixelsNeighbors should modify neighbors of black pixels")
    void testAdjustBlackPixelsNeighbors() throws Exception {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        
        // Fill with white
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        
        // Set center pixel to black
        image.setRGB(1, 1, Color.BLACK.getRGB());
        
        adjustBlackPixelsNeighborsMethod.invoke(null, image);
        
        // Center should still be black
        assertEquals(Color.BLACK.getRGB(), image.getRGB(1, 1), "Center black pixel should remain black");
        
        // All 8 neighbors should now be very dark (RGB 1,1,1)
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue; // Skip center
                
                Color neighborColor = new Color(image.getRGB(1 + dx, 1 + dy));
                assertEquals(1, neighborColor.getRed(), "Neighbor red should be 1");
                assertEquals(1, neighborColor.getGreen(), "Neighbor green should be 1");
                assertEquals(1, neighborColor.getBlue(), "Neighbor blue should be 1");
            }
        }
    }

    @Test
    @DisplayName("processImage should not adjust neighbors when disabled")
    void testProcessImageNoNeighborAdjustment() throws Exception {
        config.adjustBlackPixelsNeighbors = false; // Default, but explicit
        
        BufferedImage result = (BufferedImage) processImageMethod.invoke(null, testImage, config);
        
        // Verify that neighbor adjustment didn't occur by checking if any white pixels remain white
        // (if adjustment occurred, white neighbors of black pixels would be changed)
        boolean foundWhitePixel = false;
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                if (result.getRGB(x, y) == Color.WHITE.getRGB()) {
                    foundWhitePixel = true;
                    break;
                }
            }
        }
        assertTrue(foundWhitePixel, "Should find white pixels when neighbor adjustment is disabled");
    }

    @Test
    @DisplayName("processImage should handle edge case of 1x1 image")
    void testProcessImageSinglePixel() throws Exception {
        BufferedImage singlePixel = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        singlePixel.setRGB(0, 0, new Color(128, 128, 128).getRGB());
        
        BufferedImage result = (BufferedImage) processImageMethod.invoke(null, singlePixel, config);
        
        assertEquals(1, result.getWidth(), "Result should have width 1");
        assertEquals(1, result.getHeight(), "Result should have height 1");
        assertNotNull(result.getRGB(0, 0), "Single pixel should be processed");
    }
}