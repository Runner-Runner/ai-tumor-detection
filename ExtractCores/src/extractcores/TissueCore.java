package extractcores;

import org.opencv.core.Rect;

public class TissueCore implements Comparable<TissueCore> {
  private Rect boundingBox;
  private int centerX;
  private int centerY;
  
  public TissueCore(Rect boundingBox)
  {
    this.boundingBox = boundingBox;
    centerX = (int)(boundingBox.x + Double.valueOf(boundingBox.width)/2);
    centerY = (int)(boundingBox.y + Double.valueOf(boundingBox.height)/2);
  }

  public Rect getBoundingBox()
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
  public int compareTo(TissueCore otherCore)
  {
    //Wie nach vers. Kriterien sortierbar machen??
  }
}
