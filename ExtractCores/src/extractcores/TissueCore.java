package extractcores;

import java.awt.Rectangle;
import java.util.List;

public class TissueCore
{
  private Rectangle boundingBox;
  private int centerX;
  private int centerY;

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

  public boolean intersects(TissueCore otherCore)
  {
    return getBoundingBox().intersects(otherCore.getBoundingBox());
  }
  
  @Override
  public String toString()
  {
    return "(" + centerX + "/" + centerY + ")";
  }
  
  public static TissueCore union(List<TissueCore> cores)
  {
    if(cores.size() == 1)
    {
      return cores.get(0);
    }
    
    Rectangle unionBoundingBox = new Rectangle(-1, -1);
    for(TissueCore core :cores)
    {
      unionBoundingBox = unionBoundingBox.union(core.getBoundingBox());
    }
    return new TissueCore(unionBoundingBox.x, unionBoundingBox.y, 
            unionBoundingBox.width, unionBoundingBox.height);
  }
}
