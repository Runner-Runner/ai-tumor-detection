package extractcores.assignmentproblem;

import extractcores.LabelInformation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssignmentInformation
{
  private Integer[][] coreIndices;
  private Integer[][] coreDistances;
  private List<Assignment> coreAssignments;

  public AssignmentInformation(LabelInformation labelInformation)
  {
    coreAssignments = new ArrayList<>();

//    coreIndices = new Integer[labelInformation.getRowCount()][labelInformation.getColumnCount()];
//    coreDistances = new Integer[labelInformation.getRowCount()][labelInformation.getColumnCount()];
  }

  public void addAssignment(int row, int column, int coreIndex, int distance)
  {
    coreAssignments.add(new Assignment(row, column, coreIndex, distance));
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
}
