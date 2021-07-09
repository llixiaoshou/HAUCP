package pers.lijinhong.colocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Data {
	
	public String dataPath;
	List<Feature> featureList; 
	
	Data() {
		initialize();
	}
	
	public void initialize() {
		dataPath = Config.dataPath;
		featureList = new ArrayList<Feature>();
	}
	
	public List<Feature> readData() throws IOException{
		File file = new File(dataPath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		while((line = bufferedReader.readLine()) != null) {
			String[] itemArray = line.split(",");
			String featureName = itemArray[0];
			int featureInstanceSeq = Integer.valueOf(itemArray[1]);
			int x = Integer.valueOf(itemArray[2]);
			int y = Integer.valueOf(itemArray[3]);
			int utility = Integer.valueOf(itemArray[4]);
			
			int featureSeq = 0;
			for (int i = 1; i <= featureList.size(); i++) {
				if (featureList.get(i - 1).featureName.equals(featureName)) {
					featureSeq = i;
					break;
				}
			}
			if (featureSeq != 0) {
				featureList.get(featureSeq - 1).instanceList.add(new Instance(featureName, featureInstanceSeq, x, y));
			}else {
				featureList.add(new Feature(featureName, utility));
				featureList.get(0).instanceList.add(new Instance(featureName, featureInstanceSeq, x, y));
			}
		}
		bufferedReader.close();
		fileReader.close();
		return featureList;
	}

}
