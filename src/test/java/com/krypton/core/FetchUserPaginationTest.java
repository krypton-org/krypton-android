package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.krypton.core.data.UserPaginationData.Pagination;

public class FetchUserPaginationTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		for (int i = 1; i <= 20; i++) {
			try {
				client.register("fetchuserpagination" + String.valueOf(i) + "@example.com", password).get();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	@Test
	public void testFetchUserPagination() throws Exception {
		HashMap<String, Object> filter = new HashMap<String, Object>();
		filter.put("email_verified", false);
		String[] requestedFields = { "_id", "email_verified" };
		Pagination res = client.fetchUserWithPagination(filter, requestedFields, 1, 5).get();
		List<Map<String, Object>> items = res.getItems();
		assertTrue(items.size() >= 5);
		assertNotNull(items.get(0).get("_id"));
		assertEquals(items.get(0).get("email_verified"), false);
		assertEquals(res.getPageInfos().getCurrentPage(), 1);
		assertEquals(res.getPageInfos().getPerPage(), 5);
		assertTrue( res.getPageInfos().getPageCount() >= 4);
		assertTrue(res.getPageInfos().getItemCount() >= 20);
		assertTrue(res.getPageInfos().hasNextPage());
		assertFalse(res.getPageInfos().hasPreviousPage());

		filter.put("email_verified", true);
		res = client.fetchUserWithPagination(filter, requestedFields, 1, 5).get();
		items = res.getItems();
		assertTrue(items.size() == 0);
		assertTrue(res.getPageInfos().getItemCount() == 0);
	}

	@AfterAll
	public static void cleanUp() {
		for (int i = 1; i <= 20; i++) {
			try {
				client.login("fetchuserpagination" + String.valueOf(i) + "@example.com", password).get();
				client.delete(password).get();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
