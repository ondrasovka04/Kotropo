package cz.kotropo.drawer_items.importVocab;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.googlecode.leptonica.android.Box;
import com.googlecode.leptonica.android.Clip;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.Rotate;
import com.googlecode.leptonica.android.Skew;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OCR {
    private final String language, dataPath;
    private final ArrayList<String> foreign = new ArrayList<>(), czech = new ArrayList<>();
    private Bitmap b, b1, b2;

    public OCR(Bitmap b, String language, String dataPath) {
        this.b = b;
        this.language = language;
        this.dataPath = dataPath;
        editcv();
        startOCR();
    }

    private void editcv() {
        Mat src = new Mat(b.getHeight(), b.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(b, src);
        Mat gray = new Mat();
        Mat result = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, result, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Mat horizontal_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(40, 1));
        Mat remove_horizontal = new Mat();
        for (int i = 0; i < 2; i++) {
            Imgproc.morphologyEx(gray, remove_horizontal, Imgproc.MORPH_OPEN, horizontal_kernel);
        }
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(remove_horizontal, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat vertical_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 40));
        Mat remove_vertical = new Mat();
        for (int i = 0; i < 2; i++) {
            Imgproc.morphologyEx(gray, remove_vertical, Imgproc.MORPH_OPEN, vertical_kernel);
        }
        List<MatOfPoint> vertical_contours = new ArrayList<>();
        Imgproc.findContours(remove_vertical, vertical_contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Scalar color = new Scalar(255, 255, 255);
        Imgproc.drawContours(result, contours, -1, color, 5);
        Imgproc.drawContours(result, vertical_contours, -1, color, 5);
        Utils.matToBitmap(result, b);
        Pix px = ReadFile.readBitmap(b);
        float a = Skew.findSkew(px);
        px = Rotate.rotate(px, a);
        Pix peng = Clip.clipRectangle(px, new Box(40, 0, px.getWidth() / 2 - 40, px.getHeight()));
        Pix pces = Clip.clipRectangle(px, new Box(px.getWidth() / 2 - 30, 0, px.getWidth() / 2, px.getHeight()));
        b = WriteFile.writeBitmap(px);
        b1 = WriteFile.writeBitmap(peng);
        b2 = WriteFile.writeBitmap(pces);
    }

    private void startOCR() {
        TessBaseAPI t = new TessBaseAPI();
        t.init(dataPath, "ces+" + language);
        t.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
        t.setImage(b1);
        String s = t.getUTF8Text();
        t.setImage(b2);
        String s1 = t.getUTF8Text();
        String[] x1 = s.split("\n");
        String[] x2 = s1.split("\n");
        int i = 0;
        while (i < x1.length) {
            if (x1[i].trim().length() > 4) {
                while (Character.isDigit(x1[i].charAt(0))) {
                    x1[i] = x1[i].substring(1);
                }
                foreign.add(x1[i]);
            }
            i++;
        }
        i = 0;

        while (i < x2.length) {
            if (x2[i].trim().length() > 2) {
                while (Character.isDigit(x2[i].charAt(0))) {
                    x2[i] = x2[i].substring(1);
                }
                czech.add(x2[i]);
            }
            i++;
        }
    }

    public ArrayList<String> getForeign() {
        return foreign;
    }

    public ArrayList<String> getCzech() {
        return czech;
    }
}
