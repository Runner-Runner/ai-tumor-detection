package extractcores;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main
{
  public static void main(String[] args) throws IOException
  {
    findCores();
  }

  public static void createLabelFile()
  {
    LabelProcessor labelProcessor = new LabelProcessor();
    labelProcessor.writeTxtLabelFile(DefaultConfigValues.FILE_PATH_LABEL, 
            DefaultConfigValues.SAMPLE_XLS_LABEL);
  }
  
  public static void createEdgeImage()
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage sourceImage = imageProcessor.readImage(
            DefaultConfigValues.FILE_PATH_DOWNSAMPLE, DefaultConfigValues.SAMPLE_IMAGE_DS);
    BufferedImage edgeImage = imageProcessor.createEdgeImage(sourceImage);
    imageProcessor.writeImage(edgeImage, DefaultConfigValues.FILE_PATH_EDGE, 
            DefaultConfigValues.SAMPLE_IMAGE_EDGE);
  }
  
  public static void createDownsampleImage()
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage dsImage = imageProcessor.downsampleImage
        ("E:\\HE_TMA\\", "39387.svs", DefaultConfigValues.DOWNSAMPLE_FACTOR_X, 
                DefaultConfigValues.DOWNSAMPLE_FACTOR_Y);
    imageProcessor.writeImage(dsImage, DefaultConfigValues.FILE_PATH_DOWNSAMPLE, 
            "39387-ds.png");
  }

  public static void findCores()
  {
    ImageProcessor imageProcessor = new ImageProcessor();

    BufferedImage edgeImage = imageProcessor.readImage(
            DefaultConfigValues.FILE_PATH_EDGE, DefaultConfigValues.SAMPLE_IMAGE_EDGE);

    CoreExtractor coreExtractor = new CoreExtractor();
    coreExtractor.findCores(DefaultConfigValues.FILE_PATH_EDGE, 
            DefaultConfigValues.SAMPLE_IMAGE_EDGE, DefaultConfigValues.SAMPLE_LABEL);
  }
  
  public static void createEdgeImages()
  {
    String dirPath = "E:\\ds_output\\";
    ImageProcessor imageProcessor = new ImageProcessor();
    File directory = new File(dirPath);
    String[] imgList = directory.list();
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
