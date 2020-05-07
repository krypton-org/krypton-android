package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.krypton.core.internal.data.User;

public class RefreshTokenTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "refreshtoken@example.com";
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
	public void testRefreshToken() throws Exception {

		User user = client.login(email, password);
		assertEquals(user.getEmail(), email);
		Date beforeUpdateDate = client.getExpiryDate();

		client.refreshToken();
		assertTrue(beforeUpdateDate.getTime() < client.getExpiryDate().getTime());

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
