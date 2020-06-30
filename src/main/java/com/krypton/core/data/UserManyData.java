package com.krypton.core.data;

import java.util.List;
import java.util.Map;

public class UserManyData implements QueryData {
	public Map<String, List<Map<String, Object>>> data;
	public List<Map<String, String>> errors;

	@Override
	public Map<String, List<Map<String, Object>>> getData() {
		return this.data;
	}

	@Override
	public List<Map<String, String>> getErrors() {
		return this.errors;
	}
}
