package net.ryswick.bender.imaging;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.awt.image.BufferedImage;

// Singleton
public class Tess {
    private static Tess tess;
    private static final Tesseract tesseract;

    static {
        tess = new Tess();
        tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        tesseract.setTessVariable("user_defined_dpi", "70");
        tesseract.setVariable("tessedit_char_whitelist", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ");
    }

    public static Tess getInstance() {
        return tess;
    }

    public void setTesseractVar(String key, String value) {
        tesseract.setTessVariable(key, value);
    }

    public String readSingleLine(BufferedImage input) {
        // Tess.getInstance().setTesseractVar("psm", "3");
        String ret = this.readImage(input);
        // Tess.getInstance().setTesseractVar("psm", "8");
        return ret.toLowerCase();
    }

    // public String captureAndReadSingleLine(Position position) {
    // // 3 is fully automatic segmentation. It just works better than most other
    // // things??
    // // 7 is meant to be single line.
    // Tess.getInstance().setTesseractVar("psm", "3");
    // return readImage(Imaging.imageScreen(position, true, false)).toLowerCase();
    // }

    public String readImage(BufferedImage input) {
        try {
            return tesseract.doOCR(input).strip().toLowerCase();
        } catch (TesseractException e) {
            e.printStackTrace();
            return null;
        }
    }
}
