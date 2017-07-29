package extractcores.assignmentproblem;

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
import java.util.List;

public abstract class GridSolver extends AssignmentSolver
{
  public GridSolver(List<TissueCore> cores, LabelInformation labelInformation,
          String edgeFileName)
  {
    super(cores, labelInformation, edgeFileName);
  }

  @Override
  protected void createAssignmentInformation(int[] resultIndices)
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
      if (gridIndex == 0)
      {
        continue;
      }
      int[] indices = get2dIndices(gridIndex);
      int cellCenterX = (int) (minX + indices[1] * intervalWidth + radiusWidth);
      int cellCenterY = (int) (minY + indices[0] * intervalHeight + radiusHeight);

      g.drawLine(core.getCenterX(), core.getCenterY(), cellCenterX, cellCenterY);
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

  protected int[] get2dIndices(int index)
  {
    int rowIndex = index / labelInformation.getColumnCount();
    int columnIndex = index % labelInformation.getColumnCount();
    return new int[]
    {
      rowIndex, columnIndex
    };
  }

}
