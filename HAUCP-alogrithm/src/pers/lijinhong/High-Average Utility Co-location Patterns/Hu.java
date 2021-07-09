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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.security.auth.kerberos.KerberosKey;
import javax.swing.text.TabableView;

public class Hu {
	private boolean txt1 = true;
	private boolean txt2=true;
	private Map<String, List<Instance>> featureMap;
	private HashMap<String,List<String>> neighborhood;//保存邻近关系
	private Double min_d;//距离阈值
	private double min_u;//高效用阈值
	private int s = 4;
	private Map<String, Double> featureUtilityMap;//特征所对应的效用
	private Map<String, Double> featureUtilitySumMap;
	private Map<String ,List<String>> table2;//二阶表实例
	private Map<String,List<String>> table;//表实例
	private Map<String,List<String>>Hu;//高效用
	private Map<String,List<String>>H;
	private double utilitySum;// 数据库总效用
	private boolean txt = true;
	private double EARTH_RADIUS;
	private Map<String, Integer> featureNumMap; // 特征计数
	private double frequencyThreshold = 0.2;
	private int kNum = 30;
	
	public Hu(double min_d,double min_u) {
		featureMap=new HashMap<>();
		neighborhood=new HashMap<>();
		featureUtilityMap=new HashMap<>();
		featureUtilitySumMap = new HashMap<>();
		featureNumMap = new HashMap<String, Integer>();
		table2=new HashMap<>();
		table=new HashMap<>();
		Hu=new HashMap<>();
		H=new HashMap<>();
		this.min_d=min_d;
		this.min_u=min_u;
		utilitySum=0;
		EARTH_RADIUS = 6378137.0; 		
	}
	/**
	 * 从文件中读入数据        
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
					featureNumMap.put(feature,1);
				} else {
					featureUtilitySumMap.put(feature, featureUtilitySumMap.get(feature) + z);
					featureMap.get(feature).add(new Instance(feature, id, x, y));
					utilitySum += z;
					featureNumMap.put(feature,featureNumMap.get(feature)+1);
//						System.out.println("特征数目：" + featureMap.size());
//						System.out.println(feature + "特征的实例数目：" + featureMap.get(feature).size());
				}

			}
//			System.out.println("特征数目：" + featureNumMap);
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
		if (dist < min_d) {
  		return true;
		} else {
			return false; 
		}
//
	}
//		纬度
	//	double lat1 = Math.toRadians(instance1.y);
     //   double lat2 = Math.toRadians(instance2.y);
        // 经度
       // double lng1 = Math.toRadians(instance1.x);
      //  double lng2 = Math.toRadians(instance2.x);
        // 纬度之差
       // double a = lat1 - lat2;
      
        
        
        
        
        
        // 经度之差
        //double b = lng1 - lng2;
        // 计算两点距离的公式
       // double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
         //       Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        // 弧长乘地球半径, 返回单位: 千米
       // s =  s * EARTH_RADIUS;
	//	if(s <= min_d){
		//	return true;
		//}else{
		//	return false;
		//}
	//}

	/**
	 * 生成二阶表实例
	 * @param args
	 */
	
	public void gen_table_ins2(){
		Set<Entry<String, List<Instance>>> featureMapEntry = featureMap.entrySet();
		String feature=new String();
		for (Entry<String, List<Instance>> entry1 : featureMapEntry) {
			String feature1= entry1.getKey();
			List<Instance> featureInstanceList = featureMap.get(feature1);
			for (int i = 0; i < featureInstanceList.size(); i++) {
				Instance instance1 = featureInstanceList.get(i);
				int featureSeq1= instance1.instanceSeq;
				String s1 = String.valueOf( featureSeq1);
				if(!table.containsKey(feature1)) {
					List<String> list1 = new ArrayList<String>();
					list1.add(s1);
					table.put(feature1, list1);
					
				}else {
					table.get(feature1).add(s1);
				}
				for (Entry<String, List<Instance>> entry2 : featureMapEntry) {
					if ((int) entry2.getKey().toCharArray()[0] <= (int) entry1.getKey().toCharArray()[0]) {
						continue;
				    }
					String feature2 = entry2.getKey();
					for (int j = 0; j < featureMap.get(feature2).size(); j++) {
						Instance instance2 = featureMap.get(feature2).get(j);
						if (distance(instance1, instance2)) {
							int featureSeq2= instance2.instanceSeq;
							String  s2= String.valueOf( featureSeq2);
							String s=s1+","+s2;
							feature=feature1+feature2;
							if(!table2.containsKey(feature)) {
								List<String> list = new ArrayList<String>();
								list.add(s);
								table2.put(feature, list);
								
							}else {
								table2.get(feature).add(s);
							}
						}			
			         }
		        }	
	        }
		}
		//System.out.println(table);
		table.putAll(table2);
	  //  System.out.print("二阶表"+table);	
	}
	
