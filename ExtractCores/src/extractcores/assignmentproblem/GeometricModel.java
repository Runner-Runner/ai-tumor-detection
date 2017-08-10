package extractcores.assignmentproblem;

import extractcores.CoreComparator;
import extractcores.TissueCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GeometricModel
{
  protected List<TissueCore> assignedCores;

  public GeometricModel()
  {
    assignedCores = new ArrayList<>();
  }

  public void addCore(TissueCore core)
  {
    assignedCores.add(core);
  }

  public void addCores(List<TissueCore> cores)
  {
    assignedCores.addAll(cores);
  }

  public boolean removeCore(TissueCore core)
  {
    return assignedCores.remove(core);
  }

  public void clearCores()
  {
    assignedCores.clear();
  }

  public int getCoreCount()
  {
    return assignedCores.size();
  }

  public TissueCore getCore(int i)
  {
    return assignedCores.get(i);
  }

  public List<TissueCore> getAssignedCores()
  {
    CoreComparator coreComparator = new CoreComparator();
    coreComparator.setComparisonType(CoreComparator.CompareType.HORIZONTAL_CENTER);
    Collections.sort(assignedCores, coreComparator);
    return assignedCores;
  }

  public abstract double[] getCoefficients();

  public abstract void setCoefficients(double[] coefficients);

  public abstract double getDistance(TissueCore core);

  public abstract double getClosenessCost(TissueCore core, double intervalWidth,
          double intervalHeight);

  public abstract TissueCore getXOverlappingCore(TissueCore core);

  public abstract void updateModel();

  public abstract List<TissueCore> removeOutliers();

  public abstract int getY(int x);
}
