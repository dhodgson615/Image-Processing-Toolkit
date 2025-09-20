import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Comprehensive test suite for ImageConfig class
 * Tests all configuration parameters and their default values
 */
class ImageConfigTest {

    private Main.ImageConfig config;

    @BeforeEach
    void setUp() {
        config = new Main.ImageConfig();
    }

    @Test
    @DisplayName("ImageConfig should have correct default boolean values")
    void testDefaultBooleanValues() {
        assertTrue(config.useBinaryThreshold, "useBinaryThreshold should default to true");
        assertFalse(config.adjustBlackPixelsNeighbors, "adjustBlackPixelsNeighbors should default to false");
        assertFalse(config.useMultipleThresholds, "useMultipleThresholds should default to false");
        assertFalse(config.applyContrast, "applyContrast should default to false");
        assertFalse(config.invertColors, "invertColors should default to false");
    }

    @Test
    @DisplayName("ImageConfig should have correct default double values")
    void testDefaultDoubleValues() {
        assertEquals(0.53, config.binaryThreshold, 0.001, "binaryThreshold should default to 0.53");
        assertEquals(0.9, config.whiteThreshold, 0.001, "whiteThreshold should default to 0.9");
        assertEquals(0.7, config.blackThreshold, 0.001, "blackThreshold should default to 0.7");
        assertEquals(0.0, config.contrastThreshold, 0.001, "contrastThreshold should default to 0.0");
        assertEquals(0.0, config.multiplier, 0.001, "multiplier should default to 0.0");
    }

    @Test
    @DisplayName("ImageConfig boolean fields should be modifiable")
    void testBooleanFieldsModifiable() {
        config.useBinaryThreshold = false;
        config.adjustBlackPixelsNeighbors = true;
        config.useMultipleThresholds = true;
        config.applyContrast = true;
        config.invertColors = true;

        assertFalse(config.useBinaryThreshold);
        assertTrue(config.adjustBlackPixelsNeighbors);
        assertTrue(config.useMultipleThresholds);
        assertTrue(config.applyContrast);
        assertTrue(config.invertColors);
    }

    @Test
    @DisplayName("ImageConfig double fields should be modifiable")
    void testDoubleFieldsModifiable() {
        config.binaryThreshold = 0.75;
        config.whiteThreshold = 0.85;
        config.blackThreshold = 0.15;
        config.contrastThreshold = 0.5;
        config.multiplier = 1.5;

        assertEquals(0.75, config.binaryThreshold, 0.001);
        assertEquals(0.85, config.whiteThreshold, 0.001);
        assertEquals(0.15, config.blackThreshold, 0.001);
        assertEquals(0.5, config.contrastThreshold, 0.001);
        assertEquals(1.5, config.multiplier, 0.001);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.25, 0.5, 0.75, 1.0})
    @DisplayName("ImageConfig should accept valid threshold ranges")
    void testValidThresholdRanges(double value) {
        config.binaryThreshold = value;
        config.whiteThreshold = value;
        config.blackThreshold = value;
        config.contrastThreshold = value;

        assertEquals(value, config.binaryThreshold, 0.001);
        assertEquals(value, config.whiteThreshold, 0.001);
        assertEquals(value, config.blackThreshold, 0.001);
        assertEquals(value, config.contrastThreshold, 0.001);
    }

    @Test
    @DisplayName("ImageConfig should be a static nested class")
    void testImageConfigIsStaticNestedClass() {
        Class<?> configClass = Main.ImageConfig.class;
        assertTrue(configClass.isMemberClass(), "ImageConfig should be a member class");
        assertTrue(java.lang.reflect.Modifier.isStatic(configClass.getModifiers()), 
                   "ImageConfig should be static");
    }
}