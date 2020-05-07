package com.krypton.core.internal.data;

import java.util.HashMap;
import java.util.Map;

public class User {
	private static final String EMAIL_KEY = "email";
	private static final String ID_KEY = "_id";
	private static final String VERIFIED_KEY = "verified";
	private boolean isVerified;
	private String _id;
	private String email;
	private Map<String, Object> otherFields;

	public User(boolean isVerified, String id, String email, Map<String, Object> otherFields) {
		this.isVerified = isVerified;
		this._id = id;
		this.email = email;
		this.otherFields = otherFields;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public String getId() {
		return _id;
	}

	public String getEmail() {
		return email;
	}

	public Object getField(String key) {
		return otherFields.get(key);
	}

	public static User convertMapToUser(Map<String, Object> map) {
		HashMap<String, Object> shallowCopy = new HashMap<String, Object>(map);
		shallowCopy.remove(VERIFIED_KEY);
		shallowCopy.remove(ID_KEY);
		shallowCopy.remove(EMAIL_KEY);
		return new User((boolean) map.get(VERIFIED_KEY), (String) map.get(ID_KEY), (String) map.get(EMAIL_KEY),
				shallowCopy);
	}
}
