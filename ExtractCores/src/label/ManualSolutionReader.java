package label;

import static extractcores.DefaultConfigValues.FILE_PATH_LABEL_SOLUTION;
import extractcores.LabelInformation;
import extractcores.LabelProcessor;
import extractcores.TissueCore;
import extractcores.assignmentproblem.AssignmentInformation;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ManualSolutionReader
{
  public AssignmentInformation readSolution(int digitKey)
  {
    AssignmentInformation assignmentInformation = new AssignmentInformation();
    
    LabelProcessor labelProcessor = new LabelProcessor();
    LabelInformation labelInformation = labelProcessor.readTxtLabelFile(digitKey);
    
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(
              FILE_PATH_LABEL_SOLUTION + digitKey + "-solution.txt"));
      String line = null;
      int rowCount = 0;
      while((line = reader.readLine()) != null)
      {
        String[] splitLine = line.split(",");
        if(splitLine.length != labelInformation.getColumnCount())
        {
          System.err.println("Column count in row #" + (rowCount+1) + 
                  " doesn't match label file! Label file: " 
                + labelInformation.getColumnCount() + " columns. Solution file: " 
                + splitLine.length + " columns.");
        }
        
        for(int c=0; c<splitLine.length; c++)
        {
          String cellValue = splitLine[c];
          if(cellValue.contains("+"))
          {
            String[] splitCell = cellValue.split("\\+");
            int[] coreIds = new int[splitCell.length];
            for(int i=0; i<coreIds.length; i++)
            {
              coreIds[i] = Integer.parseInt(splitCell[i]);
            }
            TissueCore dummyCore = new TissueCore(-1, -1, -1, -1);
            dummyCore.setIds(coreIds);
            assignmentInformation.addAssignment(rowCount, c, dummyCore, null);
          }
          else if(!cellValue.contains("g"))
          {
            TissueCore dummyCore = new TissueCore(-1, -1, -1, -1);
            dummyCore.setIds(Integer.parseInt(cellValue));
            assignmentInformation.addAssignment(rowCount, c, dummyCore, null);
          }
        }
        
        rowCount++;
      }
      
      if(rowCount != labelInformation.getRowCount())
      {
        System.err.println("Row count doesn't match label file! Label file: " 
                + labelInformation.getRowCount() + " rows. Solution file: " 
                + rowCount + " rows.");
      }
      
      return assignmentInformation;
    }
    catch (IOException ex)
    {
      System.out.println("Could not read label solution file with digit key = "
              + digitKey + ".");
      System.out.println(ex.getMessage());
      return null;
    }
  }
}
