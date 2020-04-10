package com.krypton.queries;

import java.util.HashMap;

public class LoginQuery extends Query {
	public LoginQuery(HashMap<String, Object> variables) {
		super(variables);
		this.getQuery();
	}
	public void getQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("mutation login($email: String!, $password: String!) {")
		  .append("login(email: $email, password: $password) {")
		  .append("token")
		  .append("}}");
		this.query = sb.toString();
	}

}
