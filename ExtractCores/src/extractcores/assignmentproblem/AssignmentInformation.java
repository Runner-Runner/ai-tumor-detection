package extractcores.assignmentproblem;

import extractcores.TissueCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssignmentInformation
{
  private List<Assignment> coreAssignments;
  private List<int[]> mergedCoreIds;
  private int digitKey;
  private int gapCount = 0;

  public AssignmentInformation(int digitKey)
  {
    this.digitKey = digitKey;
    coreAssignments = new ArrayList<>();
    mergedCoreIds = new ArrayList<>();
  }

  public void addAssignment(int row, int column, TissueCore core, Integer distance)
  {
    coreAssignments.add(new Assignment(row, column, core, distance));
    Collections.sort(coreAssignments);
    
    int[] ids = core.getIds();
    if(ids.length > 1)
    {
      mergedCoreIds.add(ids);
    }
  }

  public void setGapCount(int gapCount)
  {
    this.gapCount = gapCount;
  }

  public int getGapCount()
  {
    return gapCount;
  }
  
  public List<Assignment> getLowestInRow(int row, int n)
  {
    //list is sorted by distance on default
    List<Assignment> lowestAssignments = new ArrayList<>();
    for (Assignment assignment : coreAssignments)
    {
      if(assignment.getRow() == row)
      {
        lowestAssignments.add(assignment);
        if(lowestAssignments.size() >= n)
        {
          break;
        }
      }
    }
    return lowestAssignments;
  }

  public Assignment getAssignment(int r, int c)
  {
    for(Assignment assignment : coreAssignments)
    {
      if(assignment.getRow() == r && assignment.getColumn() == c)
      {
        return assignment;
      }
    }
    return null;
  }
  
  public Assignment getAssignment(int index)
  {
    return coreAssignments.get(index);
  }
  
  public int getSize()
  {
    return coreAssignments.size();
  }

  public int getDigitKey()
  {
    return digitKey;
  }

  public List<int[]> getMergedCoreIds()
  {
    return mergedCoreIds;
  }
}
