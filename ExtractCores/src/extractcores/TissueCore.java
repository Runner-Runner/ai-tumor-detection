package extractcores;

import java.awt.Rectangle;
import java.util.List;

public class TissueCore
{
  private Rectangle boundingBox;
  private int centerX;
  private int centerY;
public static int aaa = 0;
  public TissueCore(int x, int y, int width, int height)
  {
    boundingBox = new Rectangle(x, y, width, height);
    centerX = (int) (x + Double.valueOf(width) / 2);
    centerY = (int) (y + Double.valueOf(height) / 2);
  }

  public Rectangle getBoundingBox()
  {
    return boundingBox;
  }

  public int getCenterX()
  {
    return centerX;
  }

public int getCenterY()
  {
    return centerY;
  }

  @Override
  public String toString()
  {
    return "(" + centerX + "/" + centerY + ")";
  }

  public boolean intersects(TissueCore otherCore)
  {
    return getBoundingBox().intersects(otherCore.getBoundingBox());
  }

  public static TissueCore union(List<TissueCore> cores)
  {
    if (cores.size() == 1)
    {
      return cores.get(0);
    }

    Rectangle unionBoundingBox = new Rectangle(-1, -1);
    for (TissueCore core : cores)
    {
      unionBoundingBox = unionBoundingBox.union(core.getBoundingBox());
    }
    return new TissueCore(unionBoundingBox.x, unionBoundingBox.y,
            unionBoundingBox.width, unionBoundingBox.height);
  }

  /**
   * Compared rectangles must not intersect.
   *
   * @param otherCore
   * @return
   */
  public boolean closeTo(TissueCore otherCore)
  {
    Rectangle otherBoundingBox = otherCore.getBoundingBox();

    boolean left = boundingBox.getMinX() > otherBoundingBox.getMaxX();
    boolean right = boundingBox.getMaxX() < otherBoundingBox.getMinX();
    boolean top = boundingBox.getMinY() > otherBoundingBox.getMaxY();
    boolean bottom = boundingBox.getMaxY() < otherBoundingBox.getMinY();

    double sideDistance;
    
    if(left && !top && !bottom)
    {
      sideDistance = boundingBox.getMinX() - otherBoundingBox.getMaxX();
    }
    else if(right && !top && !bottom)
    {
      sideDistance = otherBoundingBox.getMinX() - boundingBox.getMaxX();
    }
    else if(top && !left && !right)
    {
      sideDistance = boundingBox.getMinY() - otherBoundingBox.getMaxY();
    }
    else if(bottom && !left && !right)
    {
      sideDistance = otherBoundingBox.getMinY() - boundingBox.getMaxY();
    }
    else
    {
      return false;
    }

    return sideDistance <= DefaultConfigValues.MIN_CORE_SIDE_DISTANCE;
  }
}
