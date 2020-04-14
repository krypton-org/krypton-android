package main.java.com.krypton.queries;

import java.util.HashMap;

public class SendVerificationEmailQuery extends Query {
	
	public SendVerificationEmailQuery() {
		this.getQuery();
	}
	public void getQuery() {	
		StringBuilder sb = new StringBuilder();
		sb.append("query {")
		  .append("sendVerificationEmail")
		  .append("}");
		this.query = sb.toString();
	}

}
