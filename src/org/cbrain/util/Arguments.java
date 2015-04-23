package org.cbrain.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
/**
 * 读取命令行参数
 */
public class Arguments {
 
    private Map<String, Object> arguments = new HashMap<String, Object>();
 
    /**
     * 构造方法
     *
     * @param args 命令行参数
     */
    public Arguments(String... args) {
        String argName = "", argValue = "";
 
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (argName.length() > 0) {// 上一组参数
                    arguments.put(argName, argValue.trim());
                    argValue = "";
                }
                argName = arg.substring(1);
            } else {
            	if(argName.equals("w")){
            	    argValue += " " + arg.trim().replaceAll("\\s+", "&");
            	}else{
                    argValue += " " + arg;
            	}
            }
        }
 
        if (argValue.length() > 0) {
            arguments.put(argName, argValue.trim());
        }
    }
 
    /**
     * 获取字符串值
     *
     * @param key 参数名
     *
     * @return 参数值
     */
    public String getString(String key) {
        return String.valueOf(arguments.get(key));
    }
    
    public String getString(String key,String defaultValue) {
    	if(arguments.containsKey(key)){
            return String.valueOf(arguments.get(key));
    	}else{
    		return defaultValue;
    	}
    }
 
    /**
     * 获取数字值
     *
     * @param key          参数名
     * @param defaultValue 缺省值
     *
     * @return 参数值
     */
    public int getInteger(String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
 
    /**
     * 判断命令行参数是否存在指定的选项。用于开关式选项
     *
     * @param key 选项
     *
     * @return 如果存在则返回 true
     */
    public boolean hasOption(String key) {
        return arguments.containsKey(key);
    }
    
    public static List<String> listPostProcess(String str){
    	String[] tmp=str.split("\\s+");
    	List<String> list=new ArrayList<String>();
    	for(String w:tmp){
    		list.add(w.replaceAll("&", " "));
    	}
    	return list;
    } 
    
	/**
	 * 参数说明：
	 *  1) w : search words 支持多组查询，单组查询中若有多个词则用&连起来. 使用demo: -w 中国&军演  反恐  （demo中含两组查询词，第一组 ”中国&军演“ ， 第二组 ”反恐“）
	 *  2) o : output directory.  使用demo: -o ./data/baidu
	 *  3) n : upper number of crawled documents for each group of search words.
	 */
    public static void main(String[] args){
    	
    	//参数举例： -w "中国 军演" 反恐 -o ./data/baidu -n 100
    	Arguments arguments=new Arguments(args);
    	List<String> keywords=listPostProcess(arguments.getString("w"));
    	int n=arguments.getInteger("n", 100);
    	String outPath=arguments.getString("o");
    	System.out.println("search words : "+keywords);
    	System.out.println("num : "+n);
    	System.out.println("outPath : "+ outPath);
    	
    	
    	
    }
    
}