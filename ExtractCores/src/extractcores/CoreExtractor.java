package extractcores;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.LinkedList;
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
    CoreLabel[][] coreLabels = labelProcessor.readLabelImage(labelFileName);
    int rowCount = coreLabels.length;
    int columnCount = coreLabels[0].length;

    Rectangle[][] coreCoordinates = new Rectangle[rowCount][columnCount];

    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage edgeImage = imageProcessor.readImage(pathName, edgeFileName);

    int imageHeight = edgeImage.getHeight();
    int imageWidth = edgeImage.getWidth();

    //rgb color = -1 => bright pixel/edge
    for (int h = 0; h < imageWidth; h++)
    {
      for (int v = 0; v < imageHeight; v++)
      {
        int pixelColor = edgeImage.getRGB(h, v);
      }
    }

    ///Display
    BufferedImage foundObjectsImage = new BufferedImage(edgeImage.getWidth(),
            edgeImage.getHeight(), edgeImage.getType());
    Graphics g = foundObjectsImage.getGraphics();
    g.drawImage(edgeImage, 0, 0, null);
    ///

    Mat edgeMat = Imgcodecs.imread(pathName + edgeFileName, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

    Mat hierarchy = new Mat();
    LinkedList<MatOfPoint> contours = new LinkedList<>();
    LinkedList<MatOfPoint> finalListOfContours = new LinkedList<>();
    Imgproc.findContours(edgeMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

    List<Rect> rectangles = new ArrayList<>();
    List<Integer> weights = new ArrayList<>();

    for (int i = 0; i < contours.size(); i++)
    {
      g.setColor(Color.red);

      Rect boundingBox = Imgproc.boundingRect(contours.get(i));
      rectangles.add(boundingBox);
      weights.add(1);

      int boundingBoxArea = boundingBox.width * boundingBox.height;
//      if (Imgproc.contourArea(contours.get(i)) > 1800)

      //TODO set max limit as well
      if (boundingBoxArea > 1800)
      {
        g.setColor(Color.green);
        finalListOfContours.add(contours.get(i));
      }

//      g.drawRect(boundingBox.x, boundingBox.y,
//              boundingBox.width, boundingBox.height);
    }

    MatOfRect matOfRect = new MatOfRect();
    matOfRect.fromList(rectangles);
    MatOfInt matWeights = new MatOfInt();
    matWeights.fromList(weights);
    Objdetect.groupRectangles(matOfRect, matWeights, 1, 0.2);

    g.setColor(Color.RED);
    for (Rect rect : matOfRect.toList())
    {
      g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    g.dispose();
    imageProcessor.writeImage(foundObjectsImage,
            DefaultPaths.FILE_PATH_INFORMATIVE, "5512-rect.png");

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
