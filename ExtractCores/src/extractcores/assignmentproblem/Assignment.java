package extractcores.assignmentproblem;

import extractcores.TissueCore;

public class Assignment implements Comparable<Assignment>
{
  private int row;
  private int column;
  private TissueCore core;
  private Integer distanceCost;

  public Assignment(int row, int column, TissueCore core, Integer distanceCost)
  {
    this.row = row;
    this.column = column;
    this.core = core;
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

  public TissueCore getCore()
  {
    return core;
  }
  
  public Integer getDistanceCost()
  {
    return distanceCost;
  }
}
