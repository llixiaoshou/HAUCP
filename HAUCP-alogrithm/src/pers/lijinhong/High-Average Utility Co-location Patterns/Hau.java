
package pers.lijinhong.colocation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Hau {
	private Map<String, Integer> featureNumMap; // 特征计数
	
	private boolean txt = true;
	private boolean txt1 = true;
	
	private List<List<String>> featureSet;// 模式集
//	  private Map<String,List<String>>Instance;//保存实例及其坐标
	private List<Instance> instanceList;
	private Map<String, List<Instance>> featureMap;
	private Map<String, Double> featureUtilityMap;// 特征对应的效用
	private Map<String, Double> featureUtilitySumMap;
	private Map<String, Map<Integer, List<String>>> starMap; // 保存星型邻居
	private Map<String, Map<Integer, Double>> twuTempMap;
	private Map<String,List<String>> Hau;
	private Map<String,List<String>> Ha;
	private double min_d;// 距离阈值
	private double min_u;// 效用阈值
	private double utilitySum;// 数据库总效用
	private double EARTH_RADIUS;
	private double frequencyThreshold = 0.2;
	
	private int kNum = 30;

	public Hau(double min_d, double min_u) {
//		  featuresMap = new HashMap<>();
		featureSet = new ArrayList<>();
		starMap = new HashMap<>();
		instanceList = new ArrayList<>();
		featureMap = new HashMap<>();
		featureUtilityMap = new HashMap<>();
		featureUtilitySumMap = new HashMap<>();
		featureNumMap = new HashMap<>();
		Hau=new HashMap<>();
		Ha=new HashMap<>();
		twuTempMap = new HashMap<String, Map<Integer, Double>>();
		this.min_d = min_d;
		this.min_u = min_u;
		utilitySum = 0;
		EARTH_RADIUS = 6378137.0;
		
	}

	/**
	 * 从文件中读入数据
	 * 
	 * @param args
	 */
	public void readInstance(String path) {
		try {
			FileReader fileReader = new FileReader(new File(path));
			BufferedReader reader = new BufferedReader(fileReader);
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					continue;
				}
				String[] tmp = line.split(",");
				String feature = tmp[0];
				if(feature.contains("\uFEFF")){
					feature=feature.replace("\uFEFF","");
				}
				int id = Integer.parseInt(tmp[1]);
				double x = Double.parseDouble(tmp[2]);
				double y = Double.parseDouble(tmp[3]);
				double z = Double.parseDouble(tmp[4]);
				if (!featureMap.containsKey(feature)) {
//	                	List<String> list1 = new ArrayList<>();
//	                	list1.add(feature);
//	                	featureSet.add(list1);
					List<Instance> featureInstanceList = new ArrayList<Instance>();
					featureInstanceList.add(new Instance(feature, id, x, y));
					featureMap.put(feature, featureInstanceList);
					featureUtilityMap.put(feature, z);
					utilitySum += z;
					featureUtilitySumMap.put(feature, z);
					featureNumMap.put(feature, 1);
				} else {
					featureUtilitySumMap.put(feature, featureUtilitySumMap.get(feature) + z);
					featureMap.get(feature).add(new Instance(feature, id, x, y));
					utilitySum += z;
					featureNumMap.put(feature, featureNumMap.get(feature) + 1);
//						System.out.println("特征数目：" + featureMap.size());
//						System.out.println(feature + "特征的实例数目：" + featureMap.get(feature).size());
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 计算两点间的距离
	 */
	public boolean distance(Instance instance1, Instance instance2) {
		
		double dx = Math.pow(instance1.x - instance2.x, 2.0);
		double dy = Math.pow(instance1.y - instance2.y, 2.0);
		double dist = Math.sqrt((dx + dy));
		//System.out.println("距离"+dist);
		if (dist < min_d) {
			return true;
		} else {
			return false;
		}
	}
//		纬度
//		double lat1 = Math.toRadians(instance1.y);
//        double lat2 = Math.toRadians(instance2.y);
//        // 经度
//        double lng1 = Math.toRadians(instance1.x);
//        double lng2 = Math.toRadians(instance2.x);
//        // 纬度之差
//        double a = lat1 - lat2;
//        // 经度之差
//        double b = lng1 - lng2;
//        // 计算两点距离的公式
//        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
//                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
//        // 弧长乘地球半径, 返回单位: 千米
//        s =  s * EARTH_RADIUS;
//        //System.out.println("juli"+s);
//		 
//		if(s <= min_d){
////			System.out.println(s);
//			return true;
//		}else{
//			return false;
//		}
//	}

   
	/**
	 * 生成星型邻居
	 * 
	 * @param args
	 */
	public void generateStar() {
		Set<Entry<String, List<Instance>>> featureMapEntry = featureMap.entrySet();
		for (Entry<String, List<Instance>> entry1 : featureMapEntry) {
			String feature = entry1.getKey();
			List<Instance> featureInstanceList = featureMap.get(feature);
			Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
			for (int i = 0; i < featureInstanceList.size(); i++) {

				Instance instance1 = featureInstanceList.get(i);
				List<String> otherInstanceList = new ArrayList<String>();

				for (Entry<String, List<Instance>> entry2 : featureMapEntry) {
					if ((int) entry2.getKey().toCharArray()[0] <= (int) entry1.getKey().toCharArray()[0]) {
						continue;
					}
					String feature2 = entry2.getKey();
					for (int j = 0; j < featureMap.get(feature2).size(); j++) {
						Instance instance2 = featureMap.get(feature2).get(j);
						if (distance(instance1, instance2)) {
							String featureName = instance2.featureName;
							int featureSeq = instance2.instanceSeq;
							otherInstanceList.add(featureName + featureSeq);
						}
					}
				}

				map.put(instance1.instanceSeq, otherInstanceList);
			}
			starMap.put(feature, map);
			
		}
//		System.out.println("100"+starMap);
	}

	/**
	 * 计算每个星型行实例的权值
	 * 
	 * @param args
	 */
	public double aumtwu() {
		Set<String> keySet = starMap.keySet();
		return 1;
	}

	public void generateTwuTempMap() {
//    	private Map<String, Map<Integer, List<String>>> neighborMap;
		Set<Entry<String, Map<Integer, List<String>>>> starMapSet = starMap.entrySet();
		for (Entry<String, Map<Integer, List<String>>> entry : starMapSet) {
//    		System.out.println(entry.getKey());
			String featureName = entry.getKey();
			Map<Integer, Double> map = new HashMap<Integer, Double>();
			Set<Entry<Integer, List<String>>> neighborMapSet = starMap.get(featureName).entrySet();
			for (Entry<Integer, List<String>> neighborMapSetEntry : neighborMapSet) {
				int instanceSeq = neighborMapSetEntry.getKey();
				List<String> neighborInstanceList = starMap.get(featureName).get(instanceSeq);
				double twuMax = 0;
				int[] featureNumArray = new int[starMap.size() + 1];
				for (String str : neighborInstanceList) {
					String neighborFeatureName = str.substring(0, 1);
					// int neighborInstanceSeq = Integer.valueOf(str.substring(1));
					int featureNumArraySeq = (int) neighborFeatureName.toCharArray()[0] - (int) 'A' + 1;
					featureNumArray[featureNumArraySeq]++;
				}

				for (int i = 1; i <= starMap.size(); i++) {
					if (featureUtilityMap.get((char) ('A' + i - 1) + "") * featureNumArray[i] > twuMax) {
						twuMax = featureUtilityMap.get((char) ('A' + i - 1) + "") * featureNumArray[i];
					}
				}
				
				if (twuMax < featureUtilityMap.get(featureName)) {
					twuMax = featureUtilityMap.get(featureName);
				}

				map.put(instanceSeq, twuMax);

			}
			twuTempMap.put(featureName, map);
		
		}
		//System.out.println("效用"+twuTempMap);
	}

	public double getTwu(String model) {
    //System.out.println(model);

		double twu = 0;
		Set<Entry<String, Map<Integer, List<String>>>> starMapSet = starMap.entrySet();
		for (Entry<String, Map<Integer, List<String>>> entry : starMapSet) {
			String featureName = entry.getKey();
			Set<Entry<Integer, List<String>>> neigborMapSet = starMap.get(featureName).entrySet();
			for (Entry<Integer, List<String>> entry1 : neigborMapSet) {
				int instanceSeq = entry1.getKey();
				if (isContainModel(featureName, instanceSeq, model)) {
					twu += twuTempMap.get(featureName).get(instanceSeq);
				}
			}
		}
	System.out.println("模式"+model+"高估效用值"+twu);
		return twu;
	}

	/**
	 * 判断是否包含模式
	 * 
	 * @param featureName
	 * @param instanceSeq
	 * @param model
	 * @return
	 */
	public boolean isContainModel(String featureName, int instanceSeq, String model) {
	 	String[] featureNameArray = new String[model.length() + 1];
		for (int i = 1; i <= model.length(); i++) {
			featureNameArray[i] = model.substring(i - 1, i);
		}

		List<String> instanceFeatureNameList = new ArrayList<String>();
		instanceFeatureNameList.add(featureName);
		List<String> neighborInstanceList = starMap.get(featureName).get(instanceSeq);
		for (String neighborInstance : neighborInstanceList) {
			String str = neighborInstance.substring(0, 1);
			instanceFeatureNameList.add(str);
		}

		boolean isContain = true;
		for (int i = 1; i <= featureNameArray.length - 1; i++) {
			if (!instanceFeatureNameList.contains(featureNameArray[i])) {
				isContain = false;
				break;
			}

		}

		return isContain;
	}
	
	/**
	 *计算高平均效用函数
	 */
     public double calculate_Hau(String model, List<String> list) {
    	 double utility = 0;
    	 String[] featureArray = new String[model.length()];
    	 for (int i = 0; i < featureArray.length; i++) {
    		 featureArray[i] = model.substring(i, i + 1);
    	 }
    	 
    	 for (int i = 0; i < featureArray.length; i++) {
    		 List<Integer> instanceSeqList = new ArrayList<Integer>();
    		 String featureName = featureArray[i];
    		 double singleUtility = featureUtilityMap.get(featureName);
    		 for (String tableLine : list) {
    			 int instanceSeq = Integer.valueOf(tableLine.split(",")[i]);
    			 if (!instanceSeqList.contains(instanceSeq)) {
    				 utility += singleUtility;
    				 instanceSeqList.add(instanceSeq);
    			 }
    		 }
    	 }
    	// return utility / model.length();
    	 double m=utility / model.length();
    	// System.out.println("模式"+model+"效用"+m);
    	 return utility / model.length();
     }
     
	/**
	 * 候选模式
	 * 
	 * @param args
	 */
	public List<String> gen_cancidate_Hau(List<String> Chau, int k) {
		List<String> candi = new ArrayList<>();
		for (int i = 0; i < Chau.size(); i++) {
			String list1 = Chau.get(i);
			for (int j = i + 1; j < Chau.size(); j++) {
				String list2 = Chau.get(j);
				if (list1.substring(0, k - 2).equals(list2.substring(0, k - 2))) {
					String c = "";
					c += list1 + list2.substring(list2.length() - 1);
					candi.add(c);
					
					
					if(!isNeedCut(c,Chau)) {   //判断该模式会不会被剪掉
						candi.add(c);
					}
					// System.out.println("c:"+c);
					 
				}
			}
		}
		return candi;	
	}
	/**
	 * 由k阶候选模式，生成其K-1阶子集
	 */
	public List<String> gen_Candicate_Chau(String candi){
		List<String> result=new ArrayList<>();
		for (int i = 0; i < candi.length(); i++) {
			result.add(candi.replace(candi.toCharArray()[i], '\0'));
		}
		return result;
	}
	/*
	 * 检测生成的k阶候选模式的k-1子集是否都是频繁的
	 */
	public  boolean isNeedCut(String item,List<String>list) {
		boolean flag=false;
		List<String> sublist=gen_Candicate_Chau(item);
		for(String s:sublist) {
			if(!list.contains(s)) {
				flag=true;
				break;
			}
		}
		return flag;
		
	}
	
	/**
	 * 生成粗高平均效用模式
	 * 
	 * @param args
	 */
	public List<String> gen_Chau(List<String> candi) {
		//System.out.println("100"+candi);
		List<String> Chau = new ArrayList<>();
		for (String model : candi) {
			if (getTwu(model) / utilitySum >= min_u) {
				Chau.add(model);
			}
		}
		//System.out.println("Chau"+Chau);
		return Chau;

	}
	/**
	 * 生成某个粗高平均效用模式的星型实例
	 * @param args
	 */
	public List<String> gen_Star_Instance(String item) {
        List<String> list = new ArrayList<>();
        String[] tmp = new String[item.length()];
        for (int i = 0; i < item.length(); i++) {
        	tmp[i] = item.substring(i, i + 1);
        }
        String key = tmp[0];
        Map<Integer, List<String>> value = starMap.get(key);
        for (Map.Entry<Integer, List<String>> entry : value.entrySet()) {
            Map<String, List<String>> map = new HashMap<>();
            for (int i = 1; i < tmp.length; i++) {
                map.put(tmp[i], new ArrayList<String>()); 
            }

            List<String> list2 = entry.getValue();
            for (String ins : list2) {
                String[] strs = new String[2];
                strs[0]=ins.substring(0, 1);
                strs[1]=ins.substring(1);		
                if (map.containsKey(strs[0])) {
                    map.get(strs[0]).add(strs[1]);
                }
            } 

            List<String> result = new ArrayList<>();
            result.add(entry.getKey().toString());
            for (int i = 1; i < tmp.length; i++) {//计算笛卡尔积
                List<String> tmpList = new ArrayList<>();
                List<String> list3 = map.get(tmp[i]);
                for (String prefix : result) {
                    for (String str : list3) {
                        String s = prefix + "," + str;
                        tmpList.add(s);
                    }
                }
                result = tmpList;
            }
            list.addAll(result);
        }
        return list;
	}
	/**
     * 检验是否成团，并把不成团的实例过滤掉
     * @param args
     */
	public List<String> filter_Clique_Instance(String item1, List<String> starIns) {
		if(item1.length()<=2) {
			return starIns;
		}
		else {
		
		String item = new String("");
		
		for (int i = 0; i < item1.length(); i++) {
			if (i < item1.length() - 1) {
				item += item1.substring(i, i + 1) + ",";
			}else {
				item += item1.substring(i, i + 1);
			}
			
		}
        List<String> cliqueList = new ArrayList<>();
        int index = item.indexOf(",");
        String subPattern = item.substring(index + 1);
        String[] subPatternFeatureArray = subPattern.split(",");
        for (String ins : starIns) {
            index = ins.indexOf(",");
            String str = ins.substring(index + 1);
            String[] seqListStrArray = str.split(",");
            boolean isClique = true;
            for (int i = 0; i < subPatternFeatureArray.length; i++) {
            	for (int j = i + 1; j < subPatternFeatureArray.length; j++) {
            		if (!starMap.get(subPatternFeatureArray[i]).get(Integer.valueOf(seqListStrArray[i])).contains(subPatternFeatureArray[j] + seqListStrArray[j])) {
            			isClique = false;
            		}	
            	}	
            }
            
            if (isClique) {
            	cliqueList.add(ins);
            }   
        }
        return cliqueList;
		}
    }
	/**
	 * 生成候选模式的表实例
	 * @param args
	 */
	 public Map<String, List<String>> gen_table_instance( List<String> candidates) {
	        Map<String, List<String>> tableInstance = new HashMap<>();
      //      System.out.println("候选"+candidates);
	        for (String item : candidates) {
//	        	System.out.println("候选"+item);
	            List<String> starIns = gen_Star_Instance(item);
	            
                List<String> cliques = filter_Clique_Instance(item, starIns);
                if (cliques.size() > 0) {
                    tableInstance.put(item, cliques);
                }
            
	        }
	        return tableInstance;
	    }
	 /**
	  * 计算高平均效用，过滤掉非高平均效用，并返回高平均效用模式
	  * @param args
	  */
	 public List<String> select_Hau_co_location(Map<String, List<String>> tableInstance){
		// System.out.println("候选"+utilitySum);
		 List<String>Hau_colocation=new ArrayList<String>();
		 for(Map.Entry<String, List<String>>entry:tableInstance.entrySet()) {
			 double hau=calculate_Hau(entry.getKey(), entry.getValue());
			 //System.out.println(entry.getKey()+ hau/utilitySum);
			 if(hau/utilitySum>=min_u) {
				 Hau_colocation.add(entry.getKey()); 
				 Hau.put(entry.getKey(),entry.getValue());
			 }			 
		 }
		 
		 return Hau_colocation;
		 
	 }
	 /**
	  * 将高平均效用模式输出到文件
	  * @param args
	 * @throws IOException 
	  */
	public void write(List<String>Hau_colocation, Map<String, List<String>> tableInstance) {
		File file = new File("D://HAUColocation.txt");
		if (txt) {
			file.delete();
			txt = false;
		}
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Pattern> patternList = new ArrayList<Pattern>();
		 for (String model : Hau_colocation) {
			 patternList.add(new Pattern(model, getModelUtility(model, tableInstance)));
		 }
		 
		 Collections.sort(patternList);
		// System.out.println("阶数"+patternList.size());
//		 double qc = 0;
//		 double lower = 0;
//		 double m=0;
//		 double n=0;
//		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
//			// System.out.println("阶数"+patternList.get(i - 1).pattern);
//			 qc = patternList.get(i - 1).utility;
////			 System.out.println("效用"+qc);
//			 lower =getModelAllFeatureUtility(patternList.get(i - 1).pattern);
////			 System.out.println("效用值"+lower);
//			 m+=qc / lower;	
//			 n=m/kNum;
////			  System.out.println("效用率"+m);
//		 }
//		 qc = n;
//		 
//// 	    System.out.println("效用"+qc);
////		 int frequencyNum = 0;
////		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
////			// System.out.println("阶数"+patternList.get(i - 1).pattern);
////			 if (isFrequency(patternList.get(i - 1).pattern, tableInstance)) {
////				 frequencyNum++;
////			 }
////		 }
////		 for (String model : Hau_colocation) {
////			 if (isFrequency(model, tableInstance)) {
////				 frequencyNum++; 
////		 }
////		 }
//		   
//		// double pc = (double) frequencyNum / kNum;
//		 double p=0;
//		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
//			 //System.out.println("阶数"+patternList.get(i - 1).pattern);
////			 if (isFrequency(patternList.get(i - 1).pattern, tableInstance)) {
////				 frequencyNum++;
////			 }
//			
//			p+=Frequency(patternList.get(i - 1).pattern, tableInstance); 
//			//System.out.println("值"+ p);
//		 }
//		 double pu=p/kNum;
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		int i = 0;
		for (String str : Hau_colocation) {
//			System.out.println(str);
			try {
				if (i == 0) {
					bufferedWriter.write("\n" + str.length() + "阶高平均效用模式个数为：" + Hau_colocation.size() + "\n");
					bufferedWriter.flush();
//					bufferedWriter.write("\n" + str.length() + "阶高平均效用模式,Top-" + kNum + "的Qc值：" + qc + "\n"
//		   					+ str.length() + "阶高平均效用模式，Top-" + kNum + "的pc值" + pu + "\n");
//		   			bufferedWriter.flush();
				}
				bufferedWriter.write(str + "\t");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				bufferedWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i++;
		}
		try {
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 执行算法
	 * @param args
	 * @throws IOException 
	 */
	public void run() throws IOException {
   	 generateStar();
   	 generateTwuTempMap();
   	 List<String> Chau = new ArrayList<String>();
   	 for (Map.Entry<String, Double> entry : featureUtilityMap.entrySet()) {
   		 Chau.add(entry.getKey());
   	 }
//   	 List<String> Chau1 = new ArrayList<String>();
//   	 Chau1.addAll(modelList1);//一阶的粗高效用过滤
//   	 // System.out.println("Chau1"+Chau1);
//   	List<String> Chau=gen_Chau(Chau1);
   	 //System.out.println("Chau"+Chau);
   	 int k = 2;
   	 while (true) {
   		 List<String> candi = gen_cancidate_Hau(Chau, k);
   		 Chau= gen_Chau(candi);
   		if(Chau.size()==0) {
  			 break;
  		 }
   		 
  		// System.out.println("Chau"+Chau);
   		 
  		 File file = new File("D://HAUColocationChau.txt");
  			if (txt1) {
   				file.delete();
   				txt1 = false;
 			}
			FileWriter fileWriter = null;
   			try {
  				fileWriter = new FileWriter(file, true);
   			} catch (IOException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			}
   			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
  			
   			try {
	   			bufferedWriter.write("\n" + Chau.get(0).length() + "阶候选模式个数：" + Chau.size());
	   			bufferedWriter.flush();
	   			bufferedWriter.newLine();
   			} catch (IOException e) {
   				
   			}
   			
   			for (String str : Chau) {
//   				System.out.println(str);
   				try {
   					bufferedWriter.write(str + "\t");
   				} catch (IOException e) {
   					// TODO Auto-generated catch block
   					e.printStackTrace();
   				}
   				try {
   					bufferedWriter.flush();
   				} catch (IOException e) {
   					// TODO Auto-generated catch block
   					e.printStackTrace();
   				}
   			}
   			try {
   				bufferedWriter.close();
   			} catch (IOException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			}
   			try {
   				fileWriter.close();
   			} catch (IOException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
  			}
   		 
//   		 if(Chau.size()==0) {
//   			 break;
//   		 }
//   		 
   		 Map<String ,List<String>> tableInstance=gen_table_instance(Chau);
   		 List<String> Hau_colocation = select_Hau_co_location(tableInstance);
   		 write(Hau_colocation, tableInstance);
//   		 if(Hau_colocation.size()==0) {
//   			 break;
//   		 }
   		 k++;
   	 	}
//   	   Ha.putAll(Hau);
//   	   System.out.println(Ha);
//       write1(Ha);
   	 // System.out.println(Ha);
    }
	
//	public void write1(Map<String,List<String>>tableInstance)throws IOException {
//		List<Pattern> patternList = new ArrayList<Pattern>();
//		 for(Map.Entry<String, List<String>>entry:tableInstance.entrySet()) {
//			 //System.out.println(entry.getKey());
//			 patternList.add(new Pattern(entry.getKey(), getModelUtility(entry.getKey(), tableInstance)));
//			 
//		 }
//		 Collections.sort(patternList);
////		for(int a=1;a<= patternList.size();a++) {
////			System.out.println(patternList.get(a - 1).pattern);
////			System.out.println(patternList.get(a - 1).utility);
////		}
//		 double qc = 0;
//		 double lower = 0;
//		 double m=0;
//		 double n=0;
//		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
//			// System.out.println(patternList.size());
//			 qc = patternList.get(i - 1).utility;
////			 System.out.println("模式"+qc);
//			 //System.out.println(patternList.get(i - 1).pattern);
//			 
//			 lower = getModelAllFeatureUtility(patternList.get(i - 1).pattern);
//			 
//		   //  System.out.println(patternList.get(i - 1).pattern);
//			 m+=qc/lower;
////			 System.out.println("效用"+m);
//			 n=m/kNum;
//			//  System.out.println("高效用模式"+n);
//		 }
//		 
//		 qc = n;
//		 System.out.println("高效用模式"+qc);
//		 double p=0;
//		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
//			// System.out.println("阶数"+patternList.get(i - 1).pattern);
////			 if (isFrequency(patternList.get(i - 1).pattern, tableInstance)) {
////				 frequencyNum++;
////			 }
//			
//			p+=Frequency(patternList.get(i - 1).pattern, tableInstance); 
//			//System.out.println("值"+ p);
//		 }
//		 double pu=p/kNum;
//		 System.out.println("频繁模式"+pu);
//		
//	}
	public double getModelAllFeatureUtility(String model) {
		double utility = 0;
		for (int i = 0; i < model.length(); i++) {
			String featureName = Character.toString(model.charAt(i));
			//System.out.println("特征"+featureName);
			utility += featureUtilitySumMap.get(featureName);
			//System.out.println("模式"+featureName+"效用"+utility);
		}
    	//System.out.println("效用"+utility);
		return utility;
	}
	
	public double getModelUtility(String model, Map<String, List<String>> tableInstance) {
		//System.out.println("模式"+model)
		List<String> instanceList = tableInstance.get(model);
		double modelUtility = 0;
		for (int i = 0; i < model.length(); i++) {
			
			String featureName = Character.toString(model.charAt(i));
//			
			List<Integer> integers = new ArrayList<Integer>();
			for (int j = 0; j < instanceList.size(); j++) {
				String[] lineArray = instanceList.get(j).split(",");
				if (integers.contains(Integer.valueOf(lineArray[i]))) {
				} else {
					modelUtility += featureUtilityMap.get(featureName);
					integers.add(Integer.valueOf(lineArray[i]));
				}
			}
		}
		//System.out.println("模式"+model+"效用"+modelUtility);
		return modelUtility;
	}
	
	public double Frequency(String model, Map<String, List<String>> tableInstance) {
		//System.out.println("测试"+model);
		List<String> instanceList = tableInstance.get(model);
		double m=1;
		// double n=0;
		for (int i = 0; i < model.length(); i++) {
			int featureInstanceNum = 0;
			String featureName = Character.toString(model.charAt(i));
			//System.out.println(featureName);
			List<Integer> integers = new ArrayList<Integer>();
			for (int j = 0; j < instanceList.size(); j++) {
				String[] lineArray = instanceList.get(j).split(",");
				if (integers.contains(Integer.valueOf(lineArray[i]))) {
				} else {
					featureInstanceNum++;
					integers.add(Integer.valueOf(lineArray[i]));
				}
			}
			//System.out.println("特征"+featureName+"次数"+featureInstanceNum);
			if((double) featureInstanceNum / featureNumMap.get(featureName)<m) {
				 m=(double) featureInstanceNum / featureNumMap.get(featureName);
			}
			
			//System.out.println(model+m);
			//System.out.println("测试1"+featureInstanceNum);
			//System.out.println("测试2"+featureNumMap.get(featureName)); 
			//System.out.println("测试"+featureNumMap.get(featureName));
//			if (m >= frequencyThreshold){
//				n+=m;
//				
//			}
			//System.out.println("平均"+n);	 
			}
//		}
		//System.out.println(m);
 		return m;
	}
//			if ((double) featureInstanceNum / featureNumMap.get(featureName) < frequencyThreshold) {
//				return false;
//			}
//		}
//		return true;
//	}
//	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		long start=System.currentTimeMillis();
		//System.out.println("runtime:"+start);
		Hau hau = new Hau(2,0.3);//		hau.readInstance(Config.dataPath);
		hau.readInstance("D://CoLocation/data3.csv");
		hau.run();
		long end=System.currentTimeMillis();
		System.out.println("runtime:"+(end-start)/1000 + "s");
    }	 
}