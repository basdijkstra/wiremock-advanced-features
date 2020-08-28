import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.apache.http.client.ClientProtocolException;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

public class FaultSimulation {

    private RequestSpecification requestSpec;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9876);

    @Before
    public void createRequestSpec() {

        requestSpec = new RequestSpecBuilder().
                setBaseUri("http://localhost").
                setPort(9876).
                build();
    }

    /**
     * SIMULATE STATUS CODES INDICATING A FAILURE
     */

    public void stubForReturn503ServiceUnavailable() {

        stubFor(get(urlEqualTo("/serviceunavailable"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withStatusMessage("Service unavailable")
                ));
    }

    @Test
    public void callWireMockServiceUnavailable_shouldReturnHttp503() {

        stubForReturn503ServiceUnavailable();

        given().
                spec(requestSpec).
        when().
                get("/serviceunavailable").
        then().
                assertThat().
                statusCode(503).
        and().
                statusLine(org.hamcrest.Matchers.containsString("Service unavailable"));
    }

    /**
     * SIMULATING A DELAY
     */

    public void stubForDelayOf3000Milliseconds() {

        stubFor(get(urlEqualTo("/slow"))
                .withHeader("speed", equalTo("slow"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000)
                ));
    }

    @Test
    public void callWireMockDelayStub_shouldReturnDelayedResponse() {

        stubForDelayOf3000Milliseconds();

        given().
                spec(requestSpec).
        and().
                header("speed","slow").
        when().
                get("/slow").
        then().
                assertThat().
                statusCode(200).
        and().
                time(org.hamcrest.Matchers.greaterThan(3000L));
    }

    /**
     * SIMULATE ERRONEOUS RESPONSES
     */

    public void stubForFaultResponse() {

        stubFor(get(urlEqualTo("/fault"))
                .willReturn(aResponse()
                        .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
                ));
    }

    @Test(expected = ClientProtocolException.class)
    public void callWireMockFaultStub_shouldThrowClientProtocolException() {

        stubForFaultResponse();

        given().
                spec(requestSpec).
        when().
                get("/fault");
    }
}
