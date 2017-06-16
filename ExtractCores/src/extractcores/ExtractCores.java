package extractcores;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ExtractCores
{
  private static final String ABS_SAMPLE_DS
          = "S:\\Meine Daten\\Schutzbereich\\MoS\\Master Thesis\\impl\\extract cores\\data\\sample\\5512-ds.jpg";

  private static final String ABS_SAMPLE_FULL_IMAGE
          = "S:\\Meine Daten\\Schutzbereich\\MoS\\Master Thesis\\impl\\extract cores\\data\\sample\\5512.svs";
  private static final String ABS_SAMPLE_FULL_IMAGE_DRIVE
          = "E:\\HE_TMA\\5512.svs";
  private static final String ABS_SAMPLE_FULL_IMAGE_UNIX = "/media/daniel/Storage/Meine Daten/Schutzbereich/MoS/Master Thesis/impl/extract cores/data/sample/5512.svs";

  private static final String SAMPLE_DS = "5512-ds.png";
  private static final String SAMPLE_SVS = "5512.svs";
  private static final String SAMPLE_LABEL = "TMA GC LT3.XLS";

  public static void main(String[] args) throws IOException
  {
//    createSingleEdgeImage();
    findCores();
  }

  public static void createSingleEdgeImage()
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    imageProcessor.setFilePath("..\\data\\ds output\\");
    BufferedImage sourceImage = imageProcessor.readImage(SAMPLE_DS);
    BufferedImage edgeImage = imageProcessor.createEdgeImage(sourceImage);
    imageProcessor.writeImage(edgeImage, "test.png");
  }

  public static void findCores()
  {
    ImageProcessor imageProcessor = new ImageProcessor();

    imageProcessor.setFilePath("..\\data\\edge output\\");
    BufferedImage edgeImage = imageProcessor.readImage("5512-ds-edge.png");

    CoreExtractor coreExtractor = new CoreExtractor();
    coreExtractor.findCores(edgeImage, "5512-label.txt");
  }
  
  public static void createEdgeImages()
  {
    String dirPath = "E:\\ds_output\\";
    ImageProcessor imageProcessor = new ImageProcessor();
    imageProcessor.setFilePath(dirPath);
    File directory = new File(dirPath);
    String[] imgList = directory.list();
    for (String fileName : imgList)
    {
      try
      {
        BufferedImage sourceImage = imageProcessor.readImage(fileName);
        BufferedImage edgeImage = imageProcessor.createEdgeImage(sourceImage);
        String[] split = fileName.split("\\.");
        imageProcessor.writeImage(edgeImage, split[0] + "-edge.png");

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
    String dirPath = "E:\\HE_TMA\\";
    ImageProcessor imageProcessor = new ImageProcessor();
    imageProcessor.setFilePath(dirPath);
    File directory = new File(dirPath);
    String[] svsList = directory.list();
    for (String fileName : svsList)
    {
      try
      {
        BufferedImage downsampleImage = imageProcessor.downsampleImage(fileName,
                50, 50);
        String[] split = fileName.split("\\.");
        imageProcessor.writeImage(downsampleImage, split[0] + "-ds.png");

        System.out.println("Success: wrote downsampled image " + fileName);
      }
      catch (Exception ex)
      {
        System.out.println("Error while processing " + fileName);
      }
    }
  }

  public static void test1()
  {
    ImageProcessor imageProcessor = new ImageProcessor();
//    BufferedImage regionImage = imageProcessor.extractRegion(SAMPLE_SVS,
//            98 * 50, 48 * 50, 76 * 50, 76 * 50);
//    imageProcessor.writeImage(regionImage, "5512-core1.png");
    BufferedImage downsampleImage = imageProcessor.downsampleImage(SAMPLE_SVS,
            50, 50);
    imageProcessor.writeImage(downsampleImage, "5512-ds.png");

//    BufferedImage edgeImage = imageProcessor.createEdgeImage(downsampleImage);
//    imageProcessor.writeImage(edgeImage, "5512-edge.png");
  }
}
