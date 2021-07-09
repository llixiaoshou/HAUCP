package pers.lijinhong.colocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoLocation {
	
	public static int featureNum;
	public static double distanceThreshold;
	
	public static void main(String[] args) throws IOException {
		initialize();
		Data data = new Data();
		List<Feature> featureList = data.readData();
		List<Star> starList = generateStarList(featureList);
		printStarList(starList);
	}
	
	public static void initialize() {
		featureNum = Config.featureNum;
		distanceThreshold = Config.distanceThreshold;
	}
	
	public static List<Star> generateStarList(List<Feature> featureList) {
		List<Star> starList = new ArrayList<Star>();
		for (int i1 = 1; i1 <= featureNum; i1++) {
			Feature feature1 = featureList.get(i1 - 1); 
			starList.add(new Star(feature1.featureName));
			for (int j1 = 1; j1 <= feature1.instanceList.size(); j1++) {
				Instance instance1 = feature1.instanceList.get(j1 - 1);
				starList.get(i1 - 1).neighborList.add(new Neighbor(instance1));
				
				for (int i2 = i1 + 1; i2 <= featureNum; i2++) {
					Feature feature2 = featureList.get(i2 - 1);
					for (int j2 = 1; j2 <= feature2.instanceList.size(); j2++) {
						Instance instance2 = feature2.instanceList.get(j2 - 1);
						if (getDistance(instance1, instance2) <= distanceThreshold) {
							starList.get(i1 - 1).neighborList.get(j1 - 1).neighborInstanceList.add(instance2);
						}
					}
				}
			}
		}
		return starList;
	}
	
	public static void printStarList(List<Star> starList) {
		for (Star star : starList) {
			for (Neighbor neighbor : star.neighborList) {
				printInstance(neighbor.centerInstance);
				for (Instance neighborInstance : neighbor.neighborInstanceList) {
					printInstance(neighborInstance);
				}
				System.out.println();
			}
		}
	}
	
	public static void printInstance(Instance instance) {
		System.out.print(instance.featureName + instance.instanceSeq + " ");
	}
	
	public static double getDistance(Instance instance1, Instance instance2) {
		double x1 = instance1.x;
		double y1 = instance1.y;
		double x2 = instance2.x;
		double y2 = instance2.y;
		return Math.pow(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2), 0.5);
	}
}