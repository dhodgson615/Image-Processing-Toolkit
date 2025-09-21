import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

/**
 * Test suite for ImageProcessingGUI functionality.
 * Tests are designed to work in both headless and full GUI environments.
 */
class ImageProcessingGUITest {

    @BeforeEach
    void setUp() {
        // Skip GUI tests in headless environment
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), 
            "GUI tests require a display environment");
    }

    @Test
    @DisplayName("ImageProcessingGUI should be instantiable with default configuration")
    void testGUIInstantiation() {
        assertDoesNotThrow(() -> {
            ImageProcessingGUI gui = new ImageProcessingGUI();
            assertNotNull(gui, "GUI should be instantiable");
        }, "GUI instantiation should not throw exceptions");
    }

    @Test
    @DisplayName("ImageProcessingGUI should have proper main method")
    void testMainMethod() throws Exception {
        Method mainMethod = ImageProcessingGUI.class.getMethod("main", String[].class);
        assertNotNull(mainMethod, "Main method should exist");
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()), 
            "Main method should be static");
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()), 
            "Main method should be public");
    }

    @Test
    @DisplayName("scaleImageForDisplay method should work correctly")
    void testScaleImageForDisplay() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // Skip this test in headless environment
        }
        
        // Create a test GUI instance
        ImageProcessingGUI gui = new ImageProcessingGUI();
        
        // Access the private scaleImageForDisplay method using reflection
        Method scaleMethod = ImageProcessingGUI.class.getDeclaredMethod("scaleImageForDisplay", BufferedImage.class);
        scaleMethod.setAccessible(true);
        
        // Create a test image
        BufferedImage testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        
        // Test scaling
        BufferedImage scaledImage = (BufferedImage) scaleMethod.invoke(gui, testImage);
        assertNotNull(scaledImage, "Scaled image should not be null");
        
        // Check that large images are scaled down
        assertTrue(scaledImage.getWidth() <= 400 || scaledImage.getHeight() <= 400, 
            "Large images should be scaled down to fit display size");
    }

    @Test
    @DisplayName("ImageProcessingGUI should handle CLI integration correctly")
    void testCLIIntegration() {
        // Test that Main class can handle GUI launch request
        assertDoesNotThrow(() -> {
            String[] args = {"--gui"};
            // This should not throw an exception even in headless mode
            // The actual GUI creation happens in SwingUtilities.invokeLater
            Main.main(args);
        }, "Main should handle --gui argument gracefully");
    }
}