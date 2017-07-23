package extractcores.assignmentproblem;

import extractcores.LabelInformation;
import extractcores.TissueCore;
import java.util.List;

public abstract class GridSolver extends AssignmentSolver
{
  public GridSolver(List<TissueCore> cores, LabelInformation labelInformation)
  {
    super(cores, labelInformation);
  }

  protected int[][] createCostMatrix()
  {
    double radiusWidth = intervalWidth / 2;
    double radiusHeight = intervalHeight / 2;

    int rowCount = cores.size();
    int columnCount = labelInformation.getRowCount() * labelInformation.
            getColumnCount();
    int[][] inputMatrix = new int[rowCount][columnCount];

    for (int r = 0; r < rowCount; r++)
    {
      TissueCore core = cores.get(r);

      for (int c = 0; c < columnCount; c++)
      {
        int rowIndex = c / labelInformation.getRowCount();
        int columnIndex = c % labelInformation.getRowCount();
        int cellCenterX = (int) (minX + columnIndex * intervalWidth + radiusWidth);
        int cellCenterY = (int) (minY + rowIndex * intervalHeight + radiusHeight);
        inputMatrix[r][c] = (int) calculateCost(core, cellCenterX, cellCenterY);
      }
    }
    return inputMatrix;
  }

}
