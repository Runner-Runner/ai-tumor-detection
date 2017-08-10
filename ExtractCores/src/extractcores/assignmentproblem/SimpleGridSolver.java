package extractcores.assignmentproblem;

import extractcores.LabelInformation;
import extractcores.TissueCore;
import java.util.List;

public class SimpleGridSolver extends GridSolver
{
  public SimpleGridSolver(List<TissueCore> cores,
          LabelInformation labelInformation, String edgeFileName)
  {
    super(cores, labelInformation, edgeFileName);
  }

  public void assignCores()
  {
    calculateGridData();
    int[][] costMatrix = createCostMatrix();

    int[] resultIndices = new int[cores.size()];
    for (int i = 0; i < cores.size(); i++)
    {
      int lowestValueIndex = 0;
      int lowestValue = Integer.MAX_VALUE;
      int[] coreCostRow = costMatrix[i];
      for (int j = 0; j < coreCostRow.length; j++)
      {
        if (coreCostRow[j] < lowestValue)
        {
          lowestValue = coreCostRow[j];
          lowestValueIndex = j;
        }
      }
      resultIndices[i] = lowestValueIndex;

      int[] indices = get2dIndices(lowestValueIndex);

      assignmentInformation.addAssignment(indices[0], indices[1], cores.get(i), lowestValue);
    }

    createAssignmentInformation();
  }
}
