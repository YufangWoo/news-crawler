package org.cbrain.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.chineseutils.ChineseUtils;

public class FileUtil {
	private static final Logger log=	LogPattern.getInstance(FileUtil.class.getName());
	
	public static void checkAndRemove(String filePath){//删除已存在的文件
		File file=new File(filePath);
		if(file.exists()){
			file.delete();
			log.info("Delete the old file .....");
		}
	}
	
	public static boolean checkDir(String dir){
		 File dirFile = new File(dir);
		 boolean bFile = dirFile.exists();
			if (!bFile) {//不存在，创建
				FileUtil.mkdir(dir);
				return false;
			} 
			return true;	
	}
	public static void saveAsLine(String file, List<String> doc) {
		//追加写文件，每篇doc存成一行
		PrintWriter pw = null;
		try {
			 pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,true),"UTF-8"));
			for (String line:doc) {
				pw.print(ChineseUtils.tradToSimp(line.replaceAll("\\s+|　　|    |　", "").replaceAll("&[a-z]+[;]?", "")));//繁体转简体
			}
			pw.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	
	public static void save(String dir, String url, String author,List<String> doc){
		PrintWriter pw = null;
		try {
			 pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(dir+author+".txt"),"UTF-8"));
			 //第一行存url，方便检查doc提取的质量
			pw.println("URL>> "+url+"\n");
			for (int i = 0; i < doc.size(); i++) {
				pw.println(doc.get(i) );
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	public static boolean safeWrite(String dir, String url, String author,List<String> doc) {
		/**
		 * 存储之前判断文件是否已存在。文件以新闻来源和时间命名
		 * 返回值：true-存储成功， false-存储失败
		 */
		
		String filepath=dir+author+".txt";
		File file=new File(filepath);
		if(file.exists()) {//与存在则返回
			log.info("Already exist!!!");
			return false;
		}
		if(doc.size()==0){
			log.info("Empty doc!!");
			return false;
		}
		save(dir,url,author,doc);
		log.info("Save document successfully!!");
		return true;
	}
	
	public static void mkdir(String dirName) {
		try {
			 File dirFile = new File(dirName);
			boolean bFile = dirFile.exists();
			if (bFile == true) {
				log.info("The folder exists.");
			} else {
				log.info("The folder do not exist,now trying to create a one...");
				bFile = dirFile.mkdirs();
				if (bFile == true) {
					log.info("Create successfully!");
				} else {
					log.error("Disable to make the folder,please check the disk is full or not.");
				}
			}
		} catch (Exception err) {
			log.error("ELS - Chart : unexpected error");
			err.printStackTrace();
		}
	}
	
	//读取一个文件夹下的所有文件夹和文件
	public static List<String> readAllFileName(String inFilePath) {
		File f =  new File(inFilePath);
		File[] files = f.listFiles(); // 得到f文件夹下面的所有文件。
//		log.info(files.length);
		List<String> list = new ArrayList<String>();
		for (File file : files) {
			String fileName=file.getName();
			list.add(fileName.substring(0,fileName.indexOf(".")));
		}
		return list;		
	}
	
	public static File checkExist(String filepath) throws Exception{
	       File file=new File(filepath);
	      
	       if (file.exists()) {//判断文件或目录是否存在
	           log.info("File or Directory exists!!");
	           if(file.isDirectory()){//是否为目录
	                 log.info("It's Directory!!");      
	             }else{
//	              file.createNewFile();//创建文件
	              log.info("It's File!!" );      
	             }
	       }else {
	           log.info("File or Directory doesn't exist!!");
	           if(file.isDirectory()){//是否为目录
	        	   file.mkdirs();
		           log.info("Create directory successfully!! ");
	           }else{
	        	   File file2=new File(file.getParent());
	        	   if(!file2.exists()){
	        		   file2.mkdirs();
			           log.info("Step1: Create directory successfully!!"   );   
	        	   }
		           file.createNewFile();//创建文件 
		           log.info("Step 2: Create file successfully!!"   );   
	           }
	       }
	       return file;
	    }
	
	public static void writeLines(String file, ArrayList<String> lines,boolean append) {
		PrintWriter pw = null;
		try {
			 pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,append),"UTF-8"));
			 pw.println(lines.size()+"failed urls.....   "+new Date());//第一行存行数和写入时间
			for (String line:lines) {
				pw.println(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	public static List<String> readSearchFile(String file){
		ArrayList<String> lines=new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.trim().length()==0) continue;//去掉空行
				lines.add(line.trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}

	public static void main(String[] args) throws Exception{
//		checkExist("res/wyf/wyf3.txt");
	}
}
