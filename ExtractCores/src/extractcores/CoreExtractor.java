package extractcores;

import static extractcores.DefaultConfigValues.EXTREME_CORE_RATIO_THRESHOLD;
import static extractcores.DefaultConfigValues.LARGE_CORE_AREA_THRESHOLD;
import static extractcores.DefaultConfigValues.MIN_OBJECT_AREA;
import extractcores.assignmentproblem.Assignment;
import extractcores.assignmentproblem.AssignmentInformation;
import extractcores.assignmentproblem.GeometricBatchSolver;
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
import label.ManualSolutionReader;
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
          String labelFileName, int digitKey)
  {
    LabelProcessor labelProcessor = new LabelProcessor();
    LabelInformation labelInformation = labelProcessor.readTxtLabelFile(
            DefaultConfigValues.FILE_PATH_LABEL, labelFileName);

    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage edgeImage = imageProcessor.readImage(pathName, edgeFileName);

    List<TissueCore> cores = detectCores(pathName, edgeFileName,
            labelInformation, edgeImage);

    List<TissueCore> labeledCores = assignLabels(labelInformation, cores,
            edgeFileName, digitKey);

    return labeledCores;
  }

  private List<TissueCore> detectCores(String pathName, String edgeFileName,
          LabelInformation labelInformation, BufferedImage edgeImage)
  {
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
    for (int i = 0; i < mergedCores.size(); i++)
    {
      mergedCores.get(i).setIds(i);
    }

    //Detect and merge redundant core parts
    mergedCores = mergeBrokenCores(mergedCores, labelInformation);

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
    return mergedCores;
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
          List<TissueCore> cores, String edgeFileName, int digitKey)
  {
    GeometricBatchSolver solver = new GeometricBatchSolver(
            cores, labelInformation, edgeFileName);
//    GeometricKMeansSolver solver = new GeometricKMeansSolver(
//            cores, labelInformation, edgeFileName);
//    HungarianAssignmentSolver solver = new HungarianAssignmentSolver(
//            cores, labelInformation, edgeFileName);
//    SimpleGridSolver solver = new SimpleGridSolver(cores, labelInformation, 
//            edgeFileName);
    solver.assignCores();
    AssignmentInformation assignmentInformation = solver.getAssignmentInformation();

    ManualSolutionReader solutionReader = new ManualSolutionReader();
    AssignmentInformation solutionAssignmentInformation = solutionReader.
            readSolution(digitKey);

    int cellCount = labelInformation.getRowCount()
            * labelInformation.getColumnCount();

    int solutionCoreCount = 0;
    int solutionGapCount = 0;
    int solutionMergeCount = 0;

    int correctCoreCount = 0;
    int correctGapCount = 0;
    int correctMergeCount = 0;

    System.out.println("Core assignment results for #" + digitKey + ":");

    for (int i = 0; i < labelInformation.getRowCount(); i++)
    {
      for (int j = 0; j < labelInformation.getColumnCount(); j++)
      {
        System.out.print("Cell (" + (i + 1) + "/" + (j + 1) + "): ");

        Assignment assignment = assignmentInformation.getAssignment(i, j);
        Assignment solutionAssignment = solutionAssignmentInformation.
                getAssignment(i, j);

        if (assignment == null && solutionAssignment == null)
        {
          correctGapCount++;
          solutionGapCount++;
          System.out.print("Found correct gap.");
        }
        else if (assignment != null && solutionAssignment != null)
        {

          int[] ids = assignment.getCore().getIds();
          int[] solutionIds = solutionAssignment.getCore().getIds();

          if (solutionIds.length > 1)
          {
            solutionMergeCount++;
          }
          else
          {
            solutionCoreCount++;
          }

          //Check if both ID lists contain the exact same IDs
          if (ids.length == solutionIds.length)
          {
            boolean identical = true;
            for (int k = 0; k < ids.length; k++)
            {
              boolean found = false;
              for (int m = 0; m < solutionIds.length; m++)
              {
                if (ids[k] == solutionIds[m])
                {
                  found = true;
                  break;
                }
              }
              if (!found)
              {
                identical = false;
                break;
              }
            }
            if (identical)
            {
              if (ids.length > 1)
              {
                correctMergeCount++;
                System.out.print("Correct assignment: id = " + ids[0] + ".");
              }
              else
              {
                System.out.print("Correct merge: ids = [" + Arrays.toString(ids)
                        + "].");
                correctCoreCount++;
              }
            }
            else
            {
              if (ids.length > 1 || solutionIds.length > 1)
              {
                System.out.print("Merge difference: assigned [" + 
                        Arrays.toString(ids)
                        + "], actually [" + 
                        Arrays.toString(solutionIds) + "].");
              }
              else
              {
                System.out.print("Wrong ID: assigned [" + ids[0]
                        + "], actually [" + solutionIds[0] + "].");
              }
            }
          }
        }
        System.out.println("");
      }
    }

    double correctCorePercent = Double.valueOf(correctCoreCount) / solutionCoreCount * 100;
    double correctGapPercent = Double.valueOf(correctGapCount) / solutionGapCount * 100;
    double correctMergePercent = Double.valueOf(correctMergeCount) / solutionMergeCount * 100;
    double totalPercent = Double.valueOf(
            correctCoreCount + correctGapCount + correctMergeCount) / cellCount * 100;
    System.out.println("Total Results:");

    System.out.println("Correct cores: " + correctCoreCount + "/"
            + solutionCoreCount + ", "
            + String.format("%.2f", correctCorePercent) + "%.");
    System.out.println("Correct gaps: " + correctGapCount + "/"
            + solutionGapCount + ", "
            + String.format("%.2f", correctGapPercent) + "%.");
    System.out.println("Correct merges: " + correctMergeCount + "/"
            + solutionMergeCount + ", "
            + String.format("%.2f", correctMergePercent) + "%.");
    System.out.println("Overall performance: " + String.format("%.2f", totalPercent) + "%.");
    
    return null;
  }

  private List<TissueCore> mergeBrokenCores(List<TissueCore> cores,
          LabelInformation labelInformation)
  {
    List<TissueCore> copyCores = new ArrayList<>();
    copyCores.addAll(cores);
    List<TissueCore> mergedCores = new ArrayList<>();
    mergedCores.addAll(cores);

    CoreComparator coreComparator = new CoreComparator();
    coreComparator.setComparisonType(CoreComparator.CompareType.VERTICAL_CENTER);
    Collections.sort(copyCores, coreComparator);

    int avgBoundingArea = 0;
    int avgSmallestDistance = 0;

    List<List<Distance>> coreDistances = new ArrayList<>();
    for (int i = 0; i < copyCores.size(); i++)
    {
      List<Distance> distances = new ArrayList<>();
      coreDistances.add(distances);

      TissueCore core = copyCores.get(i);
      avgBoundingArea += core.getBoundingBox().getHeight()
              * core.getBoundingBox().getWidth();

      for (int j = 0; j < copyCores.size(); j++)
      {
        if (i == j)
        {
          continue;
        }

        TissueCore otherCore = copyCores.get(j);
        int distance = core.getDistance(otherCore);

        distances.add(new Distance(otherCore, distance));
      }
      Collections.sort(distances);
      avgSmallestDistance += distances.get(0).value;
    }

    avgBoundingArea /= copyCores.size();
    avgSmallestDistance /= copyCores.size();

    double brokenDistanceThreshold = 0.5 * avgSmallestDistance;
    double combinedSizeThreshold = 1.5 * avgBoundingArea;

    for (int i = 0; i < coreDistances.size(); i++)
    {
      List<Distance> distances = coreDistances.get(i);
      List<TissueCore> toBeMerged = new ArrayList<>();
      for (int j = 0; j < distances.size(); j++)
      {
        Distance distance = distances.get(j);
        if (distance.value > brokenDistanceThreshold)
        {
          break;
        }
        toBeMerged.add(distance.core);
      }
      if (!toBeMerged.isEmpty())
      {
        TissueCore core = copyCores.get(i);
        toBeMerged.add(core);
        int[] ids = new int[toBeMerged.size()];
        for (int k = 0; k < ids.length; k++)
        {
          ids[k] = toBeMerged.get(k).getId();
        }
        TissueCore unionCore = TissueCore.union(toBeMerged);
        unionCore.setIds(ids);

        double unionArea = unionCore.getBoundingBox().getHeight()
                * unionCore.getBoundingBox().getWidth();
        if (unionArea < combinedSizeThreshold)
        {
          boolean changed = mergedCores.removeAll(toBeMerged);
          if (changed)
          {
            mergedCores.add(unionCore);
          }
        }
      }
    }
    return mergedCores;
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

  private class Distance implements Comparable<Distance>
  {
    public TissueCore core;
    public int value;

    public Distance(TissueCore core, int value)
    {
      this.core = core;
      this.value = value;
    }

    @Override
    public int compareTo(Distance distance)
    {
      return this.value - distance.value;
    }
  }
}
