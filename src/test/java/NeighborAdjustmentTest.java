import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.io.File;
import java.nio.file.Path;

/**
 * Test suite for checkAndAdjustNeighbors method
 * Tests neighbor pixel adjustment functionality around black pixels
 */
class NeighborAdjustmentTest {

    private Method checkAndAdjustNeighborsMethod;

    @BeforeEach
    void setUp() throws Exception {
        checkAndAdjustNeighborsMethod = Main.class.getDeclaredMethod("checkAndAdjustNeighbors", BufferedImage.class, int.class, int.class);
        checkAndAdjustNeighborsMethod.setAccessible(true);
    }

    @Test
    @DisplayName("checkAndAdjustNeighbors should adjust all 8 neighbors of center black pixel")
    void testAdjustAllEightNeighbors() throws Exception {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        
        // Fill with white
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        
        // Center is black - this is where we'll call the method
        image.setRGB(1, 1, Color.BLACK.getRGB());
        
        checkAndAdjustNeighborsMethod.invoke(null, image, 1, 1);
        
        // Verify all 8 neighbors are adjusted to RGB(1,1,1)
        int[][] neighbors = {
            {0, 0}, {0, 1}, {0, 2},
            {1, 0},         {1, 2},
            {2, 0}, {2, 1}, {2, 2}
        };
        
        for (int[] neighbor : neighbors) {
            Color color = new Color(image.getRGB(neighbor[0], neighbor[1]));
            assertEquals(1, color.getRed(), "Neighbor red should be 1");
            assertEquals(1, color.getGreen(), "Neighbor green should be 1");
            assertEquals(1, color.getBlue(), "Neighbor blue should be 1");
        }
        
        // Center should remain unchanged
        assertEquals(Color.BLACK.getRGB(), image.getRGB(1, 1), "Center should remain black");
    }

    @Test
    @DisplayName("checkAndAdjustNeighbors should handle edge pixels correctly")
    void testAdjustNeighborsAtEdge() throws Exception {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        
        // Fill with white
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        
        // Top-left corner black pixel
        image.setRGB(0, 0, Color.BLACK.getRGB());
        
        checkAndAdjustNeighborsMethod.invoke(null, image, 0, 0);
        
        // Only valid neighbors should be adjusted (3 neighbors for corner)
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(0, 1), "Bottom neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(1, 0), "Right neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(1, 1), "Diagonal neighbor should be adjusted");
        
