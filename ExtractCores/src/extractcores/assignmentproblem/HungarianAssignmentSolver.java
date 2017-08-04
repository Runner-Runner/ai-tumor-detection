package extractcores.assignmentproblem;

import extractcores.CoreLabel;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import hungarian.Hungarian;
import java.util.Arrays;
import java.util.List;

public class HungarianAssignmentSolver extends GridSolver
{
  public HungarianAssignmentSolver(List<TissueCore> cores, 
          LabelInformation labelInformation, String edgeFileName)
  {
    super(cores, labelInformation, edgeFileName);
  }

  @Override
  public List<TissueCore> createLabeledCores()
  {
    calculateGridData();

    int[][] inputMatrix = createCostMatrix();

    System.out.println("Input Matrix Hungarian: " + Arrays.deepToString(inputMatrix));

    long startTime = System.currentTimeMillis();
    Hungarian hungarian = new Hungarian(inputMatrix);
    int[] result = hungarian.getResult();
//    HungarianDouble hungarianAlgorithm = new HungarianDouble(inputMatrix);
//    int[] result = hungarianAlgorithm.getResult();
    long runningtimeHungarian = System.currentTimeMillis() - startTime;
    System.out.println(String.format("Hungarian Algorithm - Running Time: %dms\n",
            runningtimeHungarian));

    System.out.println("Hungarian Result: " + Arrays.toString(result));

    for (int i = 0; i < cores.size(); i++)
    {
      int gridIndex = result[i];
      int rowIndex = gridIndex / labelInformation.getRowCount();
      int columnIndex = gridIndex % labelInformation.getRowCount();
      CoreLabel coreLabel = labelInformation.getCoreLabel(rowIndex, columnIndex);
      if (coreLabel != null)
      {
        cores.get(i).setLabel(coreLabel);
        System.out.println("Assigned label '" + coreLabel.name() + "' to Core #" + i);
      }
    }

    //TODO handle redundant cores
    //TODO check if hungarian algorithm does even improve on simple ... leave out if no time.

    createAssignmentInformation();

    return cores;
  }
}
