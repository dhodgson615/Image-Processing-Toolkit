# Image Processing Toolkit

This is an image processing application written in Java. It provides various functionalities to process and modify images through thresholding, contrast adjustment, and color inversion.

## Features

- **Binary Thresholding**: Converts the image to black and white based on a threshold value.
- **Multiple Thresholds**: Applies multiple thresholds for enhanced image processing.
- **Contrast Adjustment**: Adjusts the contrast of the image below a specified threshold.
- **Color Inversion**: Inverts the colors of the image.
- **Neighbor Adjustment**: Modifies neighboring pixels of black pixels based on specific conditions.

## How It Works

1. The application reads an input image (`img.png`) from the working directory.
2. It processes the image according to the specified configuration in the `ImageConfig` class.
3. The processed image is saved as `outputX.png` (e.g., `output1.png`, `output2.png`), ensuring no overwriting of existing files.

## Configuration

The application uses a nested static class `ImageConfig` to configure the processing parameters. Below are the available options:

- `useBinaryThreshold` (default: `true`): Enables or disables binary thresholding.
- `adjustBlackPixelsNeighbors` (default: `false`): Adjusts neighboring pixels of black pixels if enabled.
- `useMultipleThresholds` (default: `false`): Enables or disables multiple threshold processing.
- `applyContrast` (default: `false`): Enables or disables contrast adjustment.
- `invertColors` (default: `false`): Enables or disables color inversion.
- `binaryThreshold` (default: `0.53`): The threshold value for binary thresholding.
- `whiteThreshold` (default: `0.9`): The threshold for identifying white pixels in multiple thresholding.
- `blackThreshold` (default: `0.7`): The threshold for identifying black pixels in multiple thresholding.
- `contrastThreshold` (default: `0.0`): The contrast adjustment threshold.
- `multiplier` (default: `0.0`): The multiplier used for contrast adjustment.

## Methods Overview

### Main Methods

- **`main(String[] args)`**: Entry point of the application. Reads the input image, processes it, and saves the output image.

### Image Processing

- **`processImage(BufferedImage image, ImageConfig config)`**: Processes the input image using the specified configuration.
- **`applyThreshold(BufferedImage source, BufferedImage target, ImageConfig config)`**: Applies binary or multiple thresholds to the image.
- **`invertColors(BufferedImage image)`**: Inverts the colors of the input image.
- **`adjustBlackPixelsNeighbors(BufferedImage image)`**: Adjusts neighboring pixels of black pixels in the image.

### Utility Methods

- **`calculateMagnitude(Color color)`**: Calculates the normalized magnitude of a color based on its RGB components.
- **`applyMultipleThresholds(Color pixelColor, double magnitude, BufferedImage target, int x, int y, ImageConfig config)`**: Applies multiple thresholds to a pixel and modifies the target image.
- **`checkAndAdjustNeighbors(BufferedImage image, int x, int y)`**: Checks the neighbors of a black pixel and adjusts them if they are white.
- **`isValidPosition(int x, int y, int width, int height)`**: Validates the coordinates of a pixel.
- **`isWhitePixel(BufferedImage image, int x, int y)`**: Checks if a pixel is white.
- **`saveImage(BufferedImage image)`**: Saves the processed image to a file.