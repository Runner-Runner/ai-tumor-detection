package statistics;

import extractcores.LabelInformation;
import java.util.ArrayList;
import java.util.List;

public class Statistic {
  private int digitKey;
  private LabelInformation labelInformation;
  private int imageWidth;
  private int imageHeight;
  private List<Integer> coreWidths;
  private List<Integer> coreHeights;

  public Statistic()
  {
    coreWidths = new ArrayList<>();
    coreHeights = new ArrayList<>();
  }
  
  public void setDigitKey(int digitKey)
  {
    this.digitKey = digitKey;
  }
  
  public int getDigitKey()
  {
    return digitKey;
  }

  public LabelInformation getLabelInformation()
  {
    return labelInformation;
  }

  public void setLabelInformation(LabelInformation labelInformation)
  {
    this.labelInformation = labelInformation;
  }

  public int getImageWidth()
  {
    return imageWidth;
  }

  public void setImageWidth(int imageWidth)
  {
    this.imageWidth = imageWidth;
  }

  public int getImageHeight()
  {
    return imageHeight;
  }

  public void setImageHeight(int imageHeight)
  {
    this.imageHeight = imageHeight;
  }

  public int getAvgCoreWidth()
  {
    int avgCoreWidth = 0;
    for(Integer width : coreWidths)
    {
      avgCoreWidth += width;
    }
    avgCoreWidth /= coreWidths.size();
    return avgCoreWidth;
  }

  public void addCoreWidth(int coreWidth)
  {
    coreWidths.add(coreWidth);
  }

  public int getAvgCoreHeight()
  {
    int avgCoreHeight = 0;
    for(Integer height : coreHeights)
    {
      avgCoreHeight += height;
    }
    avgCoreHeight /= coreHeights.size();
    return avgCoreHeight;
  }

  public void addCoreHeight(int coreHeight)
  {
    coreHeights.add(coreHeight);
  }
  
  
}
