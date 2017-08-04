package extractcores.assignmentproblem;

import extractcores.TissueCore;
import java.util.ArrayList;
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
  
  public abstract double[] getCoefficients();
  
  public abstract double getDistance(TissueCore core);
  
  public abstract void updateModel();

}
