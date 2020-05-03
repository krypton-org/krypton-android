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

public class FetchUserPaginationTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		for (int i = 1; i <= 20; i++) {
			try {
				boolean isRegistered = client.register("fetchuserpagination" + String.valueOf(i) + "@example.com",
						password);
				assertTrue(isRegistered);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	@Test
	public void testFetchUserPagination() {
		try {
			HashMap<String, Object> filter = new HashMap<String, Object>();
			filter.put("verified", false);
			String[] requestedFields = { "_id", "verified" };
			Map<String, Object> res = client.fetchUserWithPagination(filter, requestedFields, 1, 5);
			ArrayList<Map> items = (ArrayList<Map>) res.get("items");
			assertTrue(items.size() >= 5);
			assertNotNull(items.get(0).get("_id"));
			assertEquals(items.get(0).get("verified"), false);
			Map pageInfo = (Map) res.get("pageInfo");
			assertEquals(pageInfo.get("currentPage"), 1.0);
			assertEquals(pageInfo.get("perPage"), 5.0);
			assertTrue((double) pageInfo.get("pageCount") >= 4);
			assertTrue((double) pageInfo.get("itemCount") >= 20);
			assertEquals(pageInfo.get("hasNextPage"), true);
			assertEquals(pageInfo.get("hasPreviousPage"), false);

			filter.put("verified", true);
			res = client.fetchUserWithPagination(filter, requestedFields, 1, 5);
			items = (ArrayList<Map>) res.get("items");
			assertTrue(items.size() == 0);
			pageInfo = (Map) res.get("pageInfo");
			assertTrue((double) pageInfo.get("itemCount") == 0);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@AfterAll
	public static void cleanUp() {
		for (int i = 1; i <= 20; i++) {
			try {
				client.login("fetchuserpagination" + String.valueOf(i) + "@example.com", password);
				client.delete(password);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
