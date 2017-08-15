package extractcores.assignmentproblem;

import extractcores.CoreComparator;
import static extractcores.CoreComparator.CompareType.HORIZONTAL_LEFT;
import static extractcores.CoreComparator.CompareType.HORIZONTAL_RIGHT;
import static extractcores.CoreComparator.CompareType.VERTICAL_BOTTOM;
import static extractcores.CoreComparator.CompareType.VERTICAL_CENTER;
import static extractcores.CoreComparator.CompareType.VERTICAL_UP;
import extractcores.DefaultConfigValues;
import extractcores.ImageProcessor;
import static extractcores.ImageProcessor.appendFilename;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public abstract class GridSolver extends AssignmentSolver
{
  protected int minX;
  protected int maxX;
  protected int minY;
  protected int maxY;
  protected double intervalWidth;
  protected double intervalHeight;

  public GridSolver(List<TissueCore> cores, LabelInformation labelInformation,
          String edgeFileName)
  {
    super(cores, labelInformation, edgeFileName);
    assignmentInformation = new AssignmentInformation();
  }

  @Override
  protected void createAssignmentInformation()
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage edgeImage = imageProcessor.readImage(
            DefaultConfigValues.FILE_PATH_EDGE,
            edgeFileName);
    Graphics2D g = (Graphics2D) edgeImage.getGraphics();
    g.setColor(Color.LIGHT_GRAY);

    int x = minX;
    for (int i = 0; i <= labelInformation.getColumnCount(); i++)
    {
      g.drawLine(x, minY, x, maxY);
      x += intervalWidth;
    }
    int y = minY;
    for (int i = 0; i <= labelInformation.getRowCount(); i++)
    {
      g.drawLine(minX, y, maxX, y);
      y += intervalHeight;
    }

    double radiusWidth = intervalWidth / 2;
    double radiusHeight = intervalHeight / 2;

    g.setColor(Color.GREEN);
    Stroke stroke = new BasicStroke(3);
    g.setStroke(stroke);

    for (int r = 0; r < labelInformation.getRowCount(); r++)
    {
      for (int c = 0; c < labelInformation.getColumnCount(); c++)
      {
        Assignment assignment = assignmentInformation.getAssignment(r, c);
        if (assignment != null)
        {
          TissueCore core = assignment.getCore();

          int cellCenterX = (int) (minX + c * intervalWidth + radiusWidth);
          int cellCenterY = (int) (minY + r * intervalHeight + radiusHeight);

//          g.drawLine(core.getCenterX(), core.getCenterY(), cellCenterX, cellCenterY);
        }
      }
    }

    g.dispose();
    imageProcessor.writeImage(edgeImage,
            DefaultConfigValues.FILE_PATH_INFORMATIVE,
            appendFilename(edgeFileName, "assign", "png"));
  }

  protected int[][] createCostMatrix()
  {
    double radiusWidth = intervalWidth / 2;
    double radiusHeight = intervalHeight / 2;

    int coreCount = cores.size();
    int cellCount = labelInformation.getRowCount() * labelInformation.
            getColumnCount();
    int[][] inputMatrix = new int[coreCount][cellCount];

//    int maxCount = coreCount > cellCount ? coreCount : cellCount;
//    int[][] inputMatrix = new int[maxCount][maxCount];
//    for(int i=0; i<maxCount; i++)
//    {
//      for(int j=0; j<maxCount; j++)
//      {
//        inputMatrix[i][j] = Integer.MAX_VALUE;
//      }
//    }
    for (int r = 0; r < coreCount; r++)
    {
      TissueCore core = cores.get(r);

      for (int c = 0; c < cellCount; c++)
      {
        int[] indices = get2dIndices(c);
        int cellCenterX = (int) (minX + indices[1] * intervalWidth + radiusWidth);
        int cellCenterY = (int) (minY + indices[0] * intervalHeight + radiusHeight);
        inputMatrix[r][c] = (int) calculateCost(core, cellCenterX, cellCenterY);
      }
    }
    return inputMatrix;
  }

  protected void calculateGridData()
  {
    int columnCount = labelInformation.getColumnCount();
    int rowCount = labelInformation.getRowCount();

    CoreComparator coreComparator = new CoreComparator();
    coreComparator.setComparisonType(HORIZONTAL_LEFT);
    Collections.sort(cores, coreComparator);
    TissueCore leftestCore = cores.get(0);
    coreComparator.setComparisonType(HORIZONTAL_RIGHT);
    Collections.sort(cores, coreComparator);
    TissueCore rightestCore = cores.get(cores.size() - 1);
    coreComparator.setComparisonType(VERTICAL_UP);
    Collections.sort(cores, coreComparator);
    TissueCore topCore = cores.get(0);
    coreComparator.setComparisonType(VERTICAL_BOTTOM);
    Collections.sort(cores, coreComparator);
    TissueCore bottomCore = cores.get(cores.size() - 1);

    coreComparator.setComparisonType(VERTICAL_CENTER);
    Collections.sort(cores, coreComparator);

    minX = leftestCore.getBoundingBox().x;
    maxX = rightestCore.getBoundingBox().x
            + rightestCore.getBoundingBox().width;
    minY = topCore.getBoundingBox().y;
    maxY = bottomCore.getBoundingBox().y
            + bottomCore.getBoundingBox().height;

    intervalWidth = Double.valueOf(maxX - minX) / columnCount;
    intervalHeight = Double.valueOf(maxY - minY) / rowCount;
  }

  protected int[] get2dIndices(int index)
  {
    int rowIndex = index / labelInformation.getColumnCount();
    int columnIndex = index % labelInformation.getColumnCount();
    return new int[]
    {
      rowIndex, columnIndex
    };
  }

  public double getIntervalWidth()
  {
    return intervalWidth;
  }

  public double getIntervalHeight()
  {
    return intervalHeight;
  }

  public int getMinX()
  {
    return minX;
  }

  public int getMaxX()
  {
    return maxX;
  }

  public int getMinY()
  {
    return minY;
  }

  public int getMaxY()
  {
    return maxY;
  }
}
