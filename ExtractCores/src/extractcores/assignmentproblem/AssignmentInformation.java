package extractcores.assignmentproblem;

import extractcores.TissueCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssignmentInformation
{
  private List<Assignment> coreAssignments;

  public AssignmentInformation()
  {
    coreAssignments = new ArrayList<>();
  }

  public void addAssignment(int row, int column, TissueCore core, Integer distance)
  {
    coreAssignments.add(new Assignment(row, column, core, distance));
    Collections.sort(coreAssignments);
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
  
  public int getSize()
  {
    return coreAssignments.size();
  }
}
