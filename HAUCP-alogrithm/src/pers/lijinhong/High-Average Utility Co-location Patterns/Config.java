package pers.lijinhong.colocation;

public class Config {
	
	public static int featureNum = 20;
	//public static int featureInstance =1000;
	public static int featureInstanceNumMax =500;
	public static int featureInstanceNumMin = 200;
	public static int featureUtilityMax = 1000;
	public static int featureUtilityMin = 500;
	public static int xMax = 800;
	public static int xMin = 200;
	public static int yMax = 800;
	public static int yMin = 200;
	
	public static String dataPath = "D://CoLocation/data1.csv";
	public static double distanceThreshold = 50;
}
