import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;

public class ResponseTemplating {

    private RequestSpecification requestSpec;

    @Rule
    public WireMockRule wireMockRule =
            new WireMockRule(wireMockConfig().
                    port(9876).
                    extensions(new ResponseTemplateTransformer(false))
            );

    @Before
    public void createRequestSpec() {

        requestSpec = new RequestSpecBuilder().
                setBaseUri("http://localhost").
                setPort(9876).
                build();
    }

    /**
     * RESPONSE TEMPLATE FOR PORT NUMBER
     */

    public void stubForReturningPortNumber() {

        stubFor(get(urlEqualTo("/echo-port"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Listening on port {{request.requestLine.port}}")
                        .withTransformers("response-template")
                ));
    }

    @Test
    public void callWireMockEchoPortNumberStub_shouldReturn9876() {

        stubForReturningPortNumber();

        given().
                spec(requestSpec).
                when().
                get("/echo-port").
                then().
                assertThat().
                statusCode(200).
                and().
                body(org.hamcrest.Matchers.equalTo("Listening on port 9876"));
    }

    /**
     * RESPONSE TEMPLATE FOR PATH SEGMENTS
     */

    public void stubForReturningCountryAndZipCode() {

        stubFor(get(urlMatching("/[a-z]{2}/[0-9]{5}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("You requested data for country code {{request.requestLine.pathSegments.[0]}} and zip code {{request.requestLine.pathSegments.[1]}}")
                        .withTransformers("response-template")
                ));
    }

    @Test
    public void callWireMockEchoCountryAndZipCodeStub_shouldReturnUs90210() {

        stubForReturningCountryAndZipCode();

        given().
                spec(requestSpec).
                when().
                get("/us/90210").
                then().
                assertThat().
                statusCode(200).
                and().
                body(org.hamcrest.Matchers.equalTo("You requested data for country code us and zip code 90210"));
    }

    /**
     * RESPONSE TEMPLATE FOR JSON REQUEST BODY
     */

    public void stubForReturningCarModel() {

        stubFor(post(urlEqualTo("/echo-car-model"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{{jsonPath request.body '$.car.model'}}")
                        .withTransformers("response-template")
                ));
    }

    @Test
    public void callWireMockStubEchoCarModel_shouldReturnGiulia29V6QuadrifoglioVerde() {

        stubForReturningCarModel();

        given().
                spec(requestSpec).
                contentType(ContentType.JSON).
        and().
                body("{\"car\": {\"make\": \"Alfa Romeo\", \"model\": \"Giulia 2.9 V6 Quadrifoglio Verde\",\"top_speed\": 307}}").
        when().
                post("/echo-car-model").
        then().
                assertThat().
                statusCode(200).
        and().
                body(org.hamcrest.Matchers.equalTo("Giulia 2.9 V6 Quadrifoglio Verde"));
    }
}