        // Pixels out of bounds shouldn't cause errors, and other pixels should remain white
        assertEquals(Color.WHITE.getRGB(), image.getRGB(2, 2), "Distant pixel should remain white");
    }

    @Test
    @DisplayName("checkAndAdjustNeighbors should only adjust white neighbors")
    void testOnlyAdjustWhiteNeighbors() throws Exception {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        
        // Set up mixed colors around center
        image.setRGB(0, 0, Color.WHITE.getRGB());        // Will be adjusted
        image.setRGB(0, 1, Color.BLACK.getRGB());        // Won't be adjusted (already black)
        image.setRGB(0, 2, new Color(128, 128, 128).getRGB()); // Won't be adjusted (not white)
        image.setRGB(1, 0, Color.WHITE.getRGB());        // Will be adjusted
        image.setRGB(1, 1, Color.BLACK.getRGB());        // Center - target pixel
        image.setRGB(1, 2, Color.WHITE.getRGB());        // Will be adjusted
        image.setRGB(2, 0, new Color(200, 200, 200).getRGB()); // Won't be adjusted (not pure white)
        image.setRGB(2, 1, Color.WHITE.getRGB());        // Will be adjusted
        image.setRGB(2, 2, Color.WHITE.getRGB());        // Will be adjusted
        
        checkAndAdjustNeighborsMethod.invoke(null, image, 1, 1);
        
        // Check which pixels were adjusted
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(0, 0), "White neighbor should be adjusted");
        assertEquals(Color.BLACK.getRGB(), image.getRGB(0, 1), "Black neighbor should remain black");
        assertEquals(new Color(128, 128, 128).getRGB(), image.getRGB(0, 2), "Gray neighbor should remain gray");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(1, 0), "White neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(1, 2), "White neighbor should be adjusted");
        assertEquals(new Color(200, 200, 200).getRGB(), image.getRGB(2, 0), "Light gray neighbor should remain unchanged");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(2, 1), "White neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(2, 2), "White neighbor should be adjusted");
    }

    @Test
    @DisplayName("checkAndAdjustNeighbors should handle single pixel image")
    void testSinglePixelImage() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.BLACK.getRGB());
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            checkAndAdjustNeighborsMethod.invoke(null, image, 0, 0);
        }, "Single pixel image should not cause exceptions");
        
        // Pixel should remain unchanged
        assertEquals(Color.BLACK.getRGB(), image.getRGB(0, 0), "Single pixel should remain unchanged");
    }

    @Test
    @DisplayName("checkAndAdjustNeighbors should handle bottom-right corner")
    void testBottomRightCorner() throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        
        // Fill with white
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        
        // Bottom-right corner black pixel
        image.setRGB(3, 3, Color.BLACK.getRGB());
        
        checkAndAdjustNeighborsMethod.invoke(null, image, 3, 3);
        
        // Only valid neighbors should be adjusted (3 neighbors for corner)
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(2, 2), "Top-left diagonal neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(2, 3), "Left neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(3, 2), "Top neighbor should be adjusted");
        
        // Other pixels should remain white
        assertEquals(Color.WHITE.getRGB(), image.getRGB(0, 0), "Distant pixel should remain white");
        assertEquals(Color.WHITE.getRGB(), image.getRGB(1, 1), "Non-adjacent pixel should remain white");
    }

    @Test
    @DisplayName("checkAndAdjustNeighbors should handle middle edge pixels")
    void testMiddleEdgePixel() throws Exception {
        BufferedImage image = new BufferedImage(5, 3, BufferedImage.TYPE_INT_RGB);
        
        // Fill with white
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        
        // Middle of top edge
        image.setRGB(2, 0, Color.BLACK.getRGB());
        
        checkAndAdjustNeighborsMethod.invoke(null, image, 2, 0);
        
        // Should adjust 5 neighbors (left, right, and 3 below)
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(1, 0), "Left neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(3, 0), "Right neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(1, 1), "Bottom-left neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(2, 1), "Bottom neighbor should be adjusted");
        assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(3, 1), "Bottom-right neighbor should be adjusted");
        
        // Distant pixels should remain white
        assertEquals(Color.WHITE.getRGB(), image.getRGB(0, 0), "Far left should remain white");
        assertEquals(Color.WHITE.getRGB(), image.getRGB(4, 0), "Far right should remain white");
        assertEquals(Color.WHITE.getRGB(), image.getRGB(2, 2), "Bottom row should remain white");
    }

    @Test
    @DisplayName("checkAndAdjustNeighbors should use correct neighbor directions")
    void testAllDirections() throws Exception {
        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        
        // Fill with white
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        
        // Center pixel black
        image.setRGB(2, 2, Color.BLACK.getRGB());
        
        checkAndAdjustNeighborsMethod.invoke(null, image, 2, 2);
        
        // Verify all 8 directions using relative positions
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},  // Top row
            {0, -1},           {0, 1},   // Middle row (excluding center)
            {1, -1},  {1, 0},  {1, 1}    // Bottom row
        };
        
        for (int[] dir : directions) {
            int x = 2 + dir[0];
            int y = 2 + dir[1];
            assertEquals(new Color(1, 1, 1).getRGB(), image.getRGB(x, y), 
                        String.format("Neighbor at direction (%d, %d) should be adjusted", dir[0], dir[1]));
        }
        
        // Verify center is unchanged
        assertEquals(Color.BLACK.getRGB(), image.getRGB(2, 2), "Center should remain black");
        
        // Verify non-neighbors are unchanged
        assertEquals(Color.WHITE.getRGB(), image.getRGB(0, 0), "Non-neighbor should remain white");
        assertEquals(Color.WHITE.getRGB(), image.getRGB(4, 4), "Non-neighbor should remain white");
    }
}