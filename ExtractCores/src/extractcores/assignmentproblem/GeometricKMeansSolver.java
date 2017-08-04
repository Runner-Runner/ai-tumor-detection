package extractcores.assignmentproblem;

import extractcores.DefaultConfigValues;
import extractcores.ImageProcessor;
import static extractcores.ImageProcessor.appendFilename;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import java.awt.Color;
import java.awt.Graphics2D;
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

  private static int infoImageCount = 0;
  private static final int INITIAL_MODEL_POINTS = 3;

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
    SimpleGridSolver simpleSolver = new SimpleGridSolver(cores, labelInformation, edgeFileName);
    simpleSolver.assignCores();
    AssignmentInformation assignmentInformation = simpleSolver.
            getAssignmentInformation();

    System.out.println("Geometric K-Means Solver:");
    System.out.println("Initialize Models.");
    
    initialize(assignmentInformation);

    createAssignmentInformation();

    int iterations = 5;
    for (int i = 0; i < iterations; i++)
    {
      reassignData();
      updateModels();
      System.out.println("Reassign data. Update models. Iteration #" + (i+1) + ".");

      createAssignmentInformation();
    }

  }

  private void initialize(AssignmentInformation simplestAssignmentInformation)
  {
    //TODO How to avoid "overfitted" curves?? Should always have low-angle curves.
    for (int i = 0; i < modelCount; i++)
    {
      List<Assignment> lowestInRow = simplestAssignmentInformation.
              getLowestInRow(i, INITIAL_MODEL_POINTS);
      for (Assignment assignment : lowestInRow)
      {
        geometricModel[i].addCore(cores.get(assignment.getCoreIndex()));
      }
    }
    updateModels();
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
        double distance = geometricModel[j].getDistance(core);
        if (geometricModel[j].getCoreCount() <= maxCoreCount && distance < lowestDistance)
        {
          lowestDistance = distance;
          lowestModelIndex = j;
        }
      }
      if (lowestModelIndex == -1)
      {
        int asdasd = 3;
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
    
    for (int i = 0; i < modelCount; i++)
    {
      double[] coefficients = geometricModel[i].getCoefficients();
      if (coefficients.length == 3)
      {
        //TODO draw polynomial curve with efficient function if possible
        int[] curvePointsX = new int[edgeImage.getWidth()];
        int[] curvePointsY = new int[edgeImage.getWidth()];
        for (int x = 0; x < edgeImage.getWidth(); x++)
        {
          int y = (int) (coefficients[2] * Math.pow(x, 2) + coefficients[1] * x
                  + coefficients[0]);
          curvePointsX[x] = x;
          curvePointsY[x] = y;
        }
        g.drawPolyline(curvePointsX, curvePointsY, curvePointsX.length);

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
