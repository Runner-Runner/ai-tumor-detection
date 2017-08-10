package extractcores.assignmentproblem;

import extractcores.CoreComparator;
import extractcores.DefaultConfigValues;
import extractcores.ImageProcessor;
import static extractcores.ImageProcessor.appendFilename;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import hungarian.Hungarian;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeometricBatchSolver extends AssignmentSolver
{
  private int modelCount;
  private GeometricModel[] geometricModels;
  private SimpleGridSolver simpleSolver;

  private static final int INITIAL_MODEL_POINTS = 3;
  private static final double LOW_ANGLE_INIT_FACTOR = 0.000001;

  public GeometricBatchSolver(List<TissueCore> cores, LabelInformation labelInformation, String edgeFileName)
  {
    super(cores, labelInformation, edgeFileName);
    modelCount = labelInformation.getRowCount();
    geometricModels = new GeometricModel[modelCount];
    for (int i = 0; i < modelCount; i++)
    {
      geometricModels[i] = generateGeometricModel();
    }
  }

  private GeometricModel generateGeometricModel()
  {
    return new PolynomialModel();
  }

  public void assignCores()
  {
    System.out.println("Simple Grid Solver:");
    simpleSolver = new SimpleGridSolver(cores, labelInformation, edgeFileName);
    simpleSolver.assignCores();
    AssignmentInformation simpleAssignmentInformation = simpleSolver.
            getAssignmentInformation();
    System.out.println("Done.");

    System.out.println("Geometric Batch Solver:");

    initialize(simpleAssignmentInformation);

    CoreComparator coreComparator = new CoreComparator();
    coreComparator.setComparisonType(CoreComparator.CompareType.VERTICAL_BOTTOM);
    Collections.sort(cores, coreComparator);

    List<TissueCore> copyCores = new ArrayList<>();
    copyCores.addAll(cores);
    List<TissueCore> batchCores = new ArrayList<>();

    for (int rowIndex = 0; rowIndex < modelCount; rowIndex++)
    {
      if (rowIndex == modelCount - 1)
      {
        System.out.println("Processing batch: last row " + (rowIndex + 1) + ".");
        //Assign remaining cores, remove outliers
        GeometricModel model = geometricModels[modelCount - 1];
        model.addCores(copyCores);
        model.updateModel();
        model.removeOutliers();
        createBatchInformation(rowIndex, batchCores, null);
        continue;
      }

      System.out.println("Processing batch: row " + (rowIndex + 1) + " and "
              + (rowIndex + 2) + ".");

      double yBound = Double.MAX_VALUE;
      PolynomialModel boundModel = new PolynomialModel();
      if (rowIndex == 0)
      {
        yBound = simpleSolver.getMinY() + 4 * simpleSolver.getIntervalHeight();
      }
      else
      {
        GeometricModel model = geometricModels[rowIndex - 1];
        double[] coefficients = model.getCoefficients();
        coefficients[2] += 3 * simpleSolver.getIntervalHeight();
        boundModel.setCoefficients(coefficients);
      }

      for (int i = 0; i < copyCores.size(); i++)
      {
        TissueCore core = copyCores.get(i);
        ///
        if (core.getId() == 205)
        {
          int a = 3;
        }
        ///
        if (rowIndex != 0)
        {
          yBound = boundModel.getY(core.getCenterX());
        }
        if (core.getBoundingBox().y + core.getBoundingBox().height < yBound)
        {
          batchCores.add(core);
        }
      }
      copyCores.removeAll(batchCores);

      if (rowIndex > 0)
      {
        batchCores.removeAll(geometricModels[rowIndex - 1].getAssignedCores());
        //Instead of discarding, compare to increase certainty?
        geometricModels[rowIndex].clearCores();
        createBatchInformation(rowIndex, batchCores, boundModel);
      }

      assignBatch(rowIndex, batchCores);
      createBatchInformation(rowIndex, batchCores, null);
    }

    System.out.println(copyCores.size() + " cores without assignment: ");
    for (TissueCore core : copyCores)
    {
      System.out.print(core.getId() + " ");
    }
    System.out.println("");

    assignmentInformation = new AssignmentInformation();
    for (int i = 0; i < geometricModels.length; i++)
    {
      GeometricModel model = geometricModels[i];
      List<TissueCore> assignedCores = model.getAssignedCores();
      int coreCount = assignedCores.size();
      int columnCount = labelInformation.getColumnCount();
      int[] indexResult = new int[columnCount];

      if (coreCount > columnCount)
      {
        //TODO use hungarian for all with dummies!
        System.out.print("Too many cores assigned to model #" + (i + 1) + ". Ignored cores: ");
        for (int j = coreCount - 1; j >= columnCount; j--)
        {
          TissueCore core = assignedCores.remove(j);
          coreCount = columnCount;
          System.out.print(core.getId() + " ");
        }
        System.out.println("");
      }

      if (coreCount == columnCount)
      {
        for (int j = 0; j < coreCount; j++)
        {
          indexResult[j] = j;
        }
      }
      else
      {
        int[] coreScale = new int[columnCount];
        double intervalWidth = simpleSolver.getIntervalWidth();
        int minX = simpleSolver.getMinX();
        int interval = (int) (minX + intervalWidth / 2);
        for (int j = 0; j < columnCount; j++)
        {
          coreScale[j] = interval;
          interval += intervalWidth;
        }

        int[][] rowCostMatrix = new int[columnCount][columnCount];
        for (int j = 0; j < columnCount; j++)
        {
          int centerX;
          if (j >= coreCount)
          {
            centerX = Integer.MAX_VALUE;
          }
          else
          {
            TissueCore core = assignedCores.get(j);
            centerX = core.getCenterX();
          }

          for (int k = 0; k < columnCount; k++)
          {
            if (centerX == Integer.MAX_VALUE)
            {
              rowCostMatrix[j][k] = Integer.MAX_VALUE;
            }
            else
            {
              rowCostMatrix[j][k] = (int) Math.pow(coreScale[k] - centerX, 2);
            }
          }
        }
        Hungarian hungarian = new Hungarian(rowCostMatrix);
        indexResult = hungarian.getResult();
      }
      for (int j = 0; j < coreCount; j++)
      {
        TissueCore core = assignedCores.get(j);
        int index = indexResult[j];

        assignmentInformation.addAssignment(i, index, core, null);
      }
    }

    System.out.println("Done.");
  }

  private void initialize(AssignmentInformation simplestAssignmentInformation)
  {
    for (int i = 0; i < modelCount; i++)
    {
      List<Assignment> lowestInRow = simplestAssignmentInformation.
              getLowestInRow(i, INITIAL_MODEL_POINTS);
      double yAvg = 0;
      for (Assignment assignment : lowestInRow)
      {
        yAvg += assignment.getCore().getCenterY();
      }
      yAvg /= lowestInRow.size();

      geometricModels[i].setCoefficients(new double[]
      {
        LOW_ANGLE_INIT_FACTOR, LOW_ANGLE_INIT_FACTOR, yAvg
      });
    }
  }

  private void assignBatch(int modelIndex, List<TissueCore> batchCores)
  {
    for (int i = 0; i < batchCores.size(); i++)
    {
      //TODO Check previous models for cores, if contained re-compare distances
      GeometricModel model1 = geometricModels[modelIndex];
      GeometricModel model2 = geometricModels[modelIndex + 1];

      TissueCore core = batchCores.get(i);
      ///
      if (core.getId() == 219)
      {
        int a = 3;
      }
      ///
      int distanceModel1 = (int) model1.getDistance(core);
      int distanceModel2 = (int) model2.getDistance(core);

      int distanceO1Model1 = 0;
      int distanceO1Model2 = 0;
      TissueCore overlappingCore1 = model1.getXOverlappingCore(core);
      if (overlappingCore1 != null)
      {
        distanceO1Model1 = (int) model1.getDistance(overlappingCore1);
        distanceO1Model2 = (int) model2.getDistance(overlappingCore1);
      }

      int distanceO2Model1 = 0;
      int distanceO2Model2 = 0;
      TissueCore overlappingCore2 = model2.getXOverlappingCore(core);
      if (overlappingCore2 != null)
      {
        distanceO2Model1 = (int) model1.getDistance(overlappingCore2);
        distanceO2Model2 = (int) model2.getDistance(overlappingCore2);
      }

      //TODO Check for enclosed core
      //Check all possible permutations of the max. three cores and their costs
      //Except swapping two overlapping cores, as this would have been done 
      //earlier if lower cost
      int[] scenarioCosts = new int[]
      {
        -1, -1, -1, -1, -1, -1, -1
      };

      if (overlappingCore1 != null)
      {
        model1.removeCore(overlappingCore1);
        scenarioCosts[0] = distanceModel1 + distanceO1Model2;
        scenarioCosts[2] = distanceModel2 + distanceO1Model1;
      }
      if (overlappingCore2 != null)
      {
        model2.removeCore(overlappingCore2);
        scenarioCosts[1] = distanceModel1 + distanceO2Model2;
        scenarioCosts[3] = distanceModel2 + distanceO2Model1;
      }
      if (overlappingCore1 != null && overlappingCore2 != null)
      {
        scenarioCosts[4] = distanceO1Model1 + distanceO2Model2;
      }
      if (overlappingCore1 == null && overlappingCore2 == null)
      {
        scenarioCosts[5] = distanceModel1;
        scenarioCosts[6] = distanceModel2;
      }

      int lowestCostIndex = -1;
      double lowestCost = Double.MAX_VALUE;
      for (int j = 0; j < scenarioCosts.length; j++)
      {
        double scenarioCost = scenarioCosts[j];
        if (scenarioCost == -1)
        {
          continue;
        }
        if (scenarioCost < lowestCost)
        {
          lowestCost = scenarioCost;
          lowestCostIndex = j;
        }
      }

      switch (lowestCostIndex)
      {
        case 0:
          model2.addCore(overlappingCore1);
          model1.addCore(core);
          break;
        case 1:
          model1.addCore(core);
          model2.addCore(overlappingCore2);
          break;
        case 2:
          model1.addCore(overlappingCore1);
          model2.addCore(core);
          break;
        case 3:
          model1.addCore(overlappingCore2);
          model2.addCore(core);
          break;
        case 4:
          //New core = outsider
          model1.addCore(overlappingCore1);
          model2.addCore(overlappingCore2);
          break;
        case 5:
          model1.addCore(core);
          break;
        case 6:
          model2.addCore(core);
          break;
      }
    }
    geometricModels[modelIndex].updateModel();
    geometricModels[modelIndex].removeOutliers();
  }

  @Override
  public List<TissueCore> createLabeledCores()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected void createAssignmentInformation()
  {

  }

  protected void createBatchInformation(int rowIndex, List<TissueCore> batchCores,
          GeometricModel boundModel)
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage edgeImage = imageProcessor.readImage(
            DefaultConfigValues.FILE_PATH_EDGE,
            edgeFileName);
    Graphics2D g = (Graphics2D) edgeImage.getGraphics();

    //Draw cores considered in this batch
    g.setColor(Color.ORANGE);
    g.setStroke(new BasicStroke(1));
    for (TissueCore batchCore : batchCores)
    {
      Rectangle boundingBox = batchCore.getBoundingBox();
      g.drawRect(boundingBox.x + 5, boundingBox.y + 5, boundingBox.width - 10,
              boundingBox.height - 10);
    }

    int upper = Math.min(rowIndex + 1, geometricModels.length - 1);

    GeometricModel models[] = new GeometricModel[3];
    models[0] = geometricModels[rowIndex];
    models[1] = geometricModels[upper];
    models[2] = boundModel;

    for (int i = 0; i < models.length; i++)
    {
      GeometricModel model = models[i];
      if (i == 2)
      {
        if (boundModel == null)
        {
          continue;
        }
        g.setColor(Color.ORANGE);
      }
      else
      {
        g.setColor(Color.GREEN);
      }
      g.setStroke(new BasicStroke(2));

      double[] coefficients = model.getCoefficients();
      if (coefficients.length == 3)
      {
        int[] curvePointsX = new int[edgeImage.getWidth()];
        int[] curvePointsY = new int[edgeImage.getWidth()];
        for (int x = 0; x < edgeImage.getWidth(); x++)
        {
          int y = model.getY(x);
          curvePointsX[x] = x;
          curvePointsY[x] = y;
        }
        g.drawPolyline(curvePointsX, curvePointsY, curvePointsX.length);

        g.setColor(Color.CYAN);

        for (int j = 0; j < model.getCoreCount(); j++)
        {
          TissueCore core = model.getCore(j);
          int x = core.getCenterX();
          int coreY = core.getCenterY();
          int curveY = model.getY(x);

          g.setStroke(new BasicStroke(2));
          g.drawLine(x, coreY, x, curveY);

          Rectangle boundingBox = core.getBoundingBox();
          g.setStroke(new BasicStroke(1));
          g.drawRect(boundingBox.x, boundingBox.y, boundingBox.width,
                  boundingBox.height);
        }
      }
    }

    g.dispose();
    String postfix = "";
    if (boundModel != null)
    {
      postfix = "-boundary";
    }
    imageProcessor.writeImage(edgeImage,
            DefaultConfigValues.FILE_PATH_INFORMATIVE,
            appendFilename(edgeFileName, "batch-r" + rowIndex + "-"
                    + (rowIndex + 1) + postfix, "png"));
  }
}
