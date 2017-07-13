package extractcores;

import java.util.Comparator;

public class CoreComparator implements Comparator<TissueCore>
{
  private boolean horizontalComparison = true;
  
  @Override
  public int compare(TissueCore core1, TissueCore core2)
  {
    if(horizontalComparison)
    {
      return core1.getCenterX() - core2.getCenterX();
    }
    return core1.getCenterY() - core2.getCenterY();
  }

  public void setHorizontalComparison(boolean horizontalComparison)
  {
    this.horizontalComparison = horizontalComparison;
  }
}
