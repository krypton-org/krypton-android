package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.krypton.core.data.User;

public class LoginTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "user1@example.com";
	static String password = "examplee";

	@BeforeAll
	public static void setUp() {
		try {
			client.register(email, password).get();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testLogin() throws Exception {

		assertFalse(client.isLoggedIn().get());
		User user = client.login(email, password).get();
		assertEquals(user.getEmail(), email);
		assertNotNull(user.getId());
		assertFalse((boolean) user.isVerified());
		assertTrue(client.isLoggedIn().get());

	}

	@AfterAll
	public static void cleanUp() {
		try {
			client.login(email, password).get();
			client.delete(password).get();
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}
