import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// Abstract base class
abstract class Converter {
    // Reads image from specified file path
    protected BufferedImage readImage(String fileName) throws IOException {
        return ImageIO.read(new File(fileName));
    }
    
    // Writes processed image to a file as PNG format
    protected void writeImage(BufferedImage image, String fileName) throws IOException {
        ImageIO.write(image, "PNG", new File(fileName));
    }

    // Abstract method to be implemented by all derived converter classes
    public abstract void convert(String inputFileName, String outputFileName) throws IOException;
}

// Grayscale converter
class Grayscale extends Converter {
    @Override
    public void convert(String inputFileName, String outputFileName) throws IOException {
        BufferedImage image = readImage(inputFileName);
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff; //isolates for alpha channel bits
                int red = (pixel >> 16) & 0xff; //isolates for red channel bits
                int green = (pixel >> 8) & 0xff; //isolates for green channel bits
                int blue = pixel & 0xff; //isolates for blue channel bits
                int gray = (red + green + blue) / 3; 
                int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray; //combines intensity of rgb colours with greyscale intensity
                image.setRGB(x, y, newPixel); //sets new pixel in place
            }
        }
        writeImage(image, outputFileName);
    }
}

// Rotate Converter (90 degrees clockwise)
class Rotate extends Converter {
    @Override
    public void convert(String inputFileName, String outputFileName) throws IOException {
        BufferedImage image = readImage(inputFileName);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB); //creates new instance of blank img to set pixels on

        //iterates through pixels of image and replaces pixel at new rotated coordinates
        for (int y = 0; y < height; y++) { //row
            for (int x = 0; x < width; x++) { //collums
                rotatedImage.setRGB(height - y - 1, x, image.getRGB(x, y)); //height -y-1 flips row order, row and collums swap
            }
        }
        writeImage(rotatedImage, outputFileName);
    }
}

// Blur Converter
class Blur extends Converter {
    @Override
    public void convert(String inputFileName, String outputFileName) throws IOException {
        BufferedImage image = readImage(inputFileName);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage blurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //horizontal and vertical offsets
        int[] dx = {-1, 0, 1, -1, 0, 1, -1, 0, 1}; 
        int[] dy = {-1, -1, -1, 0, 0, 0, 1, 1, 1};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int r = 0, g = 0, b = 0, a = 0;
                for (int i = 0; i < 9; i++) {
                    int nx = x + dx[i]; //new pixel x coord (uses offset of surrounding neighbour pixels)
                    int ny = y + dy[i];
                    int pixel = image.getRGB(nx, ny);
                    a += (pixel >> 24) & 0xff;
                    r += (pixel >> 16) & 0xff;
                    g += (pixel >> 8) & 0xff;
                    b += pixel & 0xff;
                }

                //takes all pixels and divides by # of pixels to get avg argb intesity of group
                a /= 9;
                r /= 9;
                g /= 9;
                b /= 9;
                int newPixel = (a << 24) | (r << 16) | (g << 8) | b;
                blurredImage.setRGB(x, y, newPixel);
            }
        }
        writeImage(blurredImage, outputFileName);
    }
}

// Cartoon Effect Converter
class CartoonEffect extends Converter {
    @Override
    public void convert(String inputFileName, String outputFileName) throws IOException {
        BufferedImage image = readImage(inputFileName);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage cartoonImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Loop through each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                // Reduce color palette (quantization)
                red = (red / 64) * 64;
                green = (green / 64) * 64;
                blue = (blue / 64) * 64;

                // Edge enhancement using a simple threshold
                int edgePixel = (x > 0 && y > 0) ? image.getRGB(x - 1, y - 1) : pixel;
                int edgeRed = Math.abs(((edgePixel >> 16) & 0xff) - red);
                int edgeGreen = Math.abs(((edgePixel >> 8) & 0xff) - green);
                int edgeBlue = Math.abs((edgePixel & 0xff) - blue);
                int edgeIntensity = Math.max(edgeRed, Math.max(edgeGreen, edgeBlue));

                // Apply edge threshold
                if (edgeIntensity > 50) {
                    red = green = blue = 0; // Black edges
                }

                int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                cartoonImage.setRGB(x, y, newPixel);
            }
        }

        writeImage(cartoonImage, outputFileName);
    }
}


