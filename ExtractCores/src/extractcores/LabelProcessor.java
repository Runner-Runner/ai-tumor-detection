package extractcores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.StringUtil;

public class LabelProcessor
{
  public void writeTxtLabelFile(String pathName, String fileName)
  {
    Properties crossref = new Properties();
    InputStream propStream;
    try
    {
      propStream = new FileInputStream(pathName + DefaultConfigValues.CROSSREF_FILE_NAME);
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
      fileStream = new FileInputStream(pathName + fileName);
    }
    catch (FileNotFoundException ex)
    {
      System.out.println("Error while reading label file.");
      System.out.println(ex.getMessage());
      return;
    }

    int tumorCount = 0;
    int normalCount = 0;
    int gapCount = 0;

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
        if (!cell.getStringCellValue().isEmpty())
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
          if (cell.getStringCellValue().isEmpty())
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

    for (int i = 0; i < coreLabelArray.length; i++)
    {
      for (int j = 0; j < coreLabelArray[0].length; j++)
      {
        switch (coreLabelArray[i][j])
        {
          case TUMOR:
            tumorCount++;
            break;
          case NORMAL:
            normalCount++;
            break;
          case GAP:
            gapCount++;
            break;
        }
      }
    }

    File outputFile = new File(pathName + correspondingSvsName + "-label.txt");
    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

      //Convenience: store array dimensions
      writer.write(coreLabelArray.length + "," + coreLabelArray[0].length
              + "," + tumorCount + "," + normalCount + "," + gapCount + "\n");

      for (int i = 0; i < coreLabelArray.length; i++)
      {
        CoreLabel[] row = coreLabelArray[i];
        String rowText = StringUtil.join(row, ",");
        writer.write(rowText);

        if (i < coreLabelArray.length - 1)
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
        else if (labelText.startsWith("T"))
        {
          coreLabel = CoreLabel.TUMOR;
        }
        else if (labelText.startsWith("gap")
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

  public LabelInformation readTxtLabelFile(String pathName, String labelName)
  {
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(pathName
              + labelName));

      String sizeLine = reader.readLine();
      if (sizeLine == null)
      {
        return null;
      }
      String[] utilityValues = sizeLine.split(",");
      int rows = Integer.parseInt(utilityValues[0]);
      int columns = Integer.parseInt(utilityValues[1]);
      int tumorCount = Integer.parseInt(utilityValues[2]);
      int normalCount = Integer.parseInt(utilityValues[3]);
      int gapCount = Integer.parseInt(utilityValues[4]);
      CoreLabel[][] coreLabelArray = new CoreLabel[rows][columns];

      String line;
      int i = 0;
      while ((line = reader.readLine()) != null)
      {
        String[] cellValueArray = line.split(",");
        for (int j = 0; j < cellValueArray.length; j++)
        {
          coreLabelArray[i][j] = CoreLabel.valueOf(cellValueArray[j]);
        }
        i++;
      }

      LabelInformation labelInformation = new LabelInformation(
              coreLabelArray, rows, columns, tumorCount, normalCount, gapCount);
      return labelInformation;
    }
    catch (IOException ex)
    {
      System.out.println("Could not read label file.");
      System.out.println(ex.getMessage());
      return null;
    }
  }
}
