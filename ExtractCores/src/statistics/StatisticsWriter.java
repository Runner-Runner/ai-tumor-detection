package statistics;

import static extractcores.DefaultConfigValues.FILE_PATH_INFORMATIVE;
import static extractcores.DefaultConfigValues.STATISTICS_FILE_NAME;
import extractcores.LabelInformation;
import extractcores.TissueCore;
import extractcores.assignmentproblem.Assignment;
import extractcores.assignmentproblem.AssignmentInformation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsWriter
{
  private static StatisticsWriter statisticsWriter;

  private List<Statistic> statistics;

  private Map<Integer, AssignmentInformation> solutionAssignmentList;

  private int rows;
  private int columns;
  private int labelAvgCores;
  private int labelAvgGaps;
  private int labelTotalCores;
  private int labelTotalGaps;
  private int labelTotalTumorCores;
  private int labelTotalNormalCores;
  private int imageWidth;
  private int imageHeight;
  private int avgCoreWidth;
  private int avgCoreHeight;

  private int foundRelevantCount = 0;
  private int foundIrrelevantCount = 0;
  private int notFoundRelevantCount = 0;
  private int notFoundIrrelevantCount = 0;
  private int solutionCoreCount = 0;
  private int solutionGapCount = 0;

  private int solutionMergeCount = 0;
  private int correctMergeCount = 0;

  private int correctAssignCoreCountGrid = 0;
  private int correctAssignCoreCountCurve = 0;
  private int correctAssignGapCountGrid = 0;
  private int correctAssignGapCountCurve = 0;

  private StatisticsWriter()
  {
    statistics = new ArrayList<>();
    solutionAssignmentList = new HashMap<>();
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
    solutionAssignmentList.put(assignmentInformation.getDigitKey(), 
            assignmentInformation);
  }

  public void addDetectionStats(int digitKey, List<TissueCore> detectedCores, 
          int discardedObjectCount)
  {
    AssignmentInformation solution = solutionAssignmentList.get(digitKey);
    if(solution == null)
    {
      return;
    }
    List<Integer> solutionCoreIds = new ArrayList<>();
    for(int i=0; i<solution.getSize(); i++)
    {
      TissueCore core = solution.getAssignment(i).getCore();
      if(core != null)
      {
        solutionCoreIds.add(core.getId());
      }
    }
            
    for(TissueCore core : detectedCores)
    {
      int id = core.getId();
      if(solutionCoreIds.contains(id))
      {
        foundRelevantCount++;
        solutionCoreIds.remove((Integer)id);
      }
      else
      {
        foundIrrelevantCount++;
      }
    }
    notFoundRelevantCount += solutionCoreIds.size();
    notFoundIrrelevantCount += discardedObjectCount;
  }
  
  public void addMergeStats(int correctMergeCount, int solutionMergeCount)
  {
    this.correctMergeCount += correctMergeCount;
    this.solutionMergeCount += solutionMergeCount;
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
      labelTotalCores += labelInformation.getCoreCount();
      labelTotalGaps += labelInformation.getGapCount();
      labelTotalTumorCores += labelInformation.getTumorCount();
      labelTotalNormalCores += labelInformation.getNormalCount();
      imageWidth += statistic.getImageWidth();
      imageHeight += statistic.getImageHeight();
      avgCoreWidth += statistic.getAvgCoreWidth();
      avgCoreHeight += statistic.getAvgCoreHeight();
    }

    int caseCount = statistics.size();
    rows /= caseCount;
    columns /= caseCount;
    labelAvgCores = labelTotalCores / caseCount;
    labelAvgGaps = labelTotalGaps / caseCount;
    imageWidth /= caseCount;
    imageHeight /= caseCount;
    avgCoreWidth /= caseCount;
    avgCoreHeight /= caseCount;
  }
  
  private void calcSolutions()
  {
    solutionCoreCount += foundRelevantCount + notFoundRelevantCount;
    for(AssignmentInformation assignmentInformation : solutionAssignmentList.values())
    {
      solutionGapCount += assignmentInformation.getGapCount();
    }
  }
  
  public void write()
  {
    if (statistics.isEmpty())
    {
      return;
    }

    calculateAvg();
    calcSolutions();
    
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
      writer.write("Avg. image width: " + imageWidth + "\n");
      writer.write("Avg. image height: " + imageHeight + "\n");
      writer.write("Avg. core width: " + avgCoreWidth + "\n");
      writer.write("Avg. core height: " + avgCoreHeight + "\n\n");
      
      writer.write("Based on label file data:\n");
      writer.write("Total nr. of cores: " + labelTotalCores + "\n");
      writer.write("Avg. nr. of cores: " + labelAvgCores + "\n");
      writer.write("Total nr. of gaps: " + labelTotalGaps + "\n");
      writer.write("Avg. nr. of gaps: " + labelAvgGaps + "\n");
      writer.write("Total nr. of tumor cores: " + labelTotalTumorCores + "\n");
      writer.write("Total nr. of normal cores: " + labelTotalNormalCores + "\n\n");
      
      writer.write("Based on solution file data:\n");
      writer.write("Total nr. of cores: " + solutionCoreCount + "\n");
      writer.write("Total nr. of gaps: " + solutionGapCount + "\n");
      
      writer.write("Detected cores (by ground truth): \n");
      writer.write("Detected relevant objects: " + foundRelevantCount + "\n");
      writer.write("Detected irrelevant objects: " + foundIrrelevantCount + "\n");
      writer.write("Not detected relevant objects: " + notFoundRelevantCount + "\n");
      writer.write("Not detected irrelevant objects: " + notFoundIrrelevantCount + "\n");
      double corePrecision
              = Double.valueOf(foundRelevantCount) / (foundRelevantCount + foundIrrelevantCount) * 100;
      writer.write("Precision: " + String.format("%.2f", corePrecision) + "%.\n");
      double coreRecall
              = Double.valueOf(foundRelevantCount) / (solutionCoreCount) * 100;
      writer.write("Recall: " + String.format("%.2f", coreRecall) + "%.\n\n");
      
      int missingCores = labelTotalCores - solutionCoreCount;
      double missingPercent = Double.valueOf(missingCores) / labelTotalCores * 100;
      writer.write("Nr. of cores worn out (cf. label <-> solution): " + 
              missingCores + ". " + String.format("%.2f", missingPercent) + 
              "% of cores from label file are missing.\n\n");

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
