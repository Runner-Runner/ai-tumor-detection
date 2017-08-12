package extractcores;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import statistics.StatisticsWriter;

public class Main
{
  public static void main(String[] args) throws IOException
  {
    processImages();
  }

  public static void processImages()
  {
    File directory = new File(DefaultConfigValues.FILE_PATH_IMAGE_DRIVE);
    String[] svsList = directory.list();
    for (String fileName : svsList)
    {
      try
      {
        int digitKey = Integer.parseInt(fileName.split("\\.")[0]);
        System.out.println("Processing digit key = " + digitKey + " ...");
        processImage(digitKey);
        System.out.println("Processed digit key = " + digitKey + ".");
      }
      catch (Exception ex)
      {
        System.out.println("Error while processing file " + fileName);
        System.out.println(ex.getMessage());
      }
    }

    StatisticsWriter.getInstance().write();
  }

  public static void processImage(int digitKey)
  {
    createDownsampleImage(digitKey);
    createEdgeImage(digitKey);
    createLabelFile(digitKey);
    findCores(digitKey);
  }

  public static void createLabelFile(int digitKey)
  {
    LabelProcessor labelProcessor = new LabelProcessor();
    labelProcessor.writeTxtLabelFile(DefaultConfigValues.FILE_PATH_LABEL,
            digitKey + "-ds.png");
    System.out.println(digitKey + ": Created label file.");
  }

  public static void createEdgeImage(int digitKey)
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage sourceImage = imageProcessor.readImage(
            DefaultConfigValues.FILE_PATH_DOWNSAMPLE, digitKey + "-ds.png");
    BufferedImage edgeImage = imageProcessor.createEdgeImage(sourceImage);
    imageProcessor.writeImage(edgeImage, DefaultConfigValues.FILE_PATH_EDGE,
            digitKey + "-edge.png");
    System.out.println(digitKey + ": Created edge image.");
  }

  public static void createDownsampleImage(int digitKey)
  {
    File svsFile = new File("E:\\HE_TMA\\" + digitKey + ".svs");
    if(svsFile.exists())
    {
      return;
    }
    
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage dsImage = imageProcessor.downsampleImage(
            "E:\\HE_TMA\\", digitKey + ".svs",
            DefaultConfigValues.DOWNSAMPLE_FACTOR_X,
            DefaultConfigValues.DOWNSAMPLE_FACTOR_Y);
    imageProcessor.writeImage(dsImage, DefaultConfigValues.FILE_PATH_DOWNSAMPLE,
            digitKey + "-ds.png");
    System.out.println(digitKey + ": Created downsampled image.");
  }

  public static void findCores(int digitKey)
  {
    ImageProcessor imageProcessor = new ImageProcessor();

    BufferedImage edgeImage = imageProcessor.readImage(
            DefaultConfigValues.FILE_PATH_EDGE, digitKey + "-edge.png");

    CoreExtractor coreExtractor = new CoreExtractor();
    coreExtractor.findCores(DefaultConfigValues.FILE_PATH_EDGE,
            digitKey + "-edge.png", digitKey + "-label.txt", digitKey);
    System.out.println(digitKey + ": Cores processed.");
  }

  public static void createLabelIdentifyingImages()
  {
    String dirPath = "E:\\HE_TMA 2\\";
    File directory = new File(dirPath);
    String[] imgList = directory.list(
            (File dir, String name)
            -> name.substring(name.lastIndexOf(".")).toLowerCase().equals(".svs"));
    ImageProcessor imageProcessor = new ImageProcessor();
    for (String fileName : imgList)
    {
      imageProcessor.createLabelIdentifyingImage(dirPath, fileName);
    }
  }

  public static void createEdgeImages()
  {
    String dirPath = "E:\\ds_output\\";
    File directory = new File(dirPath);
    String[] imgList = directory.list();
    ImageProcessor imageProcessor = new ImageProcessor();
    for (String fileName : imgList)
    {
      try
      {
        BufferedImage sourceImage = imageProcessor.readImage(
                DefaultConfigValues.FILE_PATH_IMAGE_DRIVE, fileName);
        BufferedImage edgeImage = imageProcessor.createEdgeImage(sourceImage);
        String[] split = fileName.split("\\.");
        imageProcessor.writeImage(edgeImage, dirPath, split[0] + "-edge.png");

        System.out.println("Success: wrote edge image " + fileName);
      }
      catch (Exception ex)
      {
        System.out.println("Error while processing " + fileName);
      }
    }
  }

  public static void downsampleSvsFiles()
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    File directory = new File(DefaultConfigValues.FILE_PATH_IMAGE_DRIVE);
    String[] svsList = directory.list();
    for (String fileName : svsList)
    {
      try
      {
        BufferedImage downsampleImage = imageProcessor.downsampleImage(
                DefaultConfigValues.FILE_PATH_IMAGE_DRIVE, fileName,
                DefaultConfigValues.DOWNSAMPLE_FACTOR_X,
                DefaultConfigValues.DOWNSAMPLE_FACTOR_Y);
        String[] split = fileName.split("\\.");
        imageProcessor.writeImage(downsampleImage,
                DefaultConfigValues.FILE_PATH_IMAGE_DRIVE, split[0] + "-ds.png");

        System.out.println("Success: wrote downsampled image " + fileName);
      }
      catch (Exception ex)
      {
        System.out.println("Error while processing " + fileName);
      }
    }
  }
}
