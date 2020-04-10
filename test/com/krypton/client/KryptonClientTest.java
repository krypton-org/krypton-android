package com.krypton.client;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class KryptonClientTest {
	@Test
	public void test1() {
		KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
		try {
			client.login("toto@toto.com", "totototo");
		} catch (IOException e) {
			fail();
		}
	}
}
