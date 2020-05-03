package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FetchUserByIdsTest {

	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				boolean isRegistered = client.register("fetchuserbyid" + String.valueOf(i) + "@example.com", password);
				assertTrue(isRegistered);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	@Test
	public void testFetchUserByIds() {
		try {
			HashMap<String, Object> filter = new HashMap<String, Object>();
			filter.put("verified", false);
			String[] requestedFields = { "_id" };

			Map<?,?>[] res = client.fetchUserMany(filter, requestedFields, 4);
			assertEquals(res.length, 4);
			assertNotNull(res[0].get("_id"));
			String[] requestedFields2 = { "_id", "verified" };
			ArrayList<String> data = new ArrayList<String>();
			for (int i = 0; i < res.length; i++) {
				data.add((String) res[i].get("_id"));
			}
			client.fetchUserByIds(data, requestedFields2);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@AfterAll
	public static void cleanUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				client.login("fetchuserbyid" + String.valueOf(i) + "@example.com", password);
				client.delete(password);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
