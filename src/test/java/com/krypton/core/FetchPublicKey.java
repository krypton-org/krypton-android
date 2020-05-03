package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class FetchPublicKey {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");

	@Test
	public void testFetchPublicKey() throws Exception{
		String res = client.publicKey();
		assertTrue(res instanceof String);
		assertTrue(res.contains("BEGIN PUBLIC KEY"));

	}
}
