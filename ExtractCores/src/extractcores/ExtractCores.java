package extractcores;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ExtractCores
{
    private static final String SAMPLE_DS = 
            "S:\\Meine Daten\\Schutzbereich\\MoS\\Master Thesis\\impl\\extract cores\\data\\sample\\5512-ds.jpg";
    
    
  public static void main(String[] args) throws IOException
  {
      downsample();
  }
  
  public static void test1() throws IOException
  {
    Rectangle sourceRegion = new Rectangle(0, 0, 15000, 15000); // The region you want to extract

//        String exImagePath = "E:\\HE_TMA\\5512.svs";
        String exImagePath = "S:\\Meine Daten\\Schutzbereich\\MoS\\Master Thesis\\impl\\extract cores\\data\\sample\\5512.svs";
        File inputFile = new File(exImagePath);
        boolean exists = inputFile.exists();
        ImageInputStream stream = ImageIO.createImageInputStream(inputFile);
// Test with general image
//        InputStream inputStream = new URL("http://cdn2.spiegel.de/images/image-1136945-900_breitwand_180x67-nlny-1136945.jpg").openStream();
//    InputStream inputStream = new URL("http://slides.virtualpathology.leeds.ac.uk/Research_2/Heike/HE/26587.svs").openStream();
//    ImageInputStream stream = ImageIO.createImageInputStream(inputStream);

    Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

    if (readers.hasNext())
    {
      ImageReader reader = readers.next();
      reader.setInput(stream);

      ImageReadParam param = reader.getDefaultReadParam();
      param.setSourceRegion(sourceRegion); // Set region

      BufferedImage image = reader.read(0, param); // Will read only the region specified

      File outputfile = new File("testImage.jpg");
      ImageIO.write(image, "jpg", outputfile);
    }
    else
    {
      System.out.println("Failure. No reader found for image.");
    }

//    try (InputStream in = new URL(
//            "http://slides.virtualpathology.leeds.ac.uk/Research_2/Heike/HE/Gastric/121423.svs").
//            openStream())
//    {
//      Files.copy(in, Paths.get("/media/daniel/Storage/Meine Daten/Schutzbereich/MoS/Master Thesis/impl/extract cores/HE-Gastric-121423.svs"));
//    }
//    catch (Exception ex)
//    {
//      System.out.println(ex);
//      System.out.println(ex.getMessage());
//    }
  }

  public static void downsample() throws IOException
  {
//    Rectangle sourceRegion = new Rectangle(5500, 2500, 1024, 1024); // The region you want to extract

    String exImagePath = "S:\\Meine Daten\\Schutzbereich\\MoS\\Master Thesis\\impl\\extract cores\\data\\sample\\5512.svs";
//    String exImagePath = "/media/daniel/Storage/Meine Daten/Schutzbereich/MoS/Master Thesis/impl/extract cores/data/sample/5512.svs";
    File inputFile = new File(exImagePath);
    boolean exists = inputFile.exists();
    ImageInputStream stream = ImageIO.createImageInputStream(inputFile);

    Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

    if (readers.hasNext())
    {
      ImageReader reader = readers.next();
      reader.setInput(stream);

//      int scaledWidth = (int) (reader.getWidth(0) * 0.1);
//      int scaledHeight = (int) (reader.getHeight(0) * 0.1);

      ImageReadParam param = reader.getDefaultReadParam();
//      param.setSourceRegion(sourceRegion); // Set region
      param.setSourceSubsampling(50, 50, 0, 0);
      
      Rectangle sourceRegion = param.getSourceRegion();

      BufferedImage inputImage = reader.read(0, param); // Will read only the region specified

      File outputfile = new File(inputFile.getName() + ".jpg");
      ImageIO.write(inputImage, "jpg", outputfile);

//      BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight,
//              inputImage.getType());
//
//      // scales the input image to the output image
//      Graphics2D g2d = outputImage.createGraphics();
//      g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
//      g2d.dispose();
//
//      ImageIO.write(outputImage, "jpg", new File("scaleOutput"));
    }
    else
    {
      System.out.println("Failure. No reader found for image.");
    }

  }
  
  public static void originalSize(ImageReader reader) throws IOException
  {
      int scaledWidth = (int) (reader.getWidth(0) * 0.1);
      int scaledHeight = (int) (reader.getHeight(0) * 0.1);
      int a = 3;
  }

}
