import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

public class StatefulBehaviour {

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

    public void stubForStatefulBehaviour() {

        stubFor(get(urlEqualTo("/nl/3825"))
                .inScenario("Stateful mock example")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(404)
                )
        );

        stubFor(post(urlEqualTo("/nl/3825"))
                .inScenario("Stateful mock example")
                .whenScenarioStateIs(Scenario.STARTED)
                .withRequestBody(equalTo("DATA FOR /nl/3825"))
                .willReturn(aResponse()
                        .withStatus(201)
                )
                .willSetStateTo("DATA_CREATED")
        );

        stubFor(get(urlEqualTo("/nl/3825"))
                .inScenario("Stateful mock example")
                .whenScenarioStateIs("DATA_CREATED")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("DATA FOR /nl/3825")
                )
        );
    }

    @Test
    public void callWireMockStatefulStub_shouldCompleteScenarioSuccessfully() {

        stubForStatefulBehaviour();

        given().
                spec(requestSpec).
        when().
                get("/nl/3825").
        then().
                assertThat().
                statusCode(404);

        given().
                spec(requestSpec).
        and().
                body("DATA FOR /nl/3825").
        when().
                post("/nl/3825").
        then().
                assertThat().
                statusCode(201);

        given().
                spec(requestSpec).
        when().
                get("/nl/3825").
        then().
                assertThat().
                statusCode(200).
        and().
                body(org.hamcrest.Matchers.equalTo("DATA FOR /nl/3825"));
    }
}
