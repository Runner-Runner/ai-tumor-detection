package extractcores.assignmentproblem;

import extractcores.CoreLabel;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import java.util.List;

public abstract class AssignmentSolver
{
  protected List<TissueCore> cores;
  protected LabelInformation labelInformation;
  protected String edgeFileName;

  protected AssignmentInformation assignmentInformation;

  public AssignmentSolver(List<TissueCore> cores,
          LabelInformation labelInformation, String edgeFileName)
  {
    this.cores = cores;
    this.labelInformation = labelInformation;
    this.edgeFileName = edgeFileName;
  }

  public AssignmentInformation getAssignmentInformation()
  {
    return assignmentInformation;
  }

  public List<TissueCore> createLabeledCores()
  {
    for (int r = 0; r < labelInformation.getRowCount(); r++)
    {
      for (int c = 0; c < labelInformation.getColumnCount(); c++)
      {
        Assignment assignment = assignmentInformation.getAssignment(r, c);
        if (assignment != null)
        {
          TissueCore core = assignment.getCore();
          CoreLabel coreLabel = labelInformation.getCoreLabel(r, c);
          if (coreLabel != null)
          {
            core.setLabel(coreLabel);
            System.out.println("Label [" + r + "/" + c + "] = "
                    + coreLabel.name() + "assigned to Core #" + core.getId());
          }
        }
      }
    }
    return cores;
  }

  //TODO remove indices, separate "create assignment data" and "assign labels"
  protected abstract void createAssignmentInformation();

  protected static double calculateCost(TissueCore tissueCore, double gridCellCenterX,
          double gridCellCenterY)
  {
    double xDiff = Math.abs(tissueCore.getCenterX() - gridCellCenterX);
    double yDiff = Math.abs(tissueCore.getCenterY() - gridCellCenterY);
    return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
  }
}
