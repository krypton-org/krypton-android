package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SendVerificationEmailTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "send.verification.email@example.com";
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		try {
			client.register(email, password).get();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testSendEmailVerification() throws Exception {
		client.login(email, password).get();
		assertTrue(client.sendVerificationEmail().get());

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