// Improved Swirl Converter
class Swirl extends Converter {
    @Override
    public void convert(String inputFileName, String outputFileName) throws IOException {
        BufferedImage image = readImage(inputFileName);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage swirledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //find middle of img
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double swirlStrength = Math.PI / 4;  // Swirl intensity

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double dx = x - centerX; //distance away from center points in x direction
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy); //how far pixel is away from center (pythagorean) 

                // Swirl effect: angle increases with distance
                double angle = swirlStrength * Math.sin(distance / Math.max(width, height) * Math.PI);

                // Calculate new coordinates with swirl transformation
                int srcX = (int) (Math.cos(angle) * dx - Math.sin(angle) * dy + centerX);
                int srcY = (int) (Math.sin(angle) * dx + Math.cos(angle) * dy + centerY);

                // Check bounds and set the pixel color
                if (srcX >= 0 && srcX < width && srcY >= 0 && srcY < height) {
                    swirledImage.setRGB(x, y, image.getRGB(srcX, srcY));
                } else {
                    swirledImage.setRGB(x, y, 0);  // Transparent for out-of-bounds
                }
            }
        }

        writeImage(swirledImage, outputFileName);
    }
}

// Edge Detection Converter (Sobel Filter)
class EdgeDetection extends Converter {
    @Override
    public void convert(String inputFileName, String outputFileName) throws IOException {
        BufferedImage image = readImage(inputFileName);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //Detects x and y changes, finds edges in respective direction
        int[][] sobelX = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };
        int[][] sobelY = {
            {-1, -2, -1},
            {0, 0, 0},
            {1, 2, 1}
        };

        //Loop through pixels, excluding borders to avoid oob errors
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = 0, gy = 0;
                
                //Convolution with Sobel kernels (3x3 neighborhood)
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        //Get the grayscale value of the neighboring pixel
                        int pixel = image.getRGB(x + j, y + i) & 0xff;
                        // Calculate gradients in both directions using matrices from earlier
                        gx += pixel * sobelX[i + 1][j + 1];
                        gy += pixel * sobelY[i + 1][j + 1];
                    }
                }
                //calculates combined intesity of both directions
                int magnitude = Math.min(255, (int) Math.sqrt(gx * gx + gy * gy));
                int newPixel = (255 << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
                edgeImage.setRGB(x, y, newPixel);
            }
        }

        writeImage(edgeImage, outputFileName);
    }
}

// Pixelate Converter
class Pixelate extends Converter {
    @Override
    public void convert(String inputFileName, String outputFileName) throws IOException {
        BufferedImage image = readImage(inputFileName);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage pixelatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //sets blocksize to 10x10 for pixelation
        int blockSize = 10;

        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                int r = 0, g = 0, b = 0, a = 0; //initialize argb values to be stored
                int count = 0; //to keep track of #pixels in block

                //Calculate the average color within the current block
                for (int dy = 0; dy < blockSize && y + dy < height; dy++) { //blcok rows
                    for (int dx = 0; dx < blockSize && x + dx < width; dx++) {//block collums
                        //get values from orig img
                        int pixel = image.getRGB(x + dx, y + dy);

                        //extracts argb components of pixel
                        a += (pixel >> 24) & 0xff;
                        r += (pixel >> 16) & 0xff;
                        g += (pixel >> 8) & 0xff;
                        b += pixel & 0xff;
                        count++; 
                    }
                }

                //avg colour
                a /= count;
                r /= count;
                g /= count;
                b /= count;
                int avgPixel = (a << 24) | (r << 16) | (g << 8) | b;

                //set all pixels of block to avg colour
                for (int dy = 0; dy < blockSize && y + dy < height; dy++) {
                    for (int dx = 0; dx < blockSize && x + dx < width; dx++) {
                        pixelatedImage.setRGB(x + dx, y + dy, avgPixel);
                    }
                }
            }
        }

        writeImage(pixelatedImage, outputFileName);
    }
}

// Mirror Converter
class Mirror extends Converter {
    @Override
    public void convert(String inputFileName, String outputFileName) throws IOException {
        BufferedImage image = readImage(inputFileName);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage mirroredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Mirror horizontally
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                // Calculate the mirrored position (reversed x-coordinate)                
                mirroredImage.setRGB(width - 1 - x, y, pixel); // Flip horizontally
            }
        }
        
        writeImage(mirroredImage, outputFileName);
    }
}
