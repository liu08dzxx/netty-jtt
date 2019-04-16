package com.vtradex.ehub.jtt.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    public static SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 转换为UTC时间
     * @param date
     * @return
     * @throws ParseException
     */
    public static Date convertUTC(Date date,SimpleDateFormat format)  {
    	format.setTimeZone(TimeZone.getTimeZone("UTC"));
    	try {
			return format.parse(format.format(date));
		} catch (ParseException e) {
			throw new RuntimeException("UTC时间转换失败",e);
		}
    }
}
