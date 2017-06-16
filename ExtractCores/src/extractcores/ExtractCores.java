package extractcores;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ExtractCores
{
  public static void main(String[] args) throws IOException
  {
//    createSingleEdgeImage();
    findCores();
  }

  public static void createSingleEdgeImage()
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    BufferedImage sourceImage = imageProcessor.readImage(
            DefaultPaths.FILE_PATH_DOWNSAMPLE, DefaultPaths.SAMPLE_IMAGE_DS);
    BufferedImage edgeImage = imageProcessor.createEdgeImage(sourceImage);
    imageProcessor.writeImage(edgeImage, DefaultPaths.FILE_PATH_EDGE, 
            DefaultPaths.SAMPLE_IMAGE_EDGE);
  }

  public static void findCores()
  {
    ImageProcessor imageProcessor = new ImageProcessor();

    BufferedImage edgeImage = imageProcessor.readImage(
            DefaultPaths.FILE_PATH_EDGE, DefaultPaths.SAMPLE_IMAGE_EDGE);

    CoreExtractor coreExtractor = new CoreExtractor();
    coreExtractor.findCores(DefaultPaths.FILE_PATH_EDGE, 
            DefaultPaths.SAMPLE_IMAGE_EDGE, DefaultPaths.SAMPLE_LABEL);
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
                DefaultPaths.FILE_PATH_IMAGE_DRIVE, fileName);
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
    File directory = new File(DefaultPaths.FILE_PATH_IMAGE_DRIVE);
    String[] svsList = directory.list();
    for (String fileName : svsList)
    {
      try
      {
        BufferedImage downsampleImage = imageProcessor.downsampleImage(
                DefaultPaths.FILE_PATH_IMAGE_DRIVE, fileName, 50, 50);
        String[] split = fileName.split("\\.");
        imageProcessor.writeImage(downsampleImage, 
                DefaultPaths.FILE_PATH_IMAGE_DRIVE, split[0] + "-ds.png");

        System.out.println("Success: wrote downsampled image " + fileName);
      }
      catch (Exception ex)
      {
        System.out.println("Error while processing " + fileName);
      }
    }
  }
}
