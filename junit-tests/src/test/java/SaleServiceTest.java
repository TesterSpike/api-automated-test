import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import model.client.Client;
import model.sale.Product;
import model.sale.SaleRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.SaleService;
import stub.StubHandler;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaleServiceTest {

    @BeforeAll
    static void setup() {
        new StubHandler().withAuthStub().withSaleStub().start();
    }

    @Test
    void requestWithNoTokenWillError() {
        System.out.println("5.2) A request with no token will be given an appropriate error response");

        Response response = new SaleService().ping();
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusCode(), "Http status");
        JsonPath json = response.jsonPath();
        assertEquals("error", json.getString("status"), "Message status");
        assertEquals("Missing Token", json.getString("reason"), "Message reason");
    }

    @Test
    void pingRequestWithTokenWillGetServiceStatus() {
        System.out.println("5.1) All requests to services must use a token retrieved from the Auth service");
        System.out.println("5.3) All services will provide their 'UP' status");

        Response response = new SaleService().ping(new AuthService().getToken());
        assertEquals(HttpStatus.SC_OK, response.getStatusCode(), "Http status");
        JsonPath json = response.jsonPath();
        assertEquals("client", json.getString("service"), "Service");
        assertEquals("UP", json.getString("status"), "Ping Status");
    }

    @Test
    void addSaleToClient() {
        System.out.println("4.1.2) A client can make a purchase");
        System.out.println("4.1.2) A purchase can contain 1 or more products within a market");
        System.out.println("4.1.3) A successful purchase will provide a summary of the purchase cost and relevant amounts");

        SaleRequest request = new SaleRequest()
                .market("MarketX")
                .coupons(Collections.singletonList("X-123"))
                .client(new Client().ClientId("int-1"))
                .products(Collections.singletonList(new Product().getByName("product 1")));

        Response response = new SaleService().addSale(request);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode(), "Http status");
        JsonPath json = response.jsonPath();
        assertEquals("success", json.getString("status"), "status");
        assertEquals("Sale successful for John Doe", json.getString("message"), "message");
        JsonPath details = json.setRootPath("details");
        assertEquals("GBP", details.getString("currency"), "Currency");
        assertEquals(123.45, details.getDouble("totalSale"), "Total Sale");
        assertEquals(23.45, details.getDouble("totalTax"), "Total Tax");
        assertEquals(1.23, details.getDouble("totalDiscount"), "Total Discount");
        assertEquals(12.12, details.getDouble("totalPostage"), "Total Postage");
        List<String> productList = details.getList("products");
        assertTrue(productList.contains("Product 1") && productList.contains("Product 2") && productList.contains("Product 3"), "Product list contains expected products");
        assertEquals("Leave outside by the pool.", details.getString("additionalDetails"));
    }
}