package extractcores;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.StringUtil;

public class LabelProcessor
{

  private static final String DEFAULT_LABEL_PATH = "..\\data\\labels\\";
  private static final String CROSSREF_FILE_NAME = "crossref.properties";

  private String labelPath = DEFAULT_LABEL_PATH;

  public void writeLabelFile(String fileName)
  {
    Properties crossref = new Properties();
    InputStream propStream;
    try
    {
      propStream = new FileInputStream(DEFAULT_LABEL_PATH + CROSSREF_FILE_NAME);
      crossref.load(propStream);
    }
    catch (IOException ex)
    {
      System.out.println("crossref properties file could not be found/loaded.");
      return;
    }

    //Extract key from last part of filename
    String[] split = fileName.split(" ");
    String labelKey = split[2].split("\\.")[0];
    String correspondingSvsName = crossref.getProperty(labelKey);

    FileInputStream fileStream;
    try
    {
      fileStream = new FileInputStream(labelPath + fileName);
    }
    catch (FileNotFoundException ex)
    {
      System.out.println("Error while reading label file.");
      System.out.println(ex.getMessage());
      return;
    }

    String[][] labelArray = null;

    try
    {
      POIFSFileSystem fs = new POIFSFileSystem(fileStream);
      HSSFWorkbook wb = new HSSFWorkbook(fs);
      HSSFSheet sheet = wb.getSheetAt(0);

      int rowCount = sheet.getPhysicalNumberOfRows();
      //Assuming rectangular label arrays
      int columnCount = sheet.getRow(1).getPhysicalNumberOfCells();

      //First, run through rows, check for empty rows, count "real" rows
      //Skip first row containing title
      int realRowCount = 0;
      for (int i = 1; i < rowCount; i++)
      {
        HSSFRow hssfRow = sheet.getRow(i);
        HSSFCell cell = hssfRow.getCell(0);
        if(!cell.getStringCellValue().isEmpty())
        {
          realRowCount++;
        }
      }
      
      labelArray = new String[realRowCount][columnCount];
      
      //Run through the whole rows now and store label values
      int realRowIndex = -1;
      outer:
      for (int i = 1; i < rowCount; i++)
      {
        realRowIndex++;
        HSSFRow hssfRow = sheet.getRow(i);
        int specificColumnCount = hssfRow.getPhysicalNumberOfCells();

        for (int j = 0; j < specificColumnCount; j++)
        {
          HSSFCell cell = hssfRow.getCell(j);
          String stringCellValue = cell.getStringCellValue();
          //Skip empty rows
          if(cell.getStringCellValue().isEmpty())
          {
            realRowIndex--;
            continue outer;
          }
          labelArray[realRowIndex][j] = stringCellValue;
        }

      }
    }
    catch (IOException ex)
    {
      System.out.println("Error while reading label (xls) file.");
      System.out.println(ex.getMessage());
      return;
    }

    CoreLabel[][] coreLabelArray = convertLabelArray(labelArray);

    File outputFile = new File(
            DEFAULT_LABEL_PATH + correspondingSvsName + "-label.txt");
    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
      
      for(int i=0; i<coreLabelArray.length; i++)
      {
        CoreLabel[] row = coreLabelArray[i];
        String rowText = StringUtil.join(row, ",");
        writer.write(rowText);
        
        if(i < coreLabelArray.length - 1)
        {
          writer.write("\n");
        }
      }
      
      writer.close();
    }
    catch (IOException ex)
    {
      System.out.println("Error while writing label (txt) file.");
      System.out.println(ex.getMessage());
    }
  }

  private CoreLabel[][] convertLabelArray(String[][] labelArray)
  {
    CoreLabel[][] coreLabelArray
            = new CoreLabel[labelArray.length][labelArray[0].length];
    for (int i = 0; i < labelArray.length; i++)
    {
      for (int j = 0; j < labelArray[0].length; j++)
      {
        String labelText = labelArray[i][j];
        CoreLabel coreLabel = null;
        if (labelText.startsWith("W") 
                || labelText.startsWith("N")
                || labelText.startsWith("C")
                || labelText.startsWith("A")
                || labelText.startsWith("B"))
        {
          coreLabel = CoreLabel.NORMAL;
        }
        else if(labelText.startsWith("T"))
        {
          coreLabel = CoreLabel.TUMOR;
        }
        else if(labelText.startsWith("gap")
                || labelText.startsWith("O"))
        {
          coreLabel = CoreLabel.GAP;
        }
        else
        {
          coreLabel = CoreLabel.GAP;
        }

        coreLabelArray[i][j] = coreLabel;
      }
    }
    return coreLabelArray;
  }
}
