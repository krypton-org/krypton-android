package com.krypton;

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class KryptonClientTest {
	@Test
	public void test1() {
		KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
		try {
			client.register("toto@toto.com", "totototo");
		} catch (Exception e) {
			fail();
		}
		try {
			client.login("toto@toto.com", "totototo");
		} catch (Exception e) {
			fail();
		}
	}
}
