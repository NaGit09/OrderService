package com.furniro.OrderService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceApplicationTests {

	static {
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
	}

	@Test
	void contextLoads() {
	}

}
