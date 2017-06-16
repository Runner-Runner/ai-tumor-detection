package extractcores;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import static extractcores.DefaultPaths.FILE_BASE_PATH;

/**
 *
 * @author Daniel
 */
public class ImageProcessor
{
  public BufferedImage extractRegion(String filePath, String imageName, int x, int y, int width, int height)
  {
    ImageReader reader = getImageReader(filePath, imageName);
    ImageReadParam param = reader.getDefaultReadParam();

    Rectangle sourceRegion = new Rectangle(x, y, width, height);
    param.setSourceRegion(sourceRegion);

    try
    {
      BufferedImage inputImage = reader.read(0, param);
      return inputImage;
    }
    catch (IOException ex)
    {
      System.out.println("Error while reading image region.");
      System.out.println(ex.getMessage());
      return null;
    }
  }

  public BufferedImage downsampleImage(String filePath, String imageName, 
          int sampleFactorX, int sampleFactorY)
  {
    ImageReader reader = getImageReader(filePath, imageName);
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceSubsampling(sampleFactorX, sampleFactorY, 0, 0);

    try
    {
      BufferedImage inputImage = reader.read(0, param);
      return inputImage;
    }
    catch (IOException ex)
    {
      System.out.println("Error while reading downsampled image.");
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

  private static BufferedImage convert(BufferedImage src, int bufImgType)
  {
    BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), bufImgType);
    Graphics2D g2d = img.createGraphics();
    g2d.drawImage(src, 0, 0, null);
    g2d.dispose();
    return img;
  }
}