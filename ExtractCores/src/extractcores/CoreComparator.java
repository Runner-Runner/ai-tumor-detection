package extractcores;

import java.util.Comparator;

public class CoreComparator implements Comparator<TissueCore>
{

  private CompareType compareType = CompareType.HORIZONTAL_CENTER;

  @Override
  public int compare(TissueCore core1, TissueCore core2)
  {
    int compareResult;
    switch (compareType)
    {
      case HORIZONTAL_LEFT:
        compareResult = core1.getBoundingBox().x - core2.getBoundingBox().x;
        break;
      case HORIZONTAL_RIGHT:
        compareResult = core1.getBoundingBox().x + core1.getBoundingBox().width
                - core2.getBoundingBox().x + core2.getBoundingBox().width;
        break;
      case VERTICAL_UP:
        compareResult = core1.getBoundingBox().y - core2.getBoundingBox().y;
        break;
      case VERTICAL_BOTTOM:
        compareResult = core1.getBoundingBox().y + core1.getBoundingBox().height
                - core2.getBoundingBox().y + core2.getBoundingBox().height;
        break;
      case VERTICAL_CENTER:
        compareResult = core1.getCenterY() - core2.getCenterY();
        break;
      case HORIZONTAL_CENTER:
      default:
        compareResult = core1.getCenterX() - core2.getCenterX();
        break;
    }
    return compareResult;
  }

  public void setComparisonType(CompareType compareType)
  {
    this.compareType = compareType;
  }

  public enum CompareType
  {
    HORIZONTAL_CENTER, VERTICAL_CENTER, HORIZONTAL_LEFT, HORIZONTAL_RIGHT,
    VERTICAL_UP, VERTICAL_BOTTOM
  };
}
