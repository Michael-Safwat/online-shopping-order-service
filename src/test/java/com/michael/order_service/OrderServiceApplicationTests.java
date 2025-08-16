package com.michael.order_service;

import com.michael.order_service.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.testcontainers.containers.MySQLContainer;
import static org.hamcrest.MatcherAssert.assertThat;

@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationTests {

	@ServiceConnection
	static MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.3.0");
	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setup(){
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mySQLContainer.start();
	}

	@Test
	void shouldCreateOrder() {
		String skuCode = "iphone_15";
		Integer quantity = 1;

		String requestBody = String.format("""
				{
				     "skuCode":"%s",
				     "price": 1,
				     "quantity": %d
				}
				""",skuCode, quantity);

		InventoryClientStub.stubInventoryCallInStock(skuCode, quantity);

		var responseBodyString = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/order")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBodyString, Matchers.is("order placed successfully"));
	}

	@Test
	void shouldNotCreateOrder() {
		String skuCode = "iphone_15";
		Integer quantity = 1000;

		String requestBody = String.format("""
				{
				     "skuCode":"%s",
				     "price": 1,
				     "quantity": %d
				}
				""",skuCode, quantity);

		InventoryClientStub.stubInventoryCallOutOfStock(skuCode, quantity);

		var responseBodyString = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/order")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBodyString, Matchers.is("Product with SkuCode: "+ skuCode+" is not in stock"));

	}

}
