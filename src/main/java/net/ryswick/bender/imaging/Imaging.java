package net.ryswick.bender.imaging;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Imaging {
    // TODO: Why the fuck is this here LOL
    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            System.err.println("Robot boom~");
        }
    }
    // -

    public static BufferedImage imageScreen(Position position, String fileName) {
        // This is fucking retarded. (Position object not necessary currently. Want to
        // use it for additional shit later.)
        // but for now it's retarded.
        BufferedImage capture = Imaging.robot.createScreenCapture(position.toRectangle());
        Imaging.writeBufferedImageToDisk(capture, fileName + ".jpg");
        return capture;
    }

    public static BufferedImage processTest(BufferedImage image, int r, int g, int b, int range) {
        Mat flippedBuffer = convertToMat(image);
        // Instantiate Size object with defined values from whichever Position object
        // we're working with
        Size size = new Size(image.getWidth(), image.getHeight());
        Imgproc.resize(flippedBuffer, flippedBuffer, size, 2, 2, Imgproc.INTER_AREA);
        Imgproc.cvtColor(flippedBuffer, flippedBuffer, Imgproc.COLOR_BGR2GRAY);
        BufferedImage out = convertToBufferedImage(flippedBuffer);
        out = Imaging.customThreshold(out, range, r, g, b);
        Imaging.writeBufferedImageToDisk(out, "dongs.jpg");
        return out;
    }

    public BufferedImage process(boolean useGray, boolean shouldThreshold) {
        // Not yet implemented.
        return null;
    }

    private static BufferedImage convertToBufferedImage(Mat matrix) {
        try {
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", matrix, mob);
            return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Mat convertToMat(BufferedImage image) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writeBufferedImageToDisk(BufferedImage image, String path) {
        try {
            ImageIO.write(image, "jpg", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean inColorRange(Color color, int range, int r, int g, int b) {
        if (color.getRed() >= r - range &&
                color.getRed() <= r + range &&
                color.getGreen() >= g - range &&
                color.getGreen() <= g + range &&
                color.getBlue() >= b - range &&
                color.getBlue() <= b + range) {
            return true;
        }
        return false;
    }

    /**
     * Black out all pixels in image that do not match the given r,g,b values.
     *
     * @param image
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static BufferedImage customThreshold(BufferedImage image, int range, int r, int g, int b) {
        for (int x = 0; x <= image.getWidth() - 1; x++) {
            for (int y = 0; y <= image.getHeight() - 1; y++) {
                int pixel = image.getRGB(x, y);
                Color color = new Color(pixel, true);

                // If the current pixel matches, set it to black.
                // If not, set it to green. This produces **great** ocr results.
                if (inColorRange(color, range, r, g, b))
                    image.setRGB(x, y, 0x000000);
                else
                    image.setRGB(x, y, 0x00FF00);

            }
        }
        return image;
    }
}
