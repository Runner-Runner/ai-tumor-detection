package extractcores.assignmentproblem;

import extractcores.CoreComparator;
import extractcores.DefaultConfigValues;
import extractcores.ImageProcessor;
import static extractcores.ImageProcessor.appendFilename;
import extractcores.LabelInformation;
import extractcores.TissueCore;
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
  /**
   * Upper limit for assignments, higher than max to account for redundance
   */
  private int columnCount;
  private GeometricModel[] geometricModel;
  private double simpleIntervalWidth;
  private double simpleIntervalHeight;

  private static int infoImageCount = 0;
  private static final int INITIAL_MODEL_POINTS = 3;
  private static final double LOW_ANGLE_INIT_FACTOR = 0.000001;

  public GeometricBatchSolver(List<TissueCore> cores, LabelInformation labelInformation, String edgeFileName)
  {
    super(cores, labelInformation, edgeFileName);
    columnCount = labelInformation.getColumnCount();
    modelCount = labelInformation.getRowCount();
    geometricModel = new GeometricModel[modelCount];
    //TODO does that make sense here?
    for (int i = 0; i < modelCount; i++)
    {
      geometricModel[i] = generateGeometricModel();
    }
  }

  private GeometricModel generateGeometricModel()
  {
    //TODO do I even need polynomials here? Does it work with just avged lines?
    return new PolynomialModel();
  }

  public void assignCores()
  {
    System.out.println("Simple Grid Solver:");
    SimpleGridSolver simpleSolver = new SimpleGridSolver(cores, labelInformation, edgeFileName);
    simpleSolver.assignCores();
    AssignmentInformation simpleAssignmentInformation = simpleSolver.
            getAssignmentInformation();
    simpleIntervalWidth = simpleSolver.getIntervalWidth();
    simpleIntervalHeight = simpleSolver.getIntervalHeight();
    System.out.println("Done.");

    System.out.println("Geometric Batch Solver:");

    initialize(simpleAssignmentInformation);

    CoreComparator coreComparator = new CoreComparator();
    coreComparator.setComparisonType(CoreComparator.CompareType.VERTICAL_BOTTOM);
    Collections.sort(cores, coreComparator);

    int batchSize = 3 * columnCount;
    int coreCount = cores.size();

//    for (int rowIndex = 0; rowIndex < modelCount - 1; rowIndex++)
    for (int rowIndex = 0; rowIndex < 2; rowIndex++)
    {
      System.out.println("Processing batch: row " + (rowIndex + 1) + " and "
              + (rowIndex + 2) + ".");

      int lowerBound = Math.max(rowIndex * columnCount - columnCount / 2, 0);
      int upperBound = Math.min((rowIndex + 2) * columnCount + columnCount / 2, coreCount - 1);

      List<TissueCore> batchCores = new ArrayList<>();
      for (int i = lowerBound; i <= upperBound; i++)
      {
        batchCores.add(cores.get(i));
      }
      
      //Remove all assigned cores from model 1.
      if(rowIndex > 0)
      {
        batchCores.removeAll(geometricModel[rowIndex-1].getAssignedCores());
        //Instead of discarding, compare to increase certainty?
        geometricModel[rowIndex].clearCores();
      }

      assignBatch(rowIndex, batchCores);
      createAssignmentInformation();
    }

    System.out.println("Done.");
  }

  private void initialize(AssignmentInformation simplestAssignmentInformation)
  {
    //TODO How to avoid "overfitted" curves?? Should always have low-angle curves.
    for (int i = 0; i < modelCount; i++)
    {
      List<Assignment> lowestInRow = simplestAssignmentInformation.
              getLowestInRow(i, INITIAL_MODEL_POINTS);
      double yAvg = 0;
      for (Assignment assignment : lowestInRow)
      {
        yAvg += cores.get(assignment.getCoreIndex()).getCenterY();
      }
      yAvg /= lowestInRow.size();

      geometricModel[i].setCoefficients(new double[]
      {
        LOW_ANGLE_INIT_FACTOR, LOW_ANGLE_INIT_FACTOR, yAvg
      });
    }
  }

  private void assignBatch(int modelIndex, List<TissueCore> batchCores)
  {
    for (int i = 0; i < batchCores.size(); i++)
    {
      //TODO Redundant cores need to be handled before this!
      //TODO Check previous models for cores, if contained re-compare distances
      GeometricModel model1 = geometricModel[modelIndex];
      GeometricModel model2 = geometricModel[modelIndex + 1];

      TissueCore core = batchCores.get(i);
      ///
      if (core.getId() == 254 || core.getId() == 243 || core.getId() == 224)
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
  }

  @Override
  public List<TissueCore> createLabeledCores()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected void createAssignmentInformation()
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage edgeImage = imageProcessor.readImage(
            DefaultConfigValues.FILE_PATH_EDGE,
            edgeFileName);
    Graphics2D g = (Graphics2D) edgeImage.getGraphics();

    for (int i = 0; i < modelCount; i++)
    {
      g.setColor(Color.GREEN);
      g.setStroke(new BasicStroke(2));

      double[] coefficients = geometricModel[i].getCoefficients();
      if (coefficients.length == 3)
      {
        int[] curvePointsX = new int[edgeImage.getWidth()];
        int[] curvePointsY = new int[edgeImage.getWidth()];
        for (int x = 0; x < edgeImage.getWidth(); x++)
        {
          int y = (int) (coefficients[0] * Math.pow(x, 2) + coefficients[1] * x
                  + coefficients[2]);
          curvePointsX[x] = x;
          curvePointsY[x] = y;
        }
        g.drawPolyline(curvePointsX, curvePointsY, curvePointsX.length);

        g.setColor(Color.CYAN);

        for (int j = 0; j < geometricModel[i].getCoreCount(); j++)
        {
          TissueCore core = geometricModel[i].getCore(j);
          int x = core.getCenterX();
          int coreY = core.getCenterY();
          int curveY = (int) (coefficients[0] * Math.pow(x, 2) + coefficients[1] * x
                  + coefficients[2]);

          g.setStroke(new BasicStroke(2));
          g.drawLine(x, coreY, x, curveY);

          Rectangle boundingBox = core.getBoundingBox();
          g.setStroke(new BasicStroke(1));
          g.drawRect(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        }
      }
    }

    g.dispose();
    imageProcessor.writeImage(edgeImage,
            DefaultConfigValues.FILE_PATH_INFORMATIVE,
            appendFilename(edgeFileName, "batch-" + ++infoImageCount, "png"));
  }
}
