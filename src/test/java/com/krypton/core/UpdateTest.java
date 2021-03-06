package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.krypton.core.data.User;

public class UpdateTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "updatetesddt1@example.com";
	static String password = "ex@mplePassword123";
	static String emailUpdate = "updatddde2@example.com";

	@BeforeAll
	public static void setUp() {
		try {
			client.register(email, password).get();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testUpdate() throws Exception {

		User user = client.login(email, password).get();
		System.out.println(user.getEmail());
		assertEquals(user.getEmail(), email);
		Date beforeUpdateDate = client.getExpiryDate();

		HashMap<String, Object> change = new HashMap<String, Object>();
		change.put("email", emailUpdate);
		user = client.update(change).get();
		assertEquals(user.getEmail(), emailUpdate);
		assertTrue(beforeUpdateDate.getTime() < client.getExpiryDate().getTime());

	}

	@AfterAll
	public static void cleanUp() {
		try {
			client.login(email, password).get();
			client.delete(password).get();
		} catch (Exception e) {
			;
		}
		try {
			client.login(emailUpdate, password).get();
			client.delete(password).get();
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}
