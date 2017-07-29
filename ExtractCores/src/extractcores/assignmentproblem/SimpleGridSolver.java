package extractcores.assignmentproblem;

import extractcores.CoreLabel;
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

  @Override
  public List<TissueCore> createLabeledCores()
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
      CoreLabel coreLabel = labelInformation.getCoreLabel(indices[0], indices[1]);
      if (coreLabel != null)
      {
        cores.get(i).setLabel(coreLabel);
        System.out.println("Distance cost: " + lowestValue + 
                ". Assigned label '" + coreLabel.name() + "' to Core #" + i);
      }
    }

    createAssignmentInformation(resultIndices);

    return cores;
  }
}
