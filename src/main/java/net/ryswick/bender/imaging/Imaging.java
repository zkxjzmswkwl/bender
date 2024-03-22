package net.ryswick.bender.imaging;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Imaging {
    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            System.err.println("Robot boom~");
        }
    }

    public static BufferedImage imageScreen(Position position, String fileName) {
        // This is fucking retarded. (Position object not necessary currently. Want to use it for additional shit later.)
        // but for now it's retarded.
        BufferedImage capture = Imaging.robot.createScreenCapture(position.toRectangle());
        Imaging.writeBufferedImageToDisk(capture, fileName + ".jpg");
        return capture;
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
}
