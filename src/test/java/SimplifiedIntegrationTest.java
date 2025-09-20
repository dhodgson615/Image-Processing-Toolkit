import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.lang.reflect.Method;

/**
 * Simplified integration tests for the complete image processing workflow
 */
class SimplifiedIntegrationTest {

    @Test
    @DisplayName("Main should handle missing input file gracefully")
    void testMissingInputFile(@TempDir Path tempDir) throws Exception {
        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());
            
            // No input file created - img.png doesn't exist
            assertDoesNotThrow(() -> {
                Main.main(new String[]{});
            }, "Missing input file should be handled gracefully");
            
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    @DisplayName("processImage should work with different configurations")
    void testProcessImageConfigurations() throws Exception {
        Method processImageMethod = Main.class.getDeclaredMethod("processImage", BufferedImage.class, Main.ImageConfig.class);
        processImageMethod.setAccessible(true);
        
        BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        // Create a pattern with different intensities
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                int intensity = (x * 255 / 10);
                testImage.setRGB(x, y, new Color(intensity, intensity, intensity).getRGB());
            }
        }
        
        // Test default configuration
        Main.ImageConfig defaultConfig = new Main.ImageConfig();
        BufferedImage result1 = (BufferedImage) processImageMethod.invoke(null, testImage, defaultConfig);
        assertNotNull(result1, "Default config should produce result");
        assertEquals(10, result1.getWidth(), "Width should be preserved");
        assertEquals(10, result1.getHeight(), "Height should be preserved");
        
        // Test with color inversion
        Main.ImageConfig invertConfig = new Main.ImageConfig();
        invertConfig.invertColors = true;
        BufferedImage result2 = (BufferedImage) processImageMethod.invoke(null, testImage, invertConfig);
        assertNotNull(result2, "Invert config should produce result");
        
        // Test with multiple thresholds
        Main.ImageConfig multiConfig = new Main.ImageConfig();
        multiConfig.useBinaryThreshold = false;
        multiConfig.useMultipleThresholds = true;
        BufferedImage result3 = (BufferedImage) processImageMethod.invoke(null, testImage, multiConfig);
        assertNotNull(result3, "Multiple threshold config should produce result");
    }

    @Test
    @DisplayName("ImageConfig should have correct defaults")
    void testImageConfigDefaults() {
        Main.ImageConfig config = new Main.ImageConfig();
        
        // Test boolean defaults
        assertTrue(config.useBinaryThreshold, "useBinaryThreshold should default to true");
        assertFalse(config.adjustBlackPixelsNeighbors, "adjustBlackPixelsNeighbors should default to false");
        assertFalse(config.useMultipleThresholds, "useMultipleThresholds should default to false");
        assertFalse(config.applyContrast, "applyContrast should default to false");
        assertFalse(config.invertColors, "invertColors should default to false");
        
        // Test double defaults
        assertEquals(0.53, config.binaryThreshold, 0.001, "binaryThreshold should default to 0.53");
        assertEquals(0.9, config.whiteThreshold, 0.001, "whiteThreshold should default to 0.9");
        assertEquals(0.7, config.blackThreshold, 0.001, "blackThreshold should default to 0.7");
        assertEquals(0.0, config.contrastThreshold, 0.001, "contrastThreshold should default to 0.0");
        assertEquals(0.0, config.multiplier, 0.001, "multiplier should default to 0.0");
    }
}