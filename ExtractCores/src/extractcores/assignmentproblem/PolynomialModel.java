package extractcores.assignmentproblem;

import Jama.LUDecomposition;
import Jama.Matrix;
import extractcores.TissueCore;

public class PolynomialModel extends GeometricModel
{
  private double cx2;
  private double cx;
  private double c;

  public PolynomialModel()
  {
    super();
    //TODO initial values?
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
    return distance;
  }

  @Override
  public void updateModel()
  {
    if(assignedCores.isEmpty())
    {
      //TODO That shouldn't really happen ...
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

    //TODO Test lib function. Where to find solved coefficients
    if (!(new LUDecomposition(lhsMatrix)).isNonsingular())
    {
      int asdasd = 3;
    }
    else
    {
      int asd= 3;
    }
    Matrix solution = lhsMatrix.solve(rhsMatrix);
    cx2 = solution.get(0,0);
    cx = solution.get(1,0);
    c = solution.get(2,0);
  }

  @Override
  public double[] getCoefficients()
  {
    return new double[]
    {
      cx2, cx, c
    };
  }

}
