import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import stub.StubHandler;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import service.AuthService;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthServiceTest {
    @BeforeAll
    static void setup() {
        new StubHandler().withAuthStub().start();
    }

    @Test
    void validCredentialsWillReturnToken() {
        System.out.println("1.1) Requests with valid user credentials will be given a token");
        Response response = new AuthService().getToken("fakeId", "fakePassword");
        assertEquals(HttpStatus.SC_OK, response.getStatusCode(), "Http status");
        assertEquals("fake_token", response.jsonPath().getString("token"), "Token");
    }

    @Test
    void missingCredentialsWillReturnError() {
        System.out.println("1.2) Requests with invalid user credentials will be given an appropriate error response");
        Response response = new AuthService().getToken("missingHeader");
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCode(), "Http status");
        JsonPath json = response.jsonPath();
        assertEquals("Error", json.getString("status"), "Message type");
        assertEquals("Missing Credentials", json.getString("reason"), "Message reason");
    }

    @Test
    void invalidCredentialsWillReturnError() {
        System.out.println("1.2) Requests with invalid user credentials will be given an appropriate error response");
        Response response = new AuthService().getToken("invalidClientId", "invalidClientPassword");
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusCode(), "Http status");
        JsonPath json = response.jsonPath();
        assertEquals("Error", json.getString("status"), "Message type");
        assertEquals("Invalid Credentials", json.getString("reason"), "Message reason");
    }
}
