package com.github.avarabyeu.docker;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author Andrei Varabyeu
 */
@Test
@Guice(modules = { AuthServerDockerModule.class })
public class LoginTest {

    @Inject
    private DockerContainer deployment;

    @Inject
    @Named("TokenRequest")
    private Provider<RequestSpecification> tokenRequest;

    private Integer port;

    @BeforeClass
    public void setupApp() {
        deployment.startAsync().awaitRunning();
        port = deployment.getExposedPorts().get(8181);
        AwaitUtils.waitForUp("http://localhost:" + port);
    }

    @AfterClass
    public void killApp() {
        deployment.stopAsync().awaitTerminated();
    }

    @Test
    public void loginTest() throws InterruptedException {
        final ValidatableResponse tokenResponse =
                tokenRequest.get()
                        .post("http://localhost:" + port + "/oauth/token")
                        .then();

        String token = tokenResponse
                .extract().jsonPath().get("access_token");

        given()
                .auth().oauth2(token)
                .get("http://localhost:" + port + "/api/me")
                .then()
                .body("authenticated", is(true))
                .body("principal.username", is("user"));

    }
}
