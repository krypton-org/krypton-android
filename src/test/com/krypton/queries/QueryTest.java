package test.com.krypton.queries;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import main.java.com.krypton.queries.LoginQuery;

public class QueryTest {
	@Test
	public void test1() {
		HashMap<String,Object> parameters = new HashMap<String, Object>();
		parameters.put("email","test@gmail.com");
		parameters.put("password","test");
		LoginQuery query = new LoginQuery(parameters);
		System.out.println(query.toJson());
	}

}
