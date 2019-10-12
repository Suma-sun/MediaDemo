package com.suma.mediademo.utils;

import java.util.Locale;

/**
 * 字符串工具类 <br>
 * 提供格式化功能
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-12]
 */
public class StringUtils {

	public static String format(String format, Object... args) {
		return String.format(Locale.CHINA, format, args);
	}
}
