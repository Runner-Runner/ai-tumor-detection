package extractcores.assignmentproblem;

public class Assignment implements Comparable<Assignment>
{
  private int row;
  private int column;
  private Integer coreIndex;
  private Integer distanceCost;

  public Assignment(int row, int column, Integer coreIndex, Integer distanceCost)
  {
    this.row = row;
    this.column = column;
    this.coreIndex = coreIndex;
    this.distanceCost = distanceCost;
  }

  @Override
  public int compareTo(Assignment other)
  {
    return this.getDistanceCost() - other.getDistanceCost();
  }

  public int getRow()
  {
    return row;
  }

  public int getColumn()
  {
    return column;
  }

  public Integer getCoreIndex()
  {
    return coreIndex;
  }

  public int getDistanceCost()
  {
    return distanceCost;
  }
}
