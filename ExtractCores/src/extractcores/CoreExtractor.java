package extractcores;

import static extractcores.DefaultConfigValues.EXTREME_CORE_RATIO_THRESHOLD;
import static extractcores.DefaultConfigValues.LARGE_CORE_AREA_THRESHOLD;
import static extractcores.DefaultConfigValues.MIN_OBJECT_AREA;
import extractcores.assignmentproblem.GeometricKMeansSolver;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class CoreExtractor
{
  public CoreExtractor()
  {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  public List<TissueCore> findCores(String pathName, String edgeFileName,
          String labelFileName)
  {
    LabelProcessor labelProcessor = new LabelProcessor();
    LabelInformation labelInformation = labelProcessor.readTxtLabelFile(
            DefaultConfigValues.FILE_PATH_LABEL, labelFileName);

    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage edgeImage = imageProcessor.readImage(pathName, edgeFileName);

    //rgb color = -1 => bright pixel/edge
    Mat edgeMat = Imgcodecs.imread(pathName + edgeFileName, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

    Mat hierarchy = new Mat();
    List<MatOfPoint> contours = new ArrayList<>();
    Imgproc.findContours(edgeMat, contours, hierarchy, Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE);

    List<TissueCore> allCores = new ArrayList<>();

    List<Integer> boundingAreas = new ArrayList<>();
    List<Double> boundingSideRatios = new ArrayList<>();

    for (int i = 0; i < contours.size(); i++)
    {
      Rect boundingBox = Imgproc.boundingRect(contours.get(i));
      int boundingBoxArea = boundingBox.width * boundingBox.height;
      double sideRatio = Double.valueOf(boundingBox.height) / boundingBox.width;

      //TODO set max limit as well
      if (boundingBoxArea >= MIN_OBJECT_AREA)
      {
        allCores.add(new TissueCore(boundingBox.x, boundingBox.y,
                boundingBox.width, boundingBox.height));
      }

      boundingAreas.add(boundingBoxArea);
      boundingSideRatios.add(sideRatio);
    }

    List<TissueCore> mergedCores = mergeIntersectingRectangles(allCores);

    int coreCount = labelInformation.getCoreCount();
    int foundCoreCount = mergedCores.size();
    int missingCoreCount = coreCount - foundCoreCount;
    String coreMessage;
    if (missingCoreCount >= 0)
    {
      coreMessage = " cores were not found.";
    }
    else
    {
      missingCoreCount *= -1;
      coreMessage = " redundant cores were found.";
    }

    double coreDetectionPercentage
            = Double.valueOf(foundCoreCount)
            / coreCount * 100;
    System.out.println("Detected cores: "
            + String.format("%.2f", coreDetectionPercentage) + "%. "
            + missingCoreCount + coreMessage);

    createCoreInformation(edgeImage, allCores, mergedCores, edgeFileName,
            boundingAreas, boundingSideRatios);

    return assignLabels(labelInformation, mergedCores, edgeFileName);
  }

  private List<TissueCore> mergeIntersectingRectangles(List<TissueCore> sourceCores)
  {
    List<TissueCore> toBeGroupedCores = new ArrayList<>();
    toBeGroupedCores.addAll(sourceCores);

    List<List<TissueCore>> coreGroups = new ArrayList<>();

    while (!toBeGroupedCores.isEmpty())
    {
      TissueCore sourceCore = toBeGroupedCores.remove(0);
      boolean addToGroup = false;
      for (List<TissueCore> coreGroup : coreGroups)
      {
        for (TissueCore core : coreGroup)
        {
          if (sourceCore.intersects(core) || sourceCore.closeTo(core))
          {
            addToGroup = true;
            break;
          }
        }
        if (addToGroup)
        {
          coreGroup.add(sourceCore);
          break;
        }
      }

      if (!addToGroup)
      {
        List<TissueCore> coreGroup = new ArrayList<>();
        coreGroup.add(sourceCore);
        coreGroups.add(coreGroup);
      }
    }

    List<TissueCore> mergedCores = new ArrayList<>();

    for (List<TissueCore> coreGroup : coreGroups)
    {
      TissueCore unionCore = TissueCore.union(coreGroup);

      //Special case: skip bounding rectangles of at least full core size with 
      //extreme ratio, indicating overlapping full cores on the array.
      Rectangle boundingBox = unionCore.getBoundingBox();
      int boundingBoxArea = boundingBox.width * boundingBox.height;
      double sideRatio = Double.valueOf(boundingBox.height) / boundingBox.width;
      if (boundingBoxArea > LARGE_CORE_AREA_THRESHOLD
              && sideRatio > EXTREME_CORE_RATIO_THRESHOLD)
      {
        continue;
      }

      mergedCores.add(unionCore);
    }

    //New mergeable groups can form after merging.
    //-> Recursively merge until number of cores after merging is unchanged.
    if (mergedCores.size() != sourceCores.size())
    {
      mergedCores = mergeIntersectingRectangles(mergedCores);
    }
    return mergedCores;
  }

  private List<TissueCore> assignLabels(LabelInformation labelInformation,
          List<TissueCore> cores, String edgeFileName)
  {
    GeometricKMeansSolver solver = new GeometricKMeansSolver(
            cores, labelInformation, edgeFileName);
//    HungarianAssignmentSolver solver = new HungarianAssignmentSolver(
//            cores, labelInformation, edgeFileName);
//    SimpleGridSolver solver = new SimpleGridSolver(cores, labelInformation, 
//            edgeFileName);
    solver.assignCores();
    return null;
  }

  private void createCoreInformation(BufferedImage edgeImage,
          List<TissueCore> allCores, List<TissueCore> mergedCores,
          String edgeFileName, List<Integer> boundingAreas,
          List<Double> boundingSideRatios)
  {
    BufferedImage foundObjectsImage = new BufferedImage(edgeImage.getWidth(),
            edgeImage.getHeight(), edgeImage.getType());
    Graphics g = foundObjectsImage.getGraphics();
    g.drawImage(edgeImage, 0, 0, null);

//    g.setColor(Color.red);
//    for (TissueCore core : allCores)
//    {
//      if (!mergedCores.contains(core))
//      {
//        drawBoundingBox(g, core);
//      }
//    }
    g.setColor(Color.green);
    for (int i = 0; i < mergedCores.size(); i++)
    {
      drawBoundingBox(g, mergedCores.get(i), i);
    }

    g.dispose();
    ImageProcessor imageProcessor = new ImageProcessor();
    imageProcessor.writeImage(foundObjectsImage,
            DefaultConfigValues.FILE_PATH_INFORMATIVE,
            ImageProcessor.appendFilename(edgeFileName, "info", "png"));

    Collections.sort(boundingAreas);
    Collections.reverse(boundingAreas);
    System.out.println(Arrays.toString(boundingAreas.toArray()));

    Collections.sort(boundingSideRatios);
    Collections.reverse(boundingSideRatios);
    System.out.println(Arrays.toString(boundingSideRatios.toArray()));
  }

  private void drawBoundingBox(Graphics g, TissueCore core, int index)
  {
    Rectangle boundingBox = core.getBoundingBox();
    g.drawRect(boundingBox.x, boundingBox.y,
            boundingBox.width, boundingBox.height);

    String indexString = String.valueOf(index);
    int x = boundingBox.x + boundingBox.width - 20;
    int y = boundingBox.y + boundingBox.height - 20;
    
    FontMetrics fm = g.getFontMetrics();
    Rectangle2D rect = fm.getStringBounds(indexString, g);

    g.setColor(Color.DARK_GRAY);
    g.fillRect(x,
            y - fm.getAscent(),
            (int) rect.getWidth(),
            (int) rect.getHeight());
    g.setColor(Color.GREEN);
    g.drawString(indexString, x, y);
  }
}
