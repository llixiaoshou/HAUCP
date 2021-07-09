package pers.lijinhong.colocation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class CreateData {
	
	public static int featureNum;
	public static int insNum;
	public static int featureInstanceNumMax;
	//public static int featureInstance;
	public static int featureInstanceNumMin;
	public static int featureUtilityMax;
	public static int featureUtilityMin;
	public static int xMax;
	public static int xMin;
	public static int yMax;
	public static int yMin;
	
	public static String dataPath;
	
	public static void main(String[] args) throws IOException {
		initialize();
		createData();
	}
	
	public static void initialize() {
		featureNum = 50;
		insNum = 50000;
		featureInstanceNumMax = Config.featureInstanceNumMax;
		featureInstanceNumMin = Config.featureInstanceNumMin;
		//featureInstance = Config.featureInstance;
		featureUtilityMax = Config.featureUtilityMax;
		featureUtilityMin = Config.featureUtilityMin;
		xMax = Config.xMax;
		xMin = Config.xMin;
		yMax = Config.yMax;
		yMin = Config.yMin;
		dataPath = Config.dataPath;
	}
	
	public static void createData() throws IOException {
		File file = new File(dataPath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir(); 
			file.createNewFile();
		}
		Random random = new Random();
		
		int[] num = new int[featureNum];
		for (int i = 0; i < featureNum - 1; i++) {
			num[i] = insNum / featureNum;
		}
		num[featureNum - 1] = insNum - (featureNum - 1) * insNum / featureNum;
		
		for (int i = 0; i < featureNum; i++) {
			for (int j = 0; j < featureNum; j++) {
				if (i != j) {
					if (random.nextDouble() > 0.5) {
						int temp = random.nextInt(num[i] / 2);
						num[i] -= temp;
						num[j] += temp;
					} else {
						int temp = random.nextInt(num[j] / 2);
						num[i] += temp;
						num[j] -= temp;
					}
				}
			}
		}
		
		FileWriter fileWriter = new FileWriter(file);
		String[] featureNameList = new String[featureNum + 1];
		for (int i = 1; i <= featureNum; i++) {
			featureNameList[i] = (char)('A' + i - 1) + "";
		}
		
		for (int i = 1; i <= featureNum; i++) {
			int utility = random.nextInt(featureUtilityMax - featureUtilityMin + 1) + featureUtilityMin;
			String featureName = featureNameList[i];
			for (int j = 1; j <= num[i - 1]; j++) { 
				String line = "";
				int x = random.nextInt(xMax - xMin + 1) + xMin;
				int y = random.nextInt(yMax - yMin + 1) + yMin;
				line = featureName + "," + j + "," + x + "," + y + "," + utility + "\n";
				fileWriter.write(line);
				fileWriter.flush();
			}
		}
	}
}
