package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FetchUserCountTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				client.register("fetchusercount" + String.valueOf(i) + "@example.com", password);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	@Test
	public void testFetchUserCount() throws Exception {
		assertTrue(client.fetchUserCount() >= 5);
		HashMap<String, Object> filter = new HashMap<String, Object>();
		filter.put("verified", false);
		assertTrue(client.fetchUserCount(filter) >= 5);
		filter.put("verified", true);
		assertTrue(client.fetchUserCount(filter) == 0);

	}

	@AfterAll
	public static void cleanUp() {
		for (int i = 1; i <= 5; i++) {
			try {
				client.login("fetchusercount" + String.valueOf(i) + "@example.com", password);
				client.delete(password);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
