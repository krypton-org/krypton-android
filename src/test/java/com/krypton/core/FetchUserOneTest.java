package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FetchUserOneTest {
	
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String password = "ex@mplePassword123";
	static String email = "fetchuser@test.com";
	
	@BeforeAll
	public static  void setUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				boolean isRegistered = client.register("fetchuser" + String.valueOf(i) + "@example.com", password);
				assertTrue(isRegistered);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	@Test
	public void testFetchUser() {
		try {
			client.login("fetchuser" + String.valueOf(1) + "@example.com", password);
			HashMap<String, Object> filter = new HashMap<String, Object>();
			filter.put("verified", false);
			String[] requestedFields = {"_id", "verified"};

			Map<String, Object> res = client.fetchUserOne(filter, requestedFields);
			assertEquals(res.get("verified"),false);
			assertNotNull(res.get("_id"));

		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	@AfterAll
	public static  void cleanUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				client.login("fetchuser" + String.valueOf(i) + "@example.com", password);
				client.delete(password);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
