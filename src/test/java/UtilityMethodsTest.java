import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

/**
 * Comprehensive test suite for utility methods in Main class
 * Tests calculateMagnitude, isValidPosition, isWhitePixel and other utilities
 */
class UtilityMethodsTest {

    private Method calculateMagnitudeMethod;
    private Method isValidPositionMethod;
    private Method isWhitePixelMethod;

    @BeforeEach
    void setUp() throws Exception {
        // Access private methods using reflection
        calculateMagnitudeMethod = Main.class.getDeclaredMethod("calculateMagnitude", Color.class);
        calculateMagnitudeMethod.setAccessible(true);

        isValidPositionMethod = Main.class.getDeclaredMethod("isValidPosition", int.class, int.class, int.class, int.class);
        isValidPositionMethod.setAccessible(true);

        isWhitePixelMethod = Main.class.getDeclaredMethod("isWhitePixel", BufferedImage.class, int.class, int.class);
        isWhitePixelMethod.setAccessible(true);
    }

    @Test
    @DisplayName("calculateMagnitude should handle pure colors correctly")
    void testCalculateMagnitudePureColors() throws Exception {
        // Test pure black
        Color black = new Color(0, 0, 0);
        double blackMagnitude = (double) calculateMagnitudeMethod.invoke(null, black);
        assertEquals(0.0, blackMagnitude, 0.001, "Black should have magnitude 0");

        // Test pure white
        Color white = new Color(255, 255, 255);
        double whiteMagnitude = (double) calculateMagnitudeMethod.invoke(null, white);
        assertEquals(1.0, whiteMagnitude, 0.001, "White should have magnitude 1");

        // Test pure red
        Color red = new Color(255, 0, 0);
        double redMagnitude = (double) calculateMagnitudeMethod.invoke(null, red);
        double expectedRed = Math.sqrt(255 * 255) / Math.sqrt(3 * 255 * 255);
        assertEquals(expectedRed, redMagnitude, 0.001, "Red magnitude should be calculated correctly");
    }

    @ParameterizedTest
    @CsvSource({
        "128, 128, 128, 0.5",  // Gray should be around 0.5
        "64, 64, 64, 0.25",    // Dark gray
        "192, 192, 192, 0.75"  // Light gray
    })
    @DisplayName("calculateMagnitude should handle gray colors correctly")
    void testCalculateMagnitudeGrayColors(int r, int g, int b, double expected) throws Exception {
        Color color = new Color(r, g, b);
        double magnitude = (double) calculateMagnitudeMethod.invoke(null, color);
        assertEquals(expected, magnitude, 0.01, "Gray color magnitude should be calculated correctly");
    }

    @Test
    @DisplayName("calculateMagnitude should normalize complex colors correctly")
    void testCalculateMagnitudeComplexColors() throws Exception {
        // Test arbitrary color
        Color color = new Color(100, 150, 200);
        double magnitude = (double) calculateMagnitudeMethod.invoke(null, color);
        
        // Calculate expected value manually
        double expected = Math.sqrt(100*100 + 150*150 + 200*200) / Math.sqrt(3 * 255 * 255);
        assertEquals(expected, magnitude, 0.001, "Complex color magnitude should be calculated correctly");
        
        // Magnitude should always be between 0 and 1
        assertTrue(magnitude >= 0.0 && magnitude <= 1.0, "Magnitude should be normalized between 0 and 1");
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0, 10, 10, true",     // Top-left corner
        "9, 9, 10, 10, true",     // Bottom-right corner (valid)
        "5, 5, 10, 10, true",     // Middle
        "-1, 0, 10, 10, false",   // Negative x
        "0, -1, 10, 10, false",   // Negative y
        "10, 0, 10, 10, false",   // x equals width
        "0, 10, 10, 10, false",   // y equals height
        "15, 15, 10, 10, false"   // Both out of bounds
    })
    @DisplayName("isValidPosition should validate coordinates correctly")
    void testIsValidPosition(int x, int y, int width, int height, boolean expected) throws Exception {
        boolean result = (boolean) isValidPositionMethod.invoke(null, x, y, width, height);
        assertEquals(expected, result, 
            String.format("Position (%d, %d) in %dx%d should be %s", x, y, width, height, expected ? "valid" : "invalid"));
    }

    @Test
    @DisplayName("isValidPosition should handle edge cases")
    void testIsValidPositionEdgeCases() throws Exception {
        // Zero dimensions
        assertFalse((boolean) isValidPositionMethod.invoke(null, 0, 0, 0, 0), 
                   "Position in zero-dimension image should be invalid");
        
        // Single pixel image
        assertTrue((boolean) isValidPositionMethod.invoke(null, 0, 0, 1, 1), 
                  "Origin should be valid in 1x1 image");
        assertFalse((boolean) isValidPositionMethod.invoke(null, 1, 0, 1, 1), 
                   "Position (1,0) should be invalid in 1x1 image");
    }

    @Test
    @DisplayName("isWhitePixel should identify white pixels correctly")
    void testIsWhitePixel() throws Exception {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        
        // Set different colors
        image.setRGB(0, 0, Color.WHITE.getRGB());      // Pure white
        image.setRGB(1, 0, Color.BLACK.getRGB());      // Pure black
        image.setRGB(2, 0, new Color(255, 255, 254).getRGB()); // Almost white
        image.setRGB(0, 1, new Color(128, 128, 128).getRGB()); // Gray
        
        // Test white pixel detection
        assertTrue((boolean) isWhitePixelMethod.invoke(null, image, 0, 0), 
                  "Pure white pixel should be detected as white");
        assertFalse((boolean) isWhitePixelMethod.invoke(null, image, 1, 0), 
                   "Black pixel should not be detected as white");
        assertFalse((boolean) isWhitePixelMethod.invoke(null, image, 2, 0), 
                   "Almost white pixel should not be detected as white");
        assertFalse((boolean) isWhitePixelMethod.invoke(null, image, 0, 1), 
                   "Gray pixel should not be detected as white");
    }

    @Test
    @DisplayName("isWhitePixel should use correct RGB masking")
    void testIsWhitePixelMasking() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        
        // Set white pixel with alpha (should still be detected as white due to masking)
        int whiteWithAlpha = 0xFF000000 | 0x00FFFFFF; // Alpha + white RGB
        image.setRGB(0, 0, whiteWithAlpha);
        
        // Set color that would be white if alpha was considered
        int notWhite = 0x00FFFFFF; // No alpha bits set, but RGB is white
        image.setRGB(1, 0, notWhite);
        
        assertTrue((boolean) isWhitePixelMethod.invoke(null, image, 0, 0), 
                  "White pixel with alpha should be detected as white");
        assertTrue((boolean) isWhitePixelMethod.invoke(null, image, 1, 0), 
                  "RGB white without alpha should be detected as white due to masking");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10, 100})
    @DisplayName("calculateMagnitude should handle different color intensities")
    void testCalculateMagnitudeDifferentIntensities(int intensity) throws Exception {
        Color color = new Color(intensity, intensity, intensity);
        double magnitude = (double) calculateMagnitudeMethod.invoke(null, color);
        
        double expected = (double) intensity / 255.0;
        assertEquals(expected, magnitude, 0.001, 
                    "Uniform color intensity should produce proportional magnitude");
    }
}