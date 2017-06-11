package extractcores;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author Daniel
 */
public class ImageProcessor
{
  private static final String FILE_PATH = "..\\data\\";

  private String filePath;

  public ImageProcessor()
  {
    this.filePath = FILE_PATH;
  }

  public void setFilePath(String filePath)
  {
    this.filePath = filePath;
  }

  public BufferedImage extractRegion(String imageName, int x, int y, int width, int height)
  {
    ImageReader reader = getImageReader(imageName);
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

  public BufferedImage downsampleImage(String imageName, int sampleFactorX, int sampleFactorY)
  {
    ImageReader reader = getImageReader(imageName);
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

  public BufferedImage readImage(String imageName)
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

  private ImageReader getImageReader(String imageName)
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
    edgeDetector.process();
    BufferedImage edgesImage = edgeDetector.getEdgesImage();
    return edgesImage;
  }

  public boolean writeImage(BufferedImage writeImage, String outputFileName)
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
}
