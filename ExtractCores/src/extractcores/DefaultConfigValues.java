package extractcores;

public interface DefaultConfigValues
{
  //Paths and file names
  public static final String FILE_BASE_PATH = "..\\data\\";

  public static final String FILE_PATH_EDGE = FILE_BASE_PATH + "edge output\\";
  public static final String FILE_PATH_DOWNSAMPLE = FILE_BASE_PATH + "ds output\\";
  public static final String FILE_PATH_LABEL = FILE_BASE_PATH + "labels\\";
  public static final String FILE_PATH_LABEL_SOLUTION = FILE_BASE_PATH + "labels\\solution\\";
  public static final String FILE_PATH_INFORMATIVE = FILE_BASE_PATH + "informative\\";
  public static final String FILE_PATH_INFORMATIVE_BATCHES = FILE_BASE_PATH + "informative\\batches\\";
  public static final String FILE_PATH_INPUT_SEGMENTS = FILE_BASE_PATH + "input images\\";

  public static final String STATISTICS_FILE_NAME = "statistics";
  public static final String CROSSREF_FILE_NAME = "crossref.properties";
  public static final String XLS_PREFIX = "TMA GC ";
  
  public static final String SAMPLE_IMAGE_DS = "5512-ds.png";
  public static final String SAMPLE_IMAGE_EDGE = "5512-edge.png";
  public static final String SAMPLE_SVS = "5512.svs";
  public static final String SAMPLE_LABEL = "5512-label.txt";

  public static final String FILE_PATH_IMAGE_DRIVE = "E:\\HE_TMA\\";
  public static final String FILE_PATH_IMAGE_DRIVE_OUTPUT = "E:\\training data\\";
  public static final String ABS_SAMPLE_FULL_IMAGE_DRIVE
          = "E:\\HE_TMA\\5512.svs";
  
  //Configuration Values
  public static final int DOWNSAMPLE_FACTOR = 50;
  
  public static final int MIN_OBJECT_AREA = 100;
  public static final int MAX_OBJECT_AREA = 6750;
  public static final int MIN_CORE_AREA = 1800;
  public static final double MIN_CORE_SIDE_DISTANCE = 5;
  public static final double LARGE_CORE_AREA_THRESHOLD = 3000;
  public static final double EXTREME_CORE_RATIO_THRESHOLD = 1.5;
}
