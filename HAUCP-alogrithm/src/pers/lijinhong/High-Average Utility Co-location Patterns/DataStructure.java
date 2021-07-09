package pers.lijinhong.colocation;

import java.util.ArrayList;
import java.util.List;

public class DataStructure {

}

class Instance {
	
	public String featureName;
	public int instanceSeq;
	public double x;
	public double y;
	
	Instance() {
		
	}
	
	Instance(String featureName, int instanceSeq, double x, double y) {
		this.featureName = featureName;
		this.instanceSeq = instanceSeq;
		this.x = x;
		this.y = y;
	}

//	@Override
//	public String toString() {
//		// TODO Auto-generated method stub
//		return this.featureName+","+this.instanceSeq+","+this.x+","+this.y;
//	}
	

}

class Feature {
	
	public String featureName;
	public int utility;
	public List<Instance> instanceList;
	
	Feature() {
		instanceList = new ArrayList<Instance>();
	}

	Feature(String featureName, int utility) {
		this();
		this.featureName = featureName;
		this.utility = utility;
	}
	
}

class Star{

	public String featureName;
	public List<Neighbor> neighborList;
	
	Star(String featureName){
		this.featureName = featureName;
		neighborList = new ArrayList<Neighbor>();
	}
}

class Neighbor{
	
	public Instance centerInstance;
	public List<Instance> neighborInstanceList;
	
	public Neighbor(Instance centerInstance) {
		this.centerInstance = centerInstance;
		neighborInstanceList = new ArrayList<Instance>();
	}
}

class Temp{
	public List<String> strList1;
	public List<String> strList2;
	
	public Temp() {
		// TODO Auto-generated constructor stub
	}

	public Temp(List<String> strList1, List<String> strList2) {
		super();
		this.strList1 = strList1;
		this.strList2 = strList2;
	}
	
}