	/**
	 *计算高效用函数
	 */
	public double calculate_Hau(String model, List<String> list) {
		//System.out.println("12"+model);
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
   	
   	 return utility;
   	 
    }
	/**
	 * 候选模式(由K-1阶模式产生候选K阶模式)
	 * 
	 * @param args
	 */
	
	@SuppressWarnings("unchecked")
	public List<String> mapToKeyList(Map<List<String>, List<String>> map){
		List<String> keyList = new ArrayList<String>();
		for (Map.Entry<List<String>, List<String>> m : map.entrySet()) {
			keyList = (List<String>)map.get(m.getKey());
		}
		return keyList;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> mapToValueList(Map<List<String>, List<String>> map){
		List<String> valueList = new ArrayList<String>();
		for (Map.Entry<List<String>, List<String>> m : map.entrySet()) {
			valueList = map.get(m.getValue());
		}
		return valueList;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> gen_cancidate_Hau(List<String>keyList, int k) {
		
		List<String> Chau = keyList;
		
		List<String> candi = new ArrayList<>();
		for (int i = 0; i < Chau.size(); i++) {
			String list1 = Chau.get(i);
			for (int j = i + 1; j < Chau.size(); j++) {
				String list2 = Chau.get(j);
				if (list1.substring(0, k - 2).equals(list2.substring(0, k - 2))) {
					String c = "";
					c += list1 + list2.substring(list2.length() - 1);
					if(!isNeedCut(c,Chau)) {   //判断该模式会不会被剪掉
						candi.add(c);
					}

					
					// System.out.println("c:"+c);
				}
			}
			
		}
		 //System.out.println("c:"+candi);
		return candi;
	}
	/**
	 * 由k阶候选模式，生成其K-1阶子集
	 */
	public List<String> gen_Candicate_Chau(String candi){
		List<String> result=new ArrayList<>();
		for (int i = 0; i < candi.length(); i++) {
			result.add(candi.replace(Character.toString(candi.charAt(i)), ""));
		}
		return result;
	}
	/*
	 * 检测生成的k阶候选模式的k-1子集是否都是频繁的
	 */
	public  boolean isNeedCut(String item, List<String>list) {
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
	 * 由候选模式生成表实例
	 * @param args
	 */
	public HashMap<String,List<String>>gen_table_instance(List<String>candicate,Map<String,List<String>>table){
		HashMap<String,List<String>> map=new HashMap<>();
		for(String item:candicate) {
			List<String>list=new ArrayList<>();
			String s1=item.substring(0, item.length()-2);
			String s2=item.substring(item.length()-2,item.length()-1);
			String s3=s1+s2;
			//System.out.println("c:"+s1);
			String s4=item.substring(item.length()-1,item.length());
			String s5=s1+s4;
			String s6=s2+s4;
			List<String> list1=table.get(s3);
			//System.out.println("c:"+list1);
			List<String> list2=table.get(s5);
			 for (String str1 : list1) {
	                int index = str1.lastIndexOf(",");
	                String prefix1 = str1.substring(0, index);
	                String id1 = str1.substring(index + 1);
	                for (String str2 : list2) {
	                    index=str2.lastIndexOf(",");
	                    String prefix2=str2.substring(0,index);
	                    if(prefix1.equals(prefix2)){
	                        String id2 = str2.substring(index + 1);
	                        String id=id1+","+id2;
	                        if (table2.containsKey(s6)
	                                && table2.get(s6).contains(id)) {
	                            String newClique = str1 + "," + id2;
	                            list.add(newClique);
	                        }
	                    }
	                }
			 }
			 if(list.size()>0) {
				 map.put(item, list);
			 }
		}
		//System.out.println("c:"+map);
		//table.putAll(map);
		//System.out.println("表实例"+table);
		return map;
		}
     /**
     * 计算vss
     */
	
	public double getVss(String featureName, String model, int s) {
//		return featureUtilityMap.get(featureName) * getFirstSSubModel(model, featureName, s);
		return featureUtilityMap.get(featureName) * getFeatureNumber(featureName, getFirstSSubModel(model, featureName, s), table);
	}
	
	 public static ArrayList<String> getFirstSSubModel(String model, String featureName, int s){
         ArrayList<String> firstSSubModel = new ArrayList<>();
         ArrayList<String> orderFirstSSubModel = new ArrayList<String>();
         String subModel = model.replace(featureName, "");
         int k = subModel.length()-1;
         while(firstSSubModel.size() < s && k>0){
             gen_keepK_SubModel(featureName, subModel, k, s, firstSSubModel);
             k--;
         }
         //对firstSSubModel内的每个submodel进行重排字典序
         for(String str : firstSSubModel){
             char[] strarr = str.toCharArray();
             Arrays.sort(strarr);
             str = "";
             for(char c : strarr){
                 str = str + c;
             }
             orderFirstSSubModel.add(str);
         }

         if(k==0 && firstSSubModel.size() == s-1){
             orderFirstSSubModel.add(featureName);
         }

         if(k==0 && firstSSubModel.size() < s-1){
          //   System.out.println("无法生成满足s个的子model");
             orderFirstSSubModel.add(featureName);
         }
        // System.out.println("扩展后的模式"+orderFirstSSubModel);
         return orderFirstSSubModel;
     }

 public static void gen_keepK_SubModel(String prefix, String model, int k, int s, ArrayList<String> firstSSubModel){
         if(model.length() < k){
             return;
         }
         if(model.length() == k){
             firstSSubModel.add(prefix + model);
             return;
         }
         //model.length() > k
         if(k>1){
             for(int i=0; i<model.length(); i++) {
                 String keep = String.valueOf(model.charAt(i));
                 gen_keepK_SubModel(prefix + keep, model.substring(i+1), k-1, s, firstSSubModel);
                 if(firstSSubModel.size() == s){
                     break;
                 }
             }
         }else{
             for(int i=0; i<model.length(); i++){
                 firstSSubModel.add(prefix + model.charAt(i));
                 if(firstSSubModel.size() == s){
                     break;
                 }
             }
         }
     }
 public double getFeatureNumber(String featureName, List<String> orderFirstSSubModel, Map<String, List<String>> tableInstance) {
     int num = 0;
     int orderFirstSSubModel_size = orderFirstSSubModel.size();
     Map<String, Integer> instanCountMap = new HashMap<>();
     for (String model : orderFirstSSubModel) {
    	// System.out.println(model);
         Map<String, Integer> modelInstanMap = new HashMap<>();
         List<String> instanceList = tableInstance.get(model);
        // System.out.println(instanceList);
         if(instanceList != null){
             for(int i=0;i<model.length();i++) {
                 if(featureName.equals(Character.toString(model.charAt(i)))) {
                     for (int j = 0; j < instanceList.size(); j++) {
                         String[] lineArray = instanceList.get(j).split(",");
                         String instanceId = lineArray[i] ;
                         modelInstanMap.put(instanceId, 1);
                     }
                 }
             }
             for(String instanceId:modelInstanMap.keySet()){
                 if(instanCountMap.containsKey(instanceId)){
                     instanCountMap.put(instanceId, instanCountMap.get(instanceId) + 1);
                 }else{
                     instanCountMap.put(instanceId, 1);
                 }
             }
         }else{
             orderFirstSSubModel_size --;
         }
     }
     for(String instanceId:instanCountMap.keySet()){
         if(orderFirstSSubModel_size == instanCountMap.get(instanceId)){
             num++;
         }
     }
    // System.out.println(num);
     return Double.valueOf(num);
 }
// public int getFeatureNumber(String featureName,List<String>orderFirstSSubModel, Map<String, List<String>> tableInstance) {
//	 //System.out.println(table);
//	  int num = 0;
//	//  Map<String, Integer> instanCountMap = new HashMap<>();
//	   for (String model : orderFirstSSubModel) {
//		 System.out.println(model);
//	    List<String> instanceList = tableInstance.get(model);
//	    System.out.println(instanceList);
//	    if(instanceList==null) {
//	    	continue;
//	    }else {
//	    	//List<Integer> integers1 = new ArrayList<Integer>();
//	    	for(int i=0;i<model.length();i++) {
//	    		if(featureName.equals(Character.toString(model.charAt(i)))) {
//	    			List<Integer> integers = new ArrayList<Integer>();
//	    			for (int j = 0; j < instanceList.size(); j++) {
//	    				String[] lineArray = instanceList.get(j).split(",");
//	    				if (integers.contains(Integer.valueOf(lineArray[i]))) {
//	    				}else {
//	    					//num++;
//	    					integers.add(Integer.valueOf(lineArray[i]));
//	    					num=integers.size();
//	    					 System.out.println("shuliang"+num);
//	    				}
//	    				
//	    			}
//	    			//num=integers.size();
//	    		}
//	    		
//	    	}
//	    	
//	    }
//	   }
	  
	  
//	  //System.out.println(tableInstance.keySet());
//	  Map<String, Integer> instanCountMap = new HashMap<>();
//	   for (String model : orderFirstSSubModel) {
//	    Map<String, Integer> modelInstanMap = new HashMap<>();
//	    System.out.println(model);
//	    List<String> instanceList = tableInstance.get(model);
//	    System.out.println(instanceList);
//	    for(int i=0;i<model.length();i++) {
//	     if(featureName.equals(Character.toString(model.charAt(i)))) {
//	    	// System.out.println(instanceList.size()); 
//	      for (int j = 0; j < instanceList.size(); j++) {
//	       String[] lineArray = instanceList.get(j).split(",");
//	       
//	        String instanceId = lineArray[i];
//	        
//	            modelInstanMap.put(instanceId, 1);
//	        
//	      }
//	     }
//	    }
//	    for(String instanceId:modelInstanMap.keySet()){
//	      if(instanCountMap.containsKey(instanceId)){
//	            instanCountMap.put(instanceId, instanCountMap.get(instanceId) + 1);
//	      }else{
//	            instanCountMap.put(instanceId, 1);
//	      }
//	    }
//
//	  }
//	   for(String instanceId:instanCountMap.keySet()){
//	    if(orderFirstSSubModel.size() == instanCountMap.get(instanceId)){
//	      num++;
//	    }
//	   }
//	   System.out.println("shuliang"+num);
//	
//	   return num;
// }
	/**
	 * 输出高效用模式
	 * @param args
	 */
	public Temp select_Hau_co_location(Map<String, List<String>> tableInstance){
		List<String> Ck = new ArrayList<String>();
		List<String>Hau_colocation=new ArrayList<String>();
		 for(Map.Entry<String, List<String>>entry:tableInstance.entrySet()) {
			 String model = entry.getKey();
			// System.out.println(model);
			 double hau=calculate_Hau(entry.getKey(), entry.getValue());
			 Ck.add(entry.getKey());
			 // System.out.println(entry.getKey()+hau/utilitySum);
			 if(hau/utilitySum>=min_u) {
				 Hau_colocation.add(entry.getKey()); 
				 Hu.put(entry.getKey(),entry.getValue());
				 //System.out.println(Hu); 
			 }
			 //System.out.println(Hu);
			 else { 
				 Set<String> feature=featureMap.keySet();  
				 //System.out.println("特征"+feature);
				 List<String> featureList = new ArrayList<String>();
				 feature.forEach(x -> featureList.add(x));
				// System.out.println("测试"+featureList);
				 List<String> modelFeatureList = new ArrayList<String>();
				 //System.out.println("模式"+model);
				 for (int i = 0; i < model.length(); i++) {
					 modelFeatureList.add(Integer.toString(model.charAt(i)));
				 }
				// System.out.println("候选"+modelFeatureList);
				 List<String> featureListTemp = new ArrayList<String>();
				 featureListTemp.addAll(featureList);
				 //System.out.println("候选"+ featureListTemp);
				 featureListTemp.removeAll(modelFeatureList);
				// System.out.println("候选1"+featureListTemp);
				 double epur = 0;
				//System.out.println("需要扩展的模式"+ model);
				 for (String f : featureListTemp) {
					 String modelSend = new String(model);
					// System.out.println("候选"+ f);
					 for (int i = 0; i < model.length(); i++) {
						// System.out.println("候选1"+ model.charAt(i));
//						 if (f.charAt(0) > model.charAt(i)) {
//							 if (i < model.length() - 1) {
//								  modelSend = model.substring(0, i) + f + model.substring(i + 1);
//								 //modelSend= model+f;
//								 System.out.println("候选1"+modelSend);
//								 break;
//							 }
//						 }
//					 }
//						 if(f.charAt(0) < model.charAt(i)){
//								if(i == 0){
//									modelSend = f + model;
//									//System.out.println("候选1"+modelSend);
//								}else if(f.charAt(0) != model.charAt(i-1)){
//									modelSend = model.substring(0,i) + f + model.substring(i);
//									//System.out.println("候选1"+modelSend);
//								}
//								break;
//							}else if(i == model.length()-1 && f.charAt(0) != model.charAt(i)){
//								modelSend = model + f;
////
//							}
//						
//					 }
 					 //System.out.println("扩展后的模式"+modelSend);
//					 epur += getVss(f, modelSend, s) / utilitySum;
//					// System.out.println("候选"+epur);
						 
						 if(f.charAt(0) < model.charAt(i)){
							 if(i == 0){
							  modelSend = f + model;
							 // System.out.println("【 候选1 】："+modelSend);
//							  System.out.println("测试 "+f+","+modelSend);
							  epur += getVss(f, modelSend, s) / utilitySum;
							 }else if(f.charAt(0) != model.charAt(i-1)){
							  modelSend = model.substring(0,i) + f + model.substring(i);
							 // System.out.println("【 候选1 】"+modelSend);
//							  System.out.println("测试 "+f+","+modelSend);
							  epur += getVss(f, modelSend, s) / utilitySum;
							 }
							 break;
						}else if(i == model.length()-1 && f.charAt(0) != model.charAt(i)){
							 modelSend = model + f;
							// System.out.println("【 候选1 】"+modelSend);
//							 System.out.println("测试 "+f+","+modelSend);
							 epur += getVss(f, modelSend, s) / utilitySum;
						}
						 
				 }
				 }
				 epur += hau/utilitySum;
				 if (epur < min_u) {
					 Ck.remove(model);
				 }
			 }
			 
		 }
		// System.out.println("候选"+ Ck);
		 
		 Temp temp = new Temp(Hau_colocation, Ck);
		 return temp;
		 
	}
	
	public void write(List<String>Hu_colocation, Map<String, List<String>> tableInstance) {
		File file = new File("D://HUColocation.txt");
		if (txt2) {
			file.delete();
			txt2 = false;
		}
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Pattern> patternList = new ArrayList<Pattern>();
		//List patternlist1=new ArrayList();
		 
		
		for (String model : Hu_colocation) {
			// System.out.println("100"+Hu_colocation);
			 patternList.add(new Pattern(model, getModelUtility(model, tableInstance)));
			// patternlist1.add(new Pattern1(model));	 
		 }
		 
		 Collections.sort(patternList);
		  
//		 double qc = 0;
//		 double lower = 0;
//		 double m=0;
//		 double n=0;
//		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
//			// System.out.println(patternList.size());
//			 qc = patternList.get(i - 1).utility;
////			 System.out.println("模式"+qc);
//			 lower = getModelAllFeatureUtility(patternList.get(i - 1).pattern);
//		//     System.out.println("效用"+lower);
//			 m+=qc/lower;
////			 System.out.println("效用"+m);
//			 n=m/kNum;
//		 }
////		 
//		 qc = n;
////		 System.out.println("效用"+qc);
//		 int frequencyNum = 0;
//		 double pc=0;
//		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
//			// System.out.println("阶数"+patternList.get(i - 1).pattern);
//			 pc=Frequency(patternList.get(i - 1).pattern, tableInstance);
//				 frequencyNum++;
//			 }
		 
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		int i = 0;
		for (String str : Hu_colocation) {
			try {
				if (i == 0) {
					bufferedWriter.write("\n" + str.length() + "阶高效用模式个数为：" + Hu_colocation.size() + "\n");
					bufferedWriter.flush();
//		   			bufferedWriter.write("\n" + str.length() + "阶高效用模式,Top-" + kNum + "的Qc值：" + qc + "\n"
//		   					+ str.length() + "阶高效用模式，Top-" + kNum + "的pc值" + pc + "\n");
//		   			bufferedWriter.flush();
		   			
				}
				bufferedWriter.write(str + "\t");
			} catch (IOException e) {
				// TODO Auto-ge nerated catch block
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
	public void run() throws IOException {
		readInstance("D://CoLocation/12.csv");
		int k=2;
		gen_table_ins2();
		List<String> list=new ArrayList<String>();
		for (Map.Entry<String, List<String>> entry : table2.entrySet()){
			list.add(entry.getKey());
		}
		Map<String,List<String>> tableinstanceLast = new HashMap<String, List<String>>();
		Map<String,List<String>> tableinstance = new HashMap<String, List<String>>();
		tableinstanceLast.putAll(table2);
		while(true) {
			List<String>candicate=new ArrayList<String>();
			
			if(k==2) {
				candicate.addAll(list);
				tableinstance.putAll(table2);    
				
			}
			else {
			  //System.out.println("list"+list);
			  candicate= gen_cancidate_Hau(list,k);
			   tableinstance=gen_table_instance(candicate, tableinstanceLast);
			 }
			 if(candicate.size()==0) {
	  			 break;
	  		  }
			// System.out.println("候选模式"+candicate);
			 File file = new File("D://HUColocationChau.txt");
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
		   			bufferedWriter.write("\n" + candicate.get(0).length() + "阶候选模式个数：" + candicate.size());
		   			bufferedWriter.flush();
		   			bufferedWriter.newLine();
	   			} catch (IOException e) {
	   				
	   			}
	   			
	   			for (String str : candicate) {
//	   				System.out.println(str);
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
			// Map<String,List<String>> tableinstance=gen_table_instance(candicate, tableinstanceLast);
	   		 //System.out.println("表"+tableinstance);
	   		 table.putAll(tableinstance);
	   		 //System.out.println("表"+table);
			 Temp t = select_Hau_co_location(tableinstance);
			 List<String> Hu_colocation = t.strList1;
			// System.out.println("测试1"+Hu_colocation);
			 write(Hu_colocation, tableinstance);
			// Hu.putAll(Hu_colocation,tableinstance);
			 list = t.strList2;
			 //System.out.println("测试2"+list);
			 k++;
			 tableinstanceLast = tableinstance;	 
		}
		H.putAll(Hu);
		//System.out.println(H);
		write1(H);
	}
	public void write1(Map<String,List<String>>tableInstance)throws IOException {
		List<Pattern> patternList = new ArrayList<Pattern>();
		 for(Map.Entry<String, List<String>>entry:tableInstance.entrySet()) {
			 //System.out.println(entry.getKey());
			 patternList.add(new Pattern(entry.getKey(), getModelUtility(entry.getKey(), tableInstance)));
			 
		 }
		 Collections.sort(patternList);
//		for(int a=1;a<= patternList.size();a++) {
//			System.out.println(patternList.get(a - 1).pattern);
//			System.out.println(patternList.get(a - 1).utility);
//		}
		 double qc = 0;
		 double lower = 0;
		 double m=0;
		 double n=0;
		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
			// System.out.println(patternList.size());
			 qc = patternList.get(i - 1).utility;
//			 System.out.println("模式"+qc);
			 //System.out.println(patternList.get(i - 1));
	 		 
			 lower = getModelAllFeatureUtility(patternList.get(i - 1).pattern);
			 
		   //  System.out.println(patternList.get(i - 1).pattern);
			 m+=qc/lower;
//			 System.out.println("效用"+m);
			 n=m/kNum;
			//  System.out.println("高效用模式"+n);
		 }
		 
		 qc = n;
		 System.out.println("高效用模式"+qc);
		 double p=0;
		 for (int i = 1; i <= kNum && i <= patternList.size(); i++) {
			// System.out.println("阶数"+patternList.get(i - 1).pattern);
//			 if (isFrequency(patternList.get(i - 1).pattern, tableInstance)) {
//				 frequencyNum++;
//			 }
			
			p+=Frequency(patternList.get(i - 1).pattern, tableInstance); 
			//System.out.println("值"+ p);
		 }
		 double pu=p/kNum;
		 System.out.println("频繁模式"+pu);
		
	}
	
	
	
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
		List<String> instanceList = tableInstance.get(model);
		double modelUtility = 0;
		for (int i = 0; i < model.length(); i++) {
			String featureName = Character.toString(model.charAt(i));
			//System.out.println("特征"+featureName);
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
		double n=0;
		for (int i = 0; i < model.length(); i++) {
			int featureInstanceNum = 0;
			String featureName = Character.toString(model.charAt(i));
//			System.out.println("测试"+featureName);
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
			} else {
				
			}
//			System.out.println(model);
//			System.out.println("测试啊"+m);
			//System.out.println("测试1"+featureInstanceNum);
			//System.out.println("测试2"+featureNumMap.get(featureName)); 
			//System.out.println("测试"+featureNumMap.get(featureName));
//			if (m >= frequencyThreshold){
//				n+=m;
//				
//			} ·
			//System.out.println("平均"+n);	 
			}
//		}
		//System.out.println(m);
 		return m;
	}	
	public static void main(String[] args) throws IOException {
		long start=System.currentTimeMillis();
		Hu hu = new Hu(300, 0.01);
		hu.run(); 
		long end=System.currentTimeMillis();
		System.out.println("runtime:"+(end-start)/1000 + "s");
	}

}