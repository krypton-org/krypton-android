package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FetchUserManyTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				client.register("fetchusermany" + String.valueOf(i) + "@example.com", password).get();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	@Test
	public void testFetchManyUsers() throws Exception {
		HashMap<String, Object> filter = new HashMap<String, Object>();
		filter.put("email_verified", false);
		String[] requestedFields = { "_id", "email_verified" };
		List<Map<String, Object>> res = client.fetchUserMany(filter, requestedFields, 4).get();
		assertEquals(res.size(), 4);
		Map<?, ?> res2 = res.get(0);
		assertNotNull(res2.get("_id"));
		assertEquals(res2.get("email_verified"), false);
		filter.put("email_verified", true);
		res = client.fetchUserMany(filter, requestedFields).get();
		assertEquals(res.size(), 0);

	}

	@AfterAll
	public static void cleanUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				client.login("fetchusermany" + String.valueOf(i) + "@example.com", password).get();
				client.delete(password).get();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
