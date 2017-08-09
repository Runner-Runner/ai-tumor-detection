package label;

import static extractcores.DefaultConfigValues.FILE_PATH_LABEL_SOLUTION;
import extractcores.LabelInformation;
import extractcores.LabelProcessor;
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
          System.out.println("Column count in row #" + (rowCount+1) + 
                  " doesn't match label file! Label file: " 
                + labelInformation.getColumnCount() + " columns. Solution file: " 
                + splitLine.length + " rows.");
        }
        
        for(int c=0; c<splitLine.length; c++)
        {
          String cellValue = splitLine[c];
          if(cellValue.contains("+"))
          {
            //TODO separate
            String[] splitCell = cellValue.split("+");
            if(splitCell.length != 2)
            {
              System.out.println("Wrong format: '" + cellValue + "'");
            }
            else
            {
              //TODO
              //splitCell[0]
            }
          }
          else if(!cellValue.contains("g"))
          {
            assignmentInformation.addAssignment(rowCount, c, 
                    Integer.parseInt(cellValue), null);
          }
        }
        
        rowCount++;
      }
      
      if(rowCount != labelInformation.getRowCount())
      {
        System.out.println("Row count doesn't match label file! Label file: " 
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
