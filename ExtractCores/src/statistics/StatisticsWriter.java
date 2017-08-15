package statistics;

import static extractcores.DefaultConfigValues.FILE_PATH_INFORMATIVE;
import static extractcores.DefaultConfigValues.STATISTICS_FILE_NAME;
import extractcores.LabelInformation;
import extractcores.assignmentproblem.AssignmentInformation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatisticsWriter
{
  private static StatisticsWriter statisticsWriter;

  private List<Statistic> statistics;

  private List<AssignmentInformation> solutionAssignmentList;

  private int rows;
  private int columns;
  private int cores;
  private int gaps;
  private int totalCores;
  private int totalGaps;
  private int totalTumorCores;
  private int totalNormalCores;
  private int imageWidth;
  private int imageHeight;
  private int avgCoreWidth;
  private int avgCoreHeight;

  private int solutionTotalCoreCount = 0;
  private int detectedCoreCount = 0;

  private int solutionMergeCount = 0;
  private int correctMergeCount = 0;

  private int correctAssignCoreCountGrid = 0;
  private int correctAssignCoreCountCurve = 0;
  private int solutionCoreCount = 0;
  private int correctAssignGapCountGrid = 0;
  private int correctAssignGapCountCurve = 0;
  private int solutionGapCount = 0;

  private StatisticsWriter()
  {
    statistics = new ArrayList<>();
    solutionAssignmentList = new ArrayList<>();
  }

  public static StatisticsWriter getInstance()
  {
    if (statisticsWriter == null)
    {
      statisticsWriter = new StatisticsWriter();
    }
    return statisticsWriter;
  }

  public void addStatistic(Statistic statistic)
  {
    statistics.add(statistic);
  }

  public void addSolution(AssignmentInformation assignmentInformation)
  {
    solutionAssignmentList.add(assignmentInformation);
  }

  public void addDetectionStats(int detectedCoreCount)
  {
    this.detectedCoreCount += detectedCoreCount;
  }
  
  public void addMergeStats(int correctMergeCount, int solutionMergeCount)
  {
    this.correctMergeCount += correctMergeCount;
    this.solutionMergeCount += solutionMergeCount;
  }

  public void addAssignStats(int solutionCoreCount, int solutionGapCount)
  {
    this.solutionCoreCount += solutionCoreCount;
    this.solutionGapCount += solutionGapCount;
  }

  public void addAssignStats(int correctAssignCoreCount,
          int correctAssignGapCount, boolean grid)
  {
    if (grid)
    {
      this.correctAssignCoreCountGrid += correctAssignCoreCount;
      this.correctAssignGapCountGrid += correctAssignGapCount;
    }
    else
    {
      this.correctAssignCoreCountCurve += correctAssignCoreCount;
      this.correctAssignGapCountCurve += correctAssignGapCount;
    }
  }

  private void calculateAvg()
  {
    if (statistics.isEmpty())
    {
      return;
    }

    for (Statistic statistic : statistics)
    {
      LabelInformation labelInformation = statistic.getLabelInformation();
      rows += labelInformation.getRowCount();
      columns += labelInformation.getColumnCount();
      totalCores += labelInformation.getCoreCount();
      totalGaps += labelInformation.getGapCount();
      totalTumorCores += labelInformation.getTumorCount();
      totalNormalCores += labelInformation.getNormalCount();
      imageWidth += statistic.getImageWidth();
      imageHeight += statistic.getImageHeight();
      avgCoreWidth += statistic.getAvgCoreWidth();
      avgCoreHeight += statistic.getAvgCoreHeight();
    }

    int caseCount = statistics.size();
    rows /= caseCount;
    columns /= caseCount;
    cores = totalCores / caseCount;
    gaps = totalGaps / caseCount;
    imageWidth /= caseCount;
    imageHeight /= caseCount;
    avgCoreWidth /= caseCount;
    avgCoreHeight /= caseCount;

    //Process stored solutions
    for (AssignmentInformation assignmentInformation : solutionAssignmentList)
    {
      solutionTotalCoreCount += assignmentInformation.getSize();
    }
  }

  public void write()
  {
    if (statistics.isEmpty())
    {
      return;
    }

    calculateAvg();

    File outputFile = new File(
            FILE_PATH_INFORMATIVE + STATISTICS_FILE_NAME + ".txt");
    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

      int caseCount = statistics.size();
      String keysText = "";
      for (Statistic statistic : statistics)
      {
        keysText += statistic.getDigitKey() + " ";
      }

      writer.write("Overall statistics:\n");
      writer.write("Cases: " + caseCount + ". Digit keys: " + keysText + "\n");
      writer.write("Avg. nr. of rows: " + rows + "\n");
      writer.write("Avg. nr. of columns: " + columns + "\n");
      writer.write("Total nr. of cores: " + totalCores + "\n");
      writer.write("Avg. nr. of cores: " + cores + "\n");
      writer.write("Total nr. of gaps: " + totalGaps + "\n");
      writer.write("Avg. nr. of gaps: " + gaps + "\n");
      writer.write("Avg. image width: " + imageWidth + "\n");
      writer.write("Avg. image height: " + imageHeight + "\n");
      writer.write("Avg. core width: " + avgCoreWidth + "\n");
      writer.write("Avg. core height: " + avgCoreHeight + "\n");
      writer.write("Total nr. of tumor cores: " + totalTumorCores + "\n");
      writer.write("Total nr. of normal cores: " + totalNormalCores + "\n\n");

      double coreDetectionPercentage1
              = Double.valueOf(detectedCoreCount) / totalCores * 100;
      writer.write("Detected cores (by label files): " + detectedCoreCount
              + "/" + totalCores
              + ", " + String.format("%.2f", coreDetectionPercentage1) + "%.\n");
      double coreDetectionPercentage2
              = Double.valueOf(detectedCoreCount) / solutionTotalCoreCount * 100;
      writer.write("Detected cores (by ground truth): " + detectedCoreCount
              + "/" + solutionTotalCoreCount
              + ", " + String.format("%.2f", coreDetectionPercentage2) + "%.\n");

      double mergePercentage
              = Double.valueOf(correctMergeCount) / solutionMergeCount * 100;
      writer.write("Correctly merged cores: " + correctMergeCount
              + "/" + solutionMergeCount
              + ", " + String.format("%.2f", mergePercentage) + "%.\n");

      double assignCorePercentageGrid
              = Double.valueOf(correctAssignCoreCountGrid) / solutionCoreCount * 100;
      writer.write("Grid: Correctly assigned cores (ID-checked): " + correctAssignCoreCountGrid
              + "/" + solutionCoreCount
              + ", " + String.format("%.2f", assignCorePercentageGrid) + "%.\n");
      double assignCorePercentageCurve
              = Double.valueOf(correctAssignCoreCountCurve) / solutionCoreCount * 100;
      writer.write("Curve: Correctly assigned cores (ID-checked): " + correctAssignCoreCountCurve
              + "/" + solutionCoreCount
              + ", " + String.format("%.2f", assignCorePercentageCurve) + "%.\n");

      double assignGapPercentageGrid
              = Double.valueOf(correctAssignGapCountGrid) / solutionGapCount * 100;
      writer.write("Grid: Correctly assigned gaps: " + correctAssignGapCountGrid
              + "/" + solutionGapCount
              + ", " + String.format("%.2f", assignGapPercentageGrid) + "%.\n");
      double assignGapPercentageCurve
              = Double.valueOf(correctAssignGapCountCurve) / solutionGapCount * 100;
      writer.write("Curve: Correctly assigned gaps: " + correctAssignGapCountCurve
              + "/" + solutionGapCount
              + ", " + String.format("%.2f", assignGapPercentageCurve) + "%.\n");

      writer.write("Statistics per image file:\n");
      for (Statistic statistic : statistics)
      {
        writer.write("Digit Key: " + statistic.getDigitKey() + "\n");
        writer.write("Image: w=" + statistic.getImageWidth() + ", h="
                + statistic.getImageHeight() + "\n");
        writer.write("Cores: avg dim=" + statistic.getAvgCoreWidth()
                + "*" + statistic.getAvgCoreHeight() + "\n");
      }

      writer.write("");

      writer.close();
    }
    catch (IOException ex)
    {
      System.out.println("Error while writing statistics file.");
      System.out.println(ex.getMessage());
    }
  }
}
