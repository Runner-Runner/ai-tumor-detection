package extractcores.assignmentproblem;

import extractcores.DefaultConfigValues;
import extractcores.ImageProcessor;
import static extractcores.ImageProcessor.appendFilename;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.List;

public class GeometricKMeansSolver extends AssignmentSolver
{
  private int modelCount;
  /**
   * Upper limit for assignments, higher than max to account for redundance
   */
  private int maxCoreCount;
  private GeometricModel[] geometricModel;
  private double simpleIntervalWidth;
  private double simpleIntervalHeight;

  private static int infoImageCount = 0;
  private static final int INITIAL_MODEL_POINTS = 3;
  private static final double LOW_ANGLE_INIT_FACTOR = 0.000001;

  public GeometricKMeansSolver(List<TissueCore> cores, LabelInformation labelInformation, String edgeFileName)
  {
    super(cores, labelInformation, edgeFileName);
    maxCoreCount = labelInformation.getColumnCount() + 3;
    modelCount = labelInformation.getRowCount();
    geometricModel = new GeometricModel[modelCount];
    for (int i = 0; i < modelCount; i++)
    {
      geometricModel[i] = generateGeometricModel();
    }
  }

  private GeometricModel generateGeometricModel()
  {
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

    System.out.println("Geometric K-Means Solver:");
    System.out.println("Initialize Models.");

    initialize(simpleAssignmentInformation);

    createAssignmentInformation();

    int iterations = 5;
    for (int i = 0; i < iterations; i++)
    {
      reassignData();
      updateModels();
      System.out.println("Reassign data. Update models. Iteration #" + (i + 1) + ".");

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

  private void updateModels()
  {
    for (GeometricModel model : geometricModel)
    {
      model.updateModel();
    }
  }

  private void reassignData()
  {
    //TODO introduce penalty cost for cores to close to each other
    for (int i = 0; i < modelCount; i++)
    {
      geometricModel[i].clearCores();
    }

    for (int i = 0; i < cores.size(); i++)
    {
      TissueCore core = cores.get(i);

      double lowestDistance = Double.MAX_VALUE;
      int lowestModelIndex = -1;
      for (int j = 0; j < modelCount; j++)
      {
        if (geometricModel[j].getCoreCount() <= maxCoreCount)
        {
          //Distance to curve
          double cost = geometricModel[j].getDistance(core);

          //Closeness penalty to other cores on curve
          cost += geometricModel[j].getClosenessCost(core, simpleIntervalWidth,
                  simpleIntervalHeight);

          //TODO is that a reasonable metric, how to test-tweak?
          if (cost < lowestDistance)
          {
            lowestDistance = cost;
            lowestModelIndex = j;
          }
        }
      }
      geometricModel[lowestModelIndex].addCore(core);
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
    g.setColor(Color.GREEN);
    g.setStroke(new BasicStroke(2));
    
    for (int i = 0; i < modelCount; i++)
    {
      double[] coefficients = geometricModel[i].getCoefficients();
      if (coefficients.length == 3)
      {
        //TODO draw polynomial curve with efficient function if possible
//        int[] curvePointsX = new int[edgeImage.getWidth()];
//        int[] curvePointsY = new int[edgeImage.getWidth()];
//        for (int x = 0; x < edgeImage.getWidth()-1; x++)
//        {
//          int y = (int) (coefficients[2] * Math.pow(x, 2) + coefficients[1] * x
//                  + coefficients[0]);
//          curvePointsX[x] = x;
//          curvePointsY[x] = y;
//        }
//        g.drawPolyline(curvePointsX, curvePointsY, curvePointsX.length);

        int lastY = (int) coefficients[0];
        for (int x = 1; x < edgeImage.getWidth(); x++)
        {
          int y = (int) (coefficients[2] * Math.pow(x, 2) + coefficients[1] * x
                  + coefficients[0]);
          g.drawLine(x-1, lastY, x, y);
          lastY = y;
        }

        for (int j = 0; j < geometricModel[i].getCoreCount(); j++)
        {
          TissueCore core = geometricModel[i].getCore(j);
          int x = core.getCenterX();
          int coreY = core.getCenterY();
          int curveY = (int) (coefficients[2] * Math.pow(x, 2) + coefficients[1] * x
                  + coefficients[0]);
          g.drawLine(x, coreY, x, curveY);
        }
      }
    }

    g.dispose();
    imageProcessor.writeImage(edgeImage,
            DefaultConfigValues.FILE_PATH_INFORMATIVE,
            appendFilename(edgeFileName, "kmeans-" + ++infoImageCount, "png"));
  }
}
