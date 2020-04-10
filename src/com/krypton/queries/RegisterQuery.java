package com.krypton.queries;

import java.util.HashMap;

public class RegisterQuery extends Query {
	
	public RegisterQuery(HashMap<String, Object> variables) {
		super(variables);
		this.getQuery();
	}
	
	public void getQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("mutation register($fields: UserRegisterInput!) {\n")
		  .append("register(fields: $fields)\n")
		  .append("}\n");
		this.query = sb.toString();
	}
}
