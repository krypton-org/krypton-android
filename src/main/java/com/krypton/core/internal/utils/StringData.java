package com.krypton.core.internal.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StringData implements QueryData {
	public Map<String, String> data;
	public List<Map<String, String>> errors;

	public Map<String, 	String > getData(){
			return data;
		}

	public List<Map<String, String>> getErrors() {
		return errors;
	}

}
