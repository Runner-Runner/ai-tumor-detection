package extractcores;

import static extractcores.DefaultConfigValues.DOWNSAMPLE_FACTOR;
import static extractcores.DefaultConfigValues.FILE_PATH_IMAGE_DRIVE;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import static javax.swing.Spring.height;
import static javax.swing.Spring.width;

/**
 *
 * @author Daniel
 */
public class ImageProcessor
{
  public BufferedImage extractDownsampledRegion(String filePath,
          String imageName, int x, int y, int width, int height, int sampleFactor)
  {
    ImageReader reader = getImageReader(filePath, imageName);
    ImageReadParam param = reader.getDefaultReadParam();

    Rectangle sourceRegion = new Rectangle(x, y, width, height);
    param.setSourceRegion(sourceRegion);
    param.setSourceSubsampling(sampleFactor, sampleFactor, 0, 0);

    return readImage(reader, param, 0);
  }

  public BufferedImage extractRegion(String filePath, String imageName, int x,
          int y, int width, int height)
  {
    ImageReader reader = getImageReader(filePath, imageName);
    ImageReadParam param = reader.getDefaultReadParam();

    Rectangle sourceRegion = new Rectangle(x, y, width, height);
    param.setSourceRegion(sourceRegion);

    return readImage(reader, param, 0);
  }

  public void extractCoreRegions(int digitKey, List<TissueCore> labeledCores)
  {
    ImageProcessor imageProcessor = new ImageProcessor();
    ImageReader reader = getImageReader(FILE_PATH_IMAGE_DRIVE, digitKey + ".svs");
    ImageReadParam param = reader.getDefaultReadParam();

    int backupCounter = 1;

    int size = labeledCores.size();
    for (int i=0; i<size; i++)
    {
      TissueCore core = labeledCores.get(i);
      
      CoreLabel label = core.getLabel();

      if (label != CoreLabel.TUMOR && label != CoreLabel.NORMAL)
      {
        continue;
      }

      Rectangle coreBoundingBox = core.getBoundingBox();
      coreBoundingBox.x *= DOWNSAMPLE_FACTOR;
      coreBoundingBox.y *= DOWNSAMPLE_FACTOR;
      coreBoundingBox.width *= DOWNSAMPLE_FACTOR;
      coreBoundingBox.height *= DOWNSAMPLE_FACTOR;

      try
      {
        param.setSourceRegion(coreBoundingBox);

        BufferedImage regionImage = readImage(reader, param, 0);

        String trainingDataPath = DefaultConfigValues.FILE_PATH_IMAGE_DRIVE_OUTPUT 
                + digitKey + "\\";
        File dirFile = new File(trainingDataPath);
        if(!dirFile.exists())
        {
          dirFile.mkdir();
        }
        
        String idText;
        int id = core.getId();
        if (id == -1)
        {
          idText = "noID_" + backupCounter++;
        }
        else
        {
          idText = String.valueOf(id);
        }
        String coreFileName = label.name() + "_" + idText + ".png";
        imageProcessor.writeImage(regionImage, trainingDataPath,
                coreFileName + ".png");
        
        System.out.println("Wrote training data for digitKey=" + 
                digitKey + ", core id=" + core.getId() + 
                " (" + i + "/" + size + ").");
      }
      catch (IllegalStateException ex)
      {
        System.out.println("Error writing training data for digitKey=" + 
                digitKey + ", core id=" + core.getId() + ".");
      }
    }
  }

  public BufferedImage downsampleImage(String filePath, String imageName,
          int sampleFactorX, int sampleFactorY)
  {
    ImageReader reader = getImageReader(filePath, imageName);
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceSubsampling(sampleFactorX, sampleFactorY, 0, 0);

    return readImage(reader, param, 0);
  }

  private BufferedImage readImage(ImageReader reader, ImageReadParam param,
          int imageIndex)
  {
    try
    {
      BufferedImage inputImage = reader.read(0, param);
      return inputImage;
    }
    catch (IOException ex)
    {
      System.out.println("Error while reading image.");
      System.out.println(ex.getMessage());
      return null;
    }
  }

