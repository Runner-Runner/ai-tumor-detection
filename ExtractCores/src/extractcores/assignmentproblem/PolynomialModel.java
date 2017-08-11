package extractcores.assignmentproblem;

import Jama.Matrix;
import extractcores.TissueCore;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class PolynomialModel extends GeometricModel
{
  private double cx2;
  private double cx;
  private double c;

  public PolynomialModel()
  {
    super();
    cx2 = 1;
    cx = 1;
    c = 0;
  }

  @Override
  public double getDistance(TissueCore core)
  {
    //Just use x difference for now. Should be accurate enough, as curves are 
    //max pol^2 and will rarely be steep.
    double x = core.getCenterX();
    double coreY = core.getCenterY();
    double curveY = cx2 * x * x + cx * x + c;

    double distance = Math.abs(coreY - curveY);
    //Square distances to favor distributed distances.
    return Math.pow(distance, 2);
  }

  @Override
  public double getClosenessCost(TissueCore core, double intervalWidth,
          double intervalHeight)
  {
    if (assignedCores.isEmpty())
    {
      return 0.0;
    }
    //Checks only horizontal distance for now.
    double closestCoreDistance = Double.MAX_VALUE;
    for (TissueCore otherCore : assignedCores)
    {
      double distance = Math.abs(core.getCenterX() - otherCore.getCenterX());
      if (distance < closestCoreDistance)
      {
        closestCoreDistance = distance;
      }
    }

    double closeness = Math.max(intervalWidth - closestCoreDistance, 0);
    double closenessFactor = closeness / intervalWidth;
    //TODO introduce steep function, especially to the full 100%!
    double closenessCost = closenessFactor * intervalHeight * 2;//
    return closenessCost;
  }

  @Override
  public TissueCore getXOverlappingCore(TissueCore core)
  {
    if (assignedCores.isEmpty())
    {
      return null;
    }
    for (TissueCore otherCore : assignedCores)
    {
      Rectangle boundingBox = core.getBoundingBox();
      Rectangle otherBoundingBox = otherCore.getBoundingBox();
      boolean noOverlap = boundingBox.x > otherBoundingBox.x + otherBoundingBox.width
              || boundingBox.x + boundingBox.width < otherBoundingBox.x;

      if (!noOverlap)
      {
        return otherCore;
      }
    }
    return null;
  }

  @Override
  public void updateModel()
  {
    if (assignedCores.isEmpty())
    {
      return;
    }

    double[][] fittingMatrix = new double[3][3];
    double[][] rightside = new double[3][1];

    //Create linear equation system
    double xSum = 0;
    double x2Sum = 0;
    double x3Sum = 0;
    double x4Sum = 0;
    double ySum = 0;
    double xySum = 0;
    double x2ySum = 0;
    for (int i = 0; i < assignedCores.size(); i++)
    {
      TissueCore core = assignedCores.get(i);
      int x = core.getCenterX();
      int y = core.getCenterY();

      xSum += x;
      x2Sum += Math.pow(x, 2);
      x3Sum += Math.pow(x, 3);
      x4Sum += Math.pow(x, 4);
      ySum += y;
      xySum += x * y;
      x2ySum += Math.pow(x, 2) * y;
    }
    fittingMatrix[0][0] = assignedCores.size();
    fittingMatrix[0][1] = xSum;
    fittingMatrix[0][2] = x2Sum;
    fittingMatrix[1][0] = xSum;
    fittingMatrix[1][1] = x2Sum;
    fittingMatrix[1][2] = x3Sum;
    fittingMatrix[2][0] = x2Sum;
    fittingMatrix[2][1] = x3Sum;
    fittingMatrix[2][2] = x4Sum;

    rightside[0][0] = ySum;
    rightside[1][0] = xySum;
    rightside[2][0] = x2ySum;

    Matrix lhsMatrix = new Matrix(fittingMatrix);
    Matrix rhsMatrix = new Matrix(rightside);

    Matrix solution = lhsMatrix.solve(rhsMatrix);
    c = solution.get(0, 0);
    cx = solution.get(1, 0);
    cx2 = solution.get(2, 0);
  }

  @Override
  public double[] getCoefficients()
  {
    return new double[]
    {
      cx2, cx, c
    };
  }

  @Override
  public void setCoefficients(double[] coefficients)
  {
    cx2 = coefficients[0];
    cx = coefficients[1];
    c = coefficients[2];
  }

  @Override
  public List<TissueCore> removeOutliers()
  {
    List<TissueCore> outliers = new ArrayList<>();

    double distanceMean = 0;
    double[] distances = new double[assignedCores.size()];
    for (int i = 0; i < assignedCores.size(); i++)
    {
      TissueCore core = assignedCores.get(i);
      if (core.getId() == 242)
      {
        int a = 3;
      }

      distanceMean += distances[i] = getDistance(core);
    }
    distanceMean /= assignedCores.size();

    double sum = 0;
    for (int i = 0; i < distances.length; i++)
    {
      sum += Math.pow(distances[i] - distanceMean, 2);
    }
    double distanceStandardDeviation = Math.sqrt(sum / (distances.length - 1));

    for (int i = 0; i < distances.length; i++)
    {
      double deviation = Math.abs(distances[i] - distanceStandardDeviation);

      //TODO this still doesnt work to well. How to detect these extreme outliers?
      if (deviation > distanceStandardDeviation)
      {
        outliers.add(assignedCores.get(i));
      }
    }
    assignedCores.removeAll(outliers);
    return outliers;
  }

  @Override
  public int getY(int x)
  {
    int y = (int) (cx2 * Math.pow(x, 2) + cx * x + c);
    return y;
  }
}
