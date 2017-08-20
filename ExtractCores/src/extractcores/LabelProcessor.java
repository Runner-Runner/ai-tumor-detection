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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.StringUtil;

public class LabelProcessor
{
  public void writeTxtLabelFile(String pathName, String svsFileName)
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
    Pattern digitPattern = Pattern.compile("^(\\d+)[^\\d]*");
    Matcher matcher = digitPattern.matcher(svsFileName);
    matcher.find();
    String digitKey = matcher.group(1);

    String xlsPostfix = crossref.getProperty(digitKey);

    if (xlsPostfix == null)
    {
      System.out.println("Could not find XLS name for image file '"
              + digitKey + "'");
      return;
    }

    if (!(new File(pathName + DefaultConfigValues.XLS_PREFIX
            + xlsPostfix + ".XLS")).exists())
    {
      System.out.println("Could not find XLS Label File for image file '"
              + digitKey + "': " + pathName + DefaultConfigValues.XLS_PREFIX
              + xlsPostfix + ".XLS");
      return;
    }

    FileInputStream fileStream;
    try
    {
      fileStream = new FileInputStream(pathName + DefaultConfigValues.XLS_PREFIX
              + xlsPostfix + ".XLS");
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

    String[][] labelArray;

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
        if (cell != null && !cell.getStringCellValue().isEmpty())
        {
          realRowCount++;
        }
      }

      //Repeat with columns, skip first row
      int realColumnCount = 0;
      int firstFilledRowIndex = 1;
      while (realColumnCount == 0)
      {
        HSSFRow filledRow = sheet.getRow(firstFilledRowIndex);
        for (int i = 0; i < columnCount; i++)
        {
          HSSFCell cell = filledRow.getCell(i);

          try
          {
            if (cell != null && !cell.getStringCellValue().isEmpty())
            {
              realColumnCount++;
            }
          }
          catch (IllegalStateException ex)
          {
            //Probably numeric cell ... -> skip
          }

        }
        firstFilledRowIndex++;
      }
      if (realColumnCount == 0)
      {
        System.out.println("Could not determine real column count from xls. "
                + "Cannot create label file.");
        return;
      }

      labelArray = new String[realRowCount][realColumnCount];

      //Run through the whole rows now and store label values
      List<Integer> skipColumnIndices = new ArrayList<>();
      int realRowIndex = -1;
      outer:
      for (int i = 1; i < rowCount; i++)
      {
        realRowIndex++;
        HSSFRow hssfRow = sheet.getRow(i);
        int specificColumnCount = hssfRow.getPhysicalNumberOfCells();

        int realColumnIndex = -1;
        for (int j = 0; j < specificColumnCount; j++)
        {
          realColumnIndex++;

          HSSFCell cell = hssfRow.getCell(j);
          String stringCellValue;
          try
          {
            stringCellValue = cell == null ? "" : cell.getStringCellValue();
          }
          catch (IllegalStateException ex)
          {
            //Probably numeric cell ... -> skip
            stringCellValue = "";
          }

          if (stringCellValue.isEmpty())
          {
            //Skip empty rows
            if (j == 0)
            {
              realRowIndex--;
              continue outer;
            }
            //Skip empty columns (compare with empty cells in second row)
            else if (i == 1)
            {
              skipColumnIndices.add(j);
              realColumnIndex--;
              continue;
            }
            else if (skipColumnIndices.contains(j))
            {
              realColumnIndex--;
              continue;
            }

            //Otherwise, empty value in row with values in previous cells? 
            //-> Treat as gap
            stringCellValue = "gap";
          }
          labelArray[realRowIndex][realColumnIndex] = stringCellValue;
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
          default:
            break;
        }
      }
    }

    File outputFile = new File(pathName + digitKey + "-label.txt");
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
        CoreLabel coreLabel;
        if (labelText.startsWith("N")
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
        else if (labelText.startsWith("W")
                || labelText.startsWith("C"))
        {
          coreLabel = CoreLabel.CONTROL;
        }
        else
        {
          coreLabel = CoreLabel.UNDEFINED;
        }

        coreLabelArray[i][j] = coreLabel;
      }
    }
    return coreLabelArray;
  }

  public LabelInformation readTxtLabelFile(int digitKey)
  {
    try
    {
      String pathName = DefaultConfigValues.FILE_PATH_LABEL;
      String labelName = digitKey + "-label.txt";
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

      LabelInformation labelInformation = new LabelInformation(digitKey,
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
