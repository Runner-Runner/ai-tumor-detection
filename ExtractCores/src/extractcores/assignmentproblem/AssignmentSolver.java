package extractcores.assignmentproblem;

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

  public abstract List<TissueCore> createLabeledCores();

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