  public BufferedImage readImage(String filePath, String imageName)
  {
    try
    {
      BufferedImage sourceImage = ImageIO.read(new File(filePath + imageName));
      return sourceImage;
    }
    catch (IOException ex)
    {
      System.out.println("Error while reading source image: " + imageName);
      System.out.println(ex.getMessage());
      return null;
    }
  }

  private ImageReader getImageReader(String filePath, String imageName)
  {
    File inputFile = new File(filePath + imageName);
    boolean exists = inputFile.exists();
    if (!exists)
    {
      System.out.println("Input file not found.");
      return null;
    }

    ImageInputStream stream;
    try
    {
      stream = ImageIO.createImageInputStream(inputFile);
    }
    catch (IOException ex)
    {
      System.out.println("Error while reading file into stream.");
      System.out.println(ex.getMessage());
      return null;
    }

    Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

    if (readers.hasNext())
    {
      ImageReader reader = readers.next();
      reader.setInput(stream);

      return reader;
    }
    else
    {
      System.out.println("No reader found for image.");
      return null;
    }
  }

  public BufferedImage createEdgeImage(BufferedImage sourceImage)
  {
    CannyEdgeDetector edgeDetector = new CannyEdgeDetector();
    edgeDetector.setSourceImage(sourceImage);

    //default: 2.5f
    edgeDetector.setLowThreshold(2.5f);
    //default: 7.5f
    edgeDetector.setHighThreshold(7.5f);

    edgeDetector.process();
    BufferedImage edgesImage = edgeDetector.getEdgesImage();

    //Convert to TYPE_3BYTE_BGR
    edgesImage = convert(edgesImage, BufferedImage.TYPE_3BYTE_BGR);

    return edgesImage;
  }

  public boolean writeImage(BufferedImage writeImage, String filePath, String outputFileName)
  {
    File outputFile = new File(filePath + outputFileName);
    try
    {
      ImageIO.write(writeImage, "png", outputFile);
    }
    catch (IOException ex)
    {
      System.out.println("Error while writing image \"" + outputFileName + "\".");
      System.out.println(ex.getMessage());
      return false;
    }
    return true;
  }

  public void getImageCompressionType(String filePath, String imageName)
  {
    ImageReader imageReader = getImageReader(filePath, imageName);
    try
    {
      IIOMetadata imageMetadata = imageReader.getImageMetadata(0);
      String[] extraMetadataFormatNames = imageMetadata.getExtraMetadataFormatNames();
    }
    catch (IOException ex)
    {
      System.out.println("Cannot receive compression type from reader.");
      System.out.println(ex.getMessage());
    }
  }

  public void createLabelIdentifyingImage(String filePath, String imageName)
  {
    ImageReader imageReader = getImageReader(filePath, imageName);
    try
    {
      BufferedImage labelImage = imageReader.read(5);

      //Some svs files keep the label image at different index, check by dimensions of received image
      int imageWidth = labelImage.getWidth();
      int imageHeight = labelImage.getHeight();
      if (Math.abs(imageWidth - imageHeight) > 100)
      {
        labelImage = imageReader.read(4);
      }

      writeImage(labelImage, filePath, appendFilename(imageName, "labelinfo", "png"));
    }
    catch (IOException ex)
    {
      System.out.println("Cannot read image.");
      System.out.println(ex.getMessage());
    }
  }

  private static BufferedImage convert(BufferedImage src, int bufImgType)
  {
    BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), bufImgType);
    Graphics2D g2d = img.createGraphics();
    g2d.drawImage(src, 0, 0, null);
    g2d.dispose();
    return img;
  }

  public static String appendFilename(String fileName, String suffix, String extension)
  {
    String[] split = fileName.split("\\.");
    if (split.length != 2)
    {
      return null;
    }
    String fileExtension = extension == null ? split[1] : extension;
    return split[0] + "-" + suffix + "." + fileExtension;
  }
}
