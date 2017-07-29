package extractcores.assignmentproblem;

import extractcores.CoreComparator;
import static extractcores.CoreComparator.CompareType.HORIZONTAL_LEFT;
import static extractcores.CoreComparator.CompareType.HORIZONTAL_RIGHT;
import static extractcores.CoreComparator.CompareType.VERTICAL_BOTTOM;
import static extractcores.CoreComparator.CompareType.VERTICAL_CENTER;
import static extractcores.CoreComparator.CompareType.VERTICAL_UP;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import java.util.Collections;
import java.util.List;

public abstract class AssignmentSolver
{
  protected List<TissueCore> cores;
  protected LabelInformation labelInformation;
  protected String edgeFileName;
  
  protected int minX;
  protected int maxX;
  protected int minY;
  protected int maxY;
  protected double intervalWidth;
  protected double intervalHeight;

  public AssignmentSolver(List<TissueCore> cores, 
          LabelInformation labelInformation, String edgeFileName)
  {
    this.cores = cores;
    this.labelInformation = labelInformation;
    this.edgeFileName = edgeFileName;
  }

  public abstract List<TissueCore> createLabeledCores();

  protected void calculateGridData()
  {
    int columnCount = labelInformation.getColumnCount();
    int rowCount = labelInformation.getRowCount();

    CoreComparator coreComparator = new CoreComparator();
    coreComparator.setComparisonType(HORIZONTAL_LEFT);
    Collections.sort(cores, coreComparator);
    TissueCore leftestCore = cores.get(0);
    coreComparator.setComparisonType(HORIZONTAL_RIGHT);
    Collections.sort(cores, coreComparator);
    TissueCore rightestCore = cores.get(cores.size() - 1);
    coreComparator.setComparisonType(VERTICAL_UP);
    Collections.sort(cores, coreComparator);
    TissueCore topCore = cores.get(0);
    coreComparator.setComparisonType(VERTICAL_BOTTOM);
    Collections.sort(cores, coreComparator);
    TissueCore bottomCore = cores.get(cores.size() - 1);

    coreComparator.setComparisonType(VERTICAL_CENTER);
    Collections.sort(cores, coreComparator);

    minX = leftestCore.getBoundingBox().x;
    maxX = rightestCore.getBoundingBox().x
            + rightestCore.getBoundingBox().width;
    minY = topCore.getBoundingBox().y;
    maxY = bottomCore.getBoundingBox().y
            + bottomCore.getBoundingBox().height;

    intervalWidth = Double.valueOf(maxX - minX) / columnCount;
    intervalHeight = Double.valueOf(maxY - minY) / rowCount;
  }

  protected abstract void createAssignmentInformation(int[] resultIndices);

  protected static double calculateCost(TissueCore tissueCore, double gridCellCenterX,
          double gridCellCenterY)
  {
    double xDiff = Math.abs(tissueCore.getCenterX() - gridCellCenterX);
    double yDiff = Math.abs(tissueCore.getCenterY() - gridCellCenterY);
    return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
  }
}
