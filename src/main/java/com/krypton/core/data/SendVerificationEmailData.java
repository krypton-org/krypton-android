package com.krypton.core.data;

import java.util.List;
import java.util.Map;

public class SendVerificationEmailData implements QueryData {
	public Map<String, Boolean> data;
	public List<Map<String, String>> errors;

	public Map<String, Boolean> getData() {
		return this.data;
	}

	public List<Map<String, String>> getErrors() {
		return this.errors;
	}

}