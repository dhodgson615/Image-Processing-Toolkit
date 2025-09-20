import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;

/**
 * Test suite for multi-format image support functionality
 * Tests format detection, conversion, and error handling
 */
class MultiFormatTest {
    
    private Method getFileExtensionMethod;
    private Method isFormatSupportedMethod;
    private Method getSupportedFormatsMethod;
    private Method saveImageWithFormatMethod;
    
    @BeforeEach
    void setUp() throws Exception {
        // Access private methods using reflection
        getFileExtensionMethod = Main.class.getDeclaredMethod("getFileExtension", String.class);
        getFileExtensionMethod.setAccessible(true);
        
        isFormatSupportedMethod = Main.class.getDeclaredMethod("isFormatSupported", String.class, boolean.class);
        isFormatSupportedMethod.setAccessible(true);
        
        getSupportedFormatsMethod = Main.class.getDeclaredMethod("getSupportedFormats", boolean.class);
        getSupportedFormatsMethod.setAccessible(true);
        
        saveImageWithFormatMethod = Main.class.getDeclaredMethod("saveImage", BufferedImage.class, String.class, String.class);
        saveImageWithFormatMethod.setAccessible(true);
    }
    
    @Test
    @DisplayName("getFileExtension should extract file extensions correctly")
    void testGetFileExtension() throws Exception {
        assertEquals("png", getFileExtensionMethod.invoke(null, "test.png"));
        assertEquals("jpg", getFileExtensionMethod.invoke(null, "image.jpg"));
        assertEquals("jpeg", getFileExtensionMethod.invoke(null, "photo.JPEG"));
        assertEquals("bmp", getFileExtensionMethod.invoke(null, "file.BMP"));
        assertEquals("", getFileExtensionMethod.invoke(null, "noextension"));
        assertEquals("", getFileExtensionMethod.invoke(null, ""));
        assertEquals("gif", getFileExtensionMethod.invoke(null, "path/to/image.gif"));
        assertEquals("png", getFileExtensionMethod.invoke(null, "file.with.dots.png"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
    @DisplayName("isFormatSupported should recognize common formats for reading")
    void testFormatSupportedForReading(String format) throws Exception {
        assertTrue((boolean) isFormatSupportedMethod.invoke(null, format, true),
                  "Format " + format + " should be supported for reading");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
    @DisplayName("isFormatSupported should recognize common formats for writing")
    void testFormatSupportedForWriting(String format) throws Exception {
        assertTrue((boolean) isFormatSupportedMethod.invoke(null, format, false),
                  "Format " + format + " should be supported for writing");
    }
    
    @Test
    @DisplayName("isFormatSupported should reject unsupported formats")
    void testUnsupportedFormats() throws Exception {
        assertFalse((boolean) isFormatSupportedMethod.invoke(null, "xyz", true));
        assertFalse((boolean) isFormatSupportedMethod.invoke(null, "abc", false));
        assertFalse((boolean) isFormatSupportedMethod.invoke(null, "", true));
    }
    
    @Test
    @DisplayName("getSupportedFormats should return non-empty arrays")
    void testGetSupportedFormats() throws Exception {
        String[] readFormats = (String[]) getSupportedFormatsMethod.invoke(null, true);
        String[] writeFormats = (String[]) getSupportedFormatsMethod.invoke(null, false);
        
        assertTrue(readFormats.length > 0, "Should have supported read formats");
        assertTrue(writeFormats.length > 0, "Should have supported write formats");
        
        // Verify some common formats are present
        boolean hasPng = false, hasJpg = false;
        for (String format : readFormats) {
            if (format.equalsIgnoreCase("png")) hasPng = true;
            if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) hasJpg = true;
        }
        assertTrue(hasPng, "PNG should be supported for reading");
        assertTrue(hasJpg, "JPG/JPEG should be supported for reading");
    }
    
    @Test
    @DisplayName("saveImage with format should create files in correct format")
    void testSaveImageWithFormat(@TempDir Path tempDir) throws Exception {
        BufferedImage testImage = createTestImage();
        
        File pngFile = tempDir.resolve("test.png").toFile();
        File jpgFile = tempDir.resolve("test.jpg").toFile();
        
        // Save in different formats
        saveImageWithFormatMethod.invoke(null, testImage, pngFile.getAbsolutePath(), "png");
        saveImageWithFormatMethod.invoke(null, testImage, jpgFile.getAbsolutePath(), "jpg");
        
        assertTrue(pngFile.exists(), "PNG file should be created");
        assertTrue(jpgFile.exists(), "JPG file should be created");
        
        // Verify they can be read back
        BufferedImage loadedPng = ImageIO.read(pngFile);
        BufferedImage loadedJpg = ImageIO.read(jpgFile);
        
        assertNotNull(loadedPng, "PNG file should be readable");
        assertNotNull(loadedJpg, "JPG file should be readable");
        assertEquals(testImage.getWidth(), loadedPng.getWidth());
        assertEquals(testImage.getHeight(), loadedPng.getHeight());
    }
    
    @Test
    @DisplayName("Main with valid arguments should process and convert formats")
    void testMainWithValidArguments(@TempDir Path tempDir) throws Exception {
        // Create input test image
        BufferedImage inputImage = createTestImage();
        File inputFile = tempDir.resolve("input.png").toFile();
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        ImageIO.write(inputImage, "png", inputFile);
        
        // Capture output
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outStream));
        
        try {
            String[] args = {inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), "jpg"};
            Main.main(args);
            
            assertTrue(outputFile.exists(), "Output file should be created");
            BufferedImage outputImage = ImageIO.read(outputFile);
            assertNotNull(outputImage, "Output image should be readable");
            
            String output = outStream.toString();
            assertTrue(output.contains("JPG"), "Output should mention JPG format");
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    @DisplayName("Main with missing input file should show error")
    void testMainWithMissingFile(@TempDir Path tempDir) throws Exception {
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));
        
        try {
            String[] args = {tempDir.resolve("nonexistent.png").toString()};
            Main.main(args);
            
            String error = errStream.toString();
            assertTrue(error.contains("not found"), "Should show file not found error");
        } finally {
            System.setErr(originalErr);
        }
    }
    
    @Test
    @DisplayName("Main with unsupported format should show error")
    void testMainWithUnsupportedFormat(@TempDir Path tempDir) throws Exception {
        // Create input test image
        BufferedImage inputImage = createTestImage();
        File inputFile = tempDir.resolve("input.png").toFile();
        ImageIO.write(inputImage, "png", inputFile);
        
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));
        
        try {
            String[] args = {inputFile.getAbsolutePath(), "output.xyz", "xyz"};
            Main.main(args);
            
            String error = errStream.toString();
            assertTrue(error.contains("not supported"), "Should show format not supported error");
            assertTrue(error.contains("Supported formats"), "Should list supported formats");
        } finally {
            System.setErr(originalErr);
        }
    }
    
    private BufferedImage createTestImage() {
        BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                int intensity = (x + y) * 255 / 100;
                if (intensity > 255) intensity = 255;
                image.setRGB(x, y, new Color(intensity, intensity, intensity).getRGB());
            }
        }
        return image;
    }
}