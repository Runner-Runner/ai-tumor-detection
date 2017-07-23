package extractcores.assignmentproblem;

import extractcores.CoreComparator;
import static extractcores.CoreComparator.CompareType.HORIZONTAL_LEFT;
import static extractcores.CoreComparator.CompareType.HORIZONTAL_RIGHT;
import static extractcores.CoreComparator.CompareType.VERTICAL_BOTTOM;
import static extractcores.CoreComparator.CompareType.VERTICAL_CENTER;
import static extractcores.CoreComparator.CompareType.VERTICAL_UP;
import extractcores.DefaultConfigValues;
import extractcores.ImageProcessor;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public abstract class AssignmentSolver
{
  protected List<TissueCore> cores;
  protected LabelInformation labelInformation;

  protected int minX;
  protected int maxX;
  protected int minY;
  protected int maxY;
  protected double intervalWidth;
  protected double intervalHeight;

  public AssignmentSolver(List<TissueCore> cores, LabelInformation labelInformation)
  {
    this.cores = cores;
    this.labelInformation = labelInformation;
  }

  public abstract List<TissueCore> createLabeledCores();

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

  protected void createAssignmentInformation(int[] resultIndices)
  {
    //TODO Make file name generic
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage edgeImage = imageProcessor.readImage(
            DefaultConfigValues.FILE_PATH_EDGE,
            DefaultConfigValues.SAMPLE_IMAGE_EDGE);
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

    g.setColor(Color.RED);
    Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL, 0, new float[]
            {
              9
            }, 0);
    g.setStroke(dashed);

    for (int i = 0; i < cores.size(); i++)
    {
      TissueCore core = cores.get(i);
      
      int gridIndex = resultIndices[i];
      if(gridIndex == 0)
      {
        continue;
      }
      int rowIndex = gridIndex / labelInformation.getRowCount();
      int columnIndex = gridIndex % labelInformation.getRowCount();
      int cellCenterX = (int)(minX + columnIndex * intervalWidth + radiusWidth);
      int cellCenterY = (int)(minY + rowIndex * intervalHeight + radiusHeight);
      
      g.drawLine(core.getCenterX(), core.getCenterY(), cellCenterX, cellCenterY);
    }

    g.dispose();
    imageProcessor.writeImage(edgeImage,
            DefaultConfigValues.FILE_PATH_INFORMATIVE, "assignment-test.png");
  }

  protected static double calculateCost(TissueCore tissueCore, double gridCellCenterX,
          double gridCellCenterY)
  {
    double xDiff = Math.abs(tissueCore.getCenterX() - gridCellCenterX);
    double yDiff = Math.abs(tissueCore.getCenterY() - gridCellCenterY);
    return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
  }
}
