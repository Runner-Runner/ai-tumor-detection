package extractcores;

public class LabelInformation
{
  private CoreLabel[][] coreLabelArray;
  private int rowCount;
  private int columnCount;
  private int tumorCount;
  private int normalCount;
  private int gapCount;
  private int coreCount;
  private int digitKey;

  public LabelInformation(int digitKey, CoreLabel[][] coreLabelArray, int rowCount, 
          int columnCount, int tumorCount, int normalCount, int gapCount)
  {
    this.coreLabelArray = coreLabelArray;
    this.rowCount = rowCount;
    this.columnCount = columnCount;
    this.tumorCount = tumorCount;
    this.normalCount = normalCount;
    this.gapCount = gapCount;
    this.coreCount = rowCount * columnCount - gapCount;
  }

  public CoreLabel getCoreLabel(int r, int c)
  {
    if (r >= rowCount || c >= columnCount)
    {
      return null;
    }
    return coreLabelArray[r][c];
  }

  public int getColumnCount()
  {
    return columnCount;
  }

  public int getRowCount()
  {
    return rowCount;
  }

  public int getTumorCount()
  {
    return tumorCount;
  }

  public int getNormalCount()
  {
    return normalCount;
  }

  public int getGapCount()
  {
    return gapCount;
  }

  public int getCoreCount()
  {
    return coreCount;
  }

  public int getDigitKey()
  {
    return digitKey;
  }
}
