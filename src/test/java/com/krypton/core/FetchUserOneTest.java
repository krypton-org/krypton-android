package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FetchUserOneTest {

	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				client.register("fetchuser" + String.valueOf(i) + "@example.com", password).get();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	@Test
	public void testFetchUser() throws Exception {
		HashMap<String, Object> filter = new HashMap<String, Object>();
		filter.put("email_verified", false);
		String[] requestedFields = { "_id", "email_verified" };

		Map<String, Object> res = client.fetchUserOne(filter, requestedFields).get();
		assertEquals(res.get("email_verified"), false);
		assertNotNull(res.get("_id"));

	}

	@AfterAll
	public static void cleanUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				client.login("fetchuser" + String.valueOf(i) + "@example.com", password).get();
				client.delete(password).get();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
