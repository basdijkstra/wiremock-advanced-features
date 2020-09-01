package extendingwiremock;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.restassured.RestAssured.given;

public class ContentTypeMatching {

    private RequestSpecification requestSpec;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            new WireMockConfiguration().port(9876).extensions(new ContentTypeMatcher())
    );

    @Before
    public void createRequestSpec() {

        requestSpec = new RequestSpecBuilder().
                setBaseUri("http://localhost").
                setPort(9876).
                build();
    }

    public void stubForContentTypeMatching() {

        stubFor(requestMatching(
                "content-type-matcher",
                Parameters.one("Content-Type", "application/xml")
        )
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Content-Type is XML")
                ));
    }

    @Test
    public void callWireMockWithXmlRequest_checkStatusCodeEquals200() {

        stubForContentTypeMatching();

        given().
                spec(requestSpec).
        and().
                contentType("application/xml").
        when().
                get("/content-type-matching").
        then().
                assertThat().
                statusCode(200).
        and().
                body(org.hamcrest.Matchers.equalTo("Content-Type is XML"));
    }

    @Test
    public void callWireMockWithJsonRequest_checkStatusCodeEquals404() {

        /**
         * This test throws a VerificationException because there is no matching stub for it
         */

        stubForContentTypeMatching();

        given().
                spec(requestSpec).
        and().
                contentType("application/json").
        when().
                get("/content-type-matching");
    }
}
