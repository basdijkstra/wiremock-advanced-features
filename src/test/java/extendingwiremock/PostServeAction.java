package extendingwiremock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

public class PostServeAction {

    private RequestSpecification requestSpec;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            new WireMockConfiguration().port(9876).extensions(new WriteToDBAction())
    );

    @Before
    public void createRequestSpec() {

        requestSpec = new RequestSpecBuilder().
                setBaseUri("http://localhost").
                setPort(9876).
                build();
    }

    public void stubForPostServeAction() {

        stubFor(get(urlEqualTo("/post-serve-action"))
                .withPostServeAction("write-to-database",
                        Parameters.one("dbName", "this-is-my-db")
                )
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Authorized")
                ));
    }

    @Test
    public void callWireMockWithPostServeAction_checkStatusCodeEquals200() {

        stubForPostServeAction();

        given().
                spec(requestSpec).
        when().
                get("/post-serve-action").
        then().
                assertThat().
                statusCode(200);
    }
}
