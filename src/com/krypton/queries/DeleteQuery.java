package com.krypton.queries;

import java.util.HashMap;

public class DeleteQuery extends Query {
	
	public DeleteQuery(HashMap<String, Object> variables) {
		super(variables);
		this.getQuery();
	}
	public void getQuery() {	
		StringBuilder sb = new StringBuilder();
		sb.append("mutation deleteMe($password: String!) {\n")
		  .append("deleteMe(password: $password)\n")
		  .append("}\n}");
		this.query = sb.toString();
	}
}
