package extractcores;

import static extractcores.CoreExtractor.getMatFromBufferedImage;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class OpenCVSampleCopy {
  public static LinkedList<Integer> decodeBufferedImage(BufferedImage image)
  {
    int cvType = 0;
    int bufferedImageType = image.getType();
    if (bufferedImageType == BufferedImage.TYPE_INT_BGR || bufferedImageType == BufferedImage.TYPE_3BYTE_BGR)
    {
      cvType = Imgproc.COLOR_BGR2HSV;
    }
    else if (bufferedImageType == BufferedImage.TYPE_INT_RGB)
    {
      cvType = Imgproc.COLOR_RGB2HSV;
    }
    else if (bufferedImageType == BufferedImage.TYPE_INT_ARGB)
    {
      throw new RuntimeException("Don't like ARGB");
    }
    else
    {
      throw new RuntimeException("Unexpected image type: " + bufferedImageType);
    }

    // Convert the image to a mat
    Mat mat = getMatFromBufferedImage(image);

    // Process the mat
    Imgproc.cvtColor(mat, mat, cvType);

    // Get V from HSV
    ArrayList<Mat> list = new ArrayList<Mat>();
    org.opencv.core.Core.split(mat, list);
    Mat vMat = null;
    if (list.size() == 3)
    {
      vMat = list.get(2);
    }
    else
    {
      throw new RuntimeException("Image does not split to 3 channels!");
    }

    // Adaptive Thresholding
    Imgproc.adaptiveThreshold(vMat, vMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 10); // 5,
    // 25

    // Erosion & Dilation
//    vMat = CVUtils.invertMat(vMat);
    Mat kernal = new Mat();
    org.opencv.core.Point p = new org.opencv.core.Point(-1, -1);

    LinkedList<MatOfPoint> contours = new LinkedList<MatOfPoint>();
    LinkedList<MatOfPoint> finalListOfContours = new LinkedList<MatOfPoint>();
    for (int i = 0; finalListOfContours.size() == 0; i++)
    {
      Mat dst = vMat.clone();
      contours.clear();
      Imgproc.dilate(vMat, dst, kernal, p, 1);
      Imgproc.erode(dst, vMat, kernal, p, 1);
      Imgproc.dilate(vMat, dst, kernal, p, 3 * (i + 1));
      Imgproc.erode(dst, vMat, kernal, p, 3 * (i + 1));
//      Utils.out.println("i:" + i);

      // Get contours
      Mat hierarchy = new Mat();
      Imgproc.findContours(vMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

      // Filter out bad contours
      finalListOfContours.clear();
      for (int j = 0; j < contours.size(); j++)
      {
        if (Imgproc.contourArea(contours.get(j)) > (vMat.rows() * vMat.cols() * 0.005))
        {// 0.01 is default
          finalListOfContours.add(contours.get(j));
        }
      }
    }

    contours = null;

//    LinkedList<DMImage> dmImageList = new LinkedList<DMImage>();
//    DMImageFactory factory = new DMImageFactory();
//    JSUtils.resetImageNameCounter();
    // Copy across image parts
    for (int i = 0; i < finalListOfContours.size(); i++)
    {
      MatOfPoint thisContour = finalListOfContours.get(i);
      Rect bb = Imgproc.boundingRect(thisContour);
      // Translate contours
      Point topLeft = new Point(bb.x, bb.y);
      Mat matChunk = Mat.zeros(bb.size(), image.getType());
//      MatOfPoint translatedContour = CVUtils.translateMatOfPoints(thisContour, topLeft);

      matChunk = mat.submat(bb).clone();

      // Draw fill mask
      Mat contourMask = new Mat(matChunk.size(), CvType.CV_32SC1);
      contourMask.setTo(new org.opencv.core.Scalar(0));
      LinkedList<MatOfPoint> listOfPoints = new LinkedList<MatOfPoint>();
//      listOfPoints.add(translatedContour);
//      Core.fillPoly(contourMask, listOfPoints, new org.opencv.core.Scalar(-1));

      // Add to DMImage list
      Imgproc.cvtColor(matChunk, matChunk, Imgproc.COLOR_HSV2BGR);
//      BufferedImage bufImage = CVUtils.getAlphaBufferedImageFromMat(matChunk, contourMask);

      // Add positional info
//      String name = JSUtils.generateImageName();
//      DMImage dmImage = factory.new DMImage(name, topLeft, bufImage);
//      dmImageList.add(dmImage);
    }
    if (finalListOfContours.size() == 0)
    {
//      JOptionPane.showMessageDialog(PaperPanel.getInstance(), "We could not find any characters in this image, please try again!");
//      PaperPanel.getInstance().webcamCancel();
    }

    return null;
  }
}
