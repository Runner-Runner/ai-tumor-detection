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

  public LabelInformation(CoreLabel[][] coreLabelArray, int rowCount, 
          int columnCount, int tumorCount, int normalCount, int gapCount)
  {
    this.coreLabelArray = coreLabelArray;
    this.rowCount = rowCount;
    this.columnCount = columnCount;
    this.tumorCount = tumorCount;
    this.normalCount = normalCount;
    this.gapCount = gapCount;
    this.coreCount = tumorCount + normalCount;
  }

  public CoreLabel[][] getCoreLabelArray()
  {
    return coreLabelArray;
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
}
