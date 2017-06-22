package extractcores;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;

public class CoreExtractor
{
  public CoreExtractor()
  {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  public Rectangle[][] findCores(String pathName, String edgeFileName,
          String labelFileName)
  {
    LabelProcessor labelProcessor = new LabelProcessor();
    LabelInformation labelInformation = labelProcessor.readTxtLabelFile(
            DefaultConfigValues.FILE_PATH_LABEL, labelFileName);
    int rowCount = labelInformation.getRowCount();
    int columnCount = labelInformation.getColumnCount();

    Rectangle[][] coreCoordinates = new Rectangle[rowCount][columnCount];

    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage edgeImage = imageProcessor.readImage(pathName, edgeFileName);

    int imageHeight = edgeImage.getHeight();
    int imageWidth = edgeImage.getWidth();

    //rgb color = -1 => bright pixel/edge
    ///Display
    BufferedImage foundObjectsImage = new BufferedImage(edgeImage.getWidth(),
            edgeImage.getHeight(), edgeImage.getType());
    Graphics g = foundObjectsImage.getGraphics();
    g.drawImage(edgeImage, 0, 0, null);
    ///

    Mat edgeMat = Imgcodecs.imread(pathName + edgeFileName, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

    Mat hierarchy = new Mat();
    List<MatOfPoint> contours = new ArrayList<>();
    List<MatOfPoint> finalContourList = new ArrayList<>();
    Imgproc.findContours(edgeMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, 
            Imgproc.CHAIN_APPROX_SIMPLE);

    List<TissueCore> cores = new ArrayList<>();
    List<Rect> boundingRects = new ArrayList<>();
    List<Integer> weights = new ArrayList<>();

    List<Integer> boundingAreas = new ArrayList<>();
    List<Double> boundingSideRatios = new ArrayList<>();

    for (int i = 0; i < contours.size(); i++)
    {
      g.setColor(Color.red);

      Rect boundingBox = Imgproc.boundingRect(contours.get(i));
      boundingRects.add(boundingBox);
      cores.add(new TissueCore(boundingBox));
      weights.add(1);

      int boundingBoxArea = boundingBox.width * boundingBox.height;
      boundingAreas.add(boundingBoxArea);
//      if (Imgproc.contourArea(contours.get(i)) > 1800)

      //TODO set max limit as well
      if (boundingBoxArea > 1800)
      {
        double sideRatio = Double.valueOf(boundingBox.height) / boundingBox.width;

        //Special case: skip bounding rectangles of full core size with extreme
        //ratio, indicating overlapping full cores on the array.
        if (boundingBoxArea > 3000 && sideRatio > 1.5)
        {
          continue;
        }

        g.setColor(Color.green);
        finalContourList.add(contours.get(i));
        boundingSideRatios.add(sideRatio);
      }

      g.drawRect(boundingBox.x, boundingBox.y,
              boundingBox.width, boundingBox.height);
      g.drawString(String.valueOf(boundingBoxArea),
              boundingBox.x + boundingBox.width + 5,
              boundingBox.y + boundingBox.height);
    }

    MatOfRect matOfRect = new MatOfRect();
    matOfRect.fromList(boundingRects);
    MatOfInt matWeights = new MatOfInt();
    matWeights.fromList(weights);
    Objdetect.groupRectangles(matOfRect, matWeights, 1, 0.2);

    g.setColor(Color.ORANGE);
    for (Rect rect : matOfRect.toList())
    {
      g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    g.dispose();
    imageProcessor.writeImage(foundObjectsImage,
            DefaultConfigValues.FILE_PATH_INFORMATIVE, "5512-rect.png");

    int missingCoreCount = labelInformation.getCoreCount() - finalContourList.size();
    double coreDetectionPercentage
            = Double.valueOf(finalContourList.size())
            / labelInformation.getCoreCount();
    System.out.println("Detected cores: " + coreDetectionPercentage + "%. "
            + missingCoreCount + " cores were not found.");

    Collections.sort(boundingAreas);
    Collections.reverse(boundingAreas);
    System.out.println(Arrays.toString(boundingAreas.toArray()));

    Collections.sort(boundingSideRatios);
    Collections.reverse(boundingSideRatios);
    System.out.println(Arrays.toString(boundingSideRatios.toArray()));

    //Sort found objects into array
//    boundingRectangles.get(0).tl();
//new class? how to sort into columns dynamically, measuring mean on the fly and
//adapting accordingly?
    

    return coreCoordinates;
  }

  //TODO Probably not needed
  /**
   * @param BufferedImage image -- expects BufferedImage.TYPE_3BYTE_BGR
   * @return
   */
  public static Mat getMatFromBufferedImage(BufferedImage image)
  {
    int type = image.getType();
    if (image.getType() != BufferedImage.TYPE_3BYTE_BGR)
    {
      throw new RuntimeException("Exception: getMatFromBufferedImage expects BufferedImage.TYPE_3BYTE_BGR as input");
    }

    DataBufferByte dbi = (DataBufferByte) image.getRaster().getDataBuffer();
    byte[] buff = dbi.getData();

    Mat mat = Mat.zeros(new Size(image.getWidth(), image.getHeight()), CvType.CV_8UC3);
    mat.put(0, 0, buff);
    return mat;
  }
}
