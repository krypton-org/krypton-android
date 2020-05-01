package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoginTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "user1@example.com";
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		try {
			client.register(email, password);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testLogin() {
		try {
			assertFalse(client.isLoggedIn());
			Map<String, Object> res = client.login(email, password);
			assertEquals(res.get("email"), email);
			assertNotNull(res.get("_id"));
			assertFalse((boolean) res.get("verified"));
			assertTrue(client.isLoggedIn());
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@AfterAll
	public static void cleanUp() {
		try {
			client.login(email, password);
			client.delete(password);
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}
