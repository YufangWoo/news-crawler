package org.cbrain.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;  

public class TimeUtil {

	public static String modifyTimeFormat(String time){
		//时间可能未精确到秒
		int len=time.substring(time.indexOf("_")+1).length();
		int less=6-len;
		while(less>0){
			time+="0";
			less--;
		}
		if(time.indexOf("_")<0)
			time="_"+time;
		//时间可能缺少年月日
		less=15-time.length();
		Calendar now=Calendar.getInstance();
		switch(less){
		case 0:
			break;
		case 4: //缺年份
			int year=now.get(Calendar.YEAR);
			time=year+time;
			break;
		case 6: //缺年份和月份
			int year2=now.get(Calendar.YEAR);
			int month2=now.get(Calendar.MONTH);
			time=year2+""+((month2<9)?"0":"")+month2+time;
			break;
		case 8: //缺年月日
			int year3=now.get(Calendar.YEAR);
			int month3=now.get(Calendar.MONTH);
			int date3=now.get(Calendar.DATE);
			time=year3+""+((month3<9)?"0":"")+month3+((date3<9)?"0":"")+date3+time;
			break;
		}
		return time;
	}
	
	public static int compareTime(String time1, String time2) {
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
		long diff=0;
		try {
			Date d1 = df.parse(time1);
			Date d2 = df.parse(time2);
			diff = d1.getTime() - d2.getTime();

		} catch (ParseException e) {
			System.err.println("wrong time format!!!");
			e.printStackTrace();
			return 1;//时间格式错误则认为当前时间是最新的
		}
		if(diff>0)
			return 1;
		else if(diff<0)
			return -1;
		else 
			return 0;
	}
	
	public static void main(String[] args){
//		String time1="2012-02-09 19:53:00";//20120209_195300
//		String time2="2014-07-23 07:55:00";//20140723_075500
		String time1="20120209_195300";//20120209_195300
		String time2="20140723_075500";//20140723_075500
		System.out.println(compareTime(time2,time1));
		System.out.println(modifyTimeFormat("1023_2020"));
	}

}
