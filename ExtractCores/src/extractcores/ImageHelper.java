package extractcores;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Daniel
 */
public class ImageHelper
{
  private String imagePath;
  private BufferedImage sourceImage;

  public ImageHelper(String imagePath)
  {
    this.imagePath = imagePath;
    try
    {
      sourceImage = ImageIO.read(new File(imagePath));
    }
    catch (IOException ex)
    {
      System.out.println("Source Image not found.");
    }

  }

  public void createEdgeImage()
  {
    CannyEdgeDetector edgeDetector = new CannyEdgeDetector();
    edgeDetector.setSourceImage(sourceImage);
    edgeDetector.process();
    BufferedImage edgesImage = edgeDetector.getEdgesImage();
    File edgeOutput = new File("edgeOutput");
    try
    {
      ImageIO.write(edgesImage, "png", edgeOutput);
    }
    catch (IOException ex)
    {
      System.out.println("Error while writing edge image.");
    }
  }
}
