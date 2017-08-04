package extractcores.assignmentproblem;

import extractcores.CoreLabel;
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
  protected AssignmentInformation assignmentInformation;

  public GridSolver(List<TissueCore> cores, LabelInformation labelInformation,
          String edgeFileName)
  {
    super(cores, labelInformation, edgeFileName);
    assignmentInformation = new AssignmentInformation(labelInformation);
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

    g.setColor(Color.RED);
    Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL, 0, new float[]
            {
              9
            }, 0);
    g.setStroke(dashed);

    for (int r = 0; r < labelInformation.getRowCount(); r++)
    {
      for (int c = 0; c < labelInformation.getColumnCount(); c++)
      {
        Assignment assignment = assignmentInformation.getAssignment(r, c);
        if (assignment != null)
        {
          Integer coreIndex = assignment.getCoreIndex();
          if (coreIndex != null)
          {
            //TODO Test
            TissueCore core = cores.get(coreIndex);

            int cellCenterX = (int) (minX + c * intervalWidth + radiusWidth);
            int cellCenterY = (int) (minY + r * intervalHeight + radiusHeight);

            g.drawLine(core.getCenterX(), core.getCenterY(), cellCenterX, cellCenterY);
          }
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

  @Override
  public List<TissueCore> createLabeledCores()
  {
    for (int r = 0; r < labelInformation.getRowCount(); r++)
    {
      for (int c = 0; c < labelInformation.getColumnCount(); c++)
      {
        Assignment assignment = assignmentInformation.getAssignment(r, c);
        if (assignment != null)
        {
          Integer coreIndex = assignment.getCoreIndex();
          CoreLabel coreLabel = labelInformation.getCoreLabel(r, c);
          if (coreLabel != null && assignment.getCoreIndex() != null)
          {
            cores.get(coreIndex).setLabel(coreLabel);
            System.out.println("Label [" + r + "/" + c + "] = "
                    + coreLabel.name() + "assigned to Core #" + coreIndex);
          }
        }
      }
    }
    return cores;
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

  public AssignmentInformation getAssignmentInformation()
  {
    return assignmentInformation;
  }
}
