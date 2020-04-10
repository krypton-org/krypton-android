package com.krypton.queries;
import java.util.HashMap;

public class UpdateQuery extends Query {
	public UpdateQuery(HashMap<String, Object> variables) {
		super(variables);
		this.getQuery();
	}
	public void getQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("mutation updateMe($fields: UserUpdateInput!) {\n")
		  .append("updateMe(fields: $fields) {\n")
		  .append("token\n")
		  .append("}\n}");
		this.query = sb.toString();
	}
	
}
