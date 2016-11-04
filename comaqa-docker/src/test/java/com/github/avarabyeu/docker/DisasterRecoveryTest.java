package com.github.avarabyeu.docker;

import com.github.dockerjava.api.DockerClient;
import com.google.common.collect.ImmutableMap;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

/**
 * @author Andrei Varabyeu
 */
@Test
@Guice(modules = { AuthServerDockerModule.class })
public class DisasterRecoveryTest {

    @Inject
    private DockerClient dockerClient;

    @Inject
    @Named("TokenRequest")
    private Provider<RequestSpecification> tokenRequest;

    private DockerContainer authServer;
    private DockerContainer redisServer;
    private Integer appPort;

    @BeforeClass
    public void setupApp() {
        redisServer = new DockerContainer(dockerClient, "redis:alpine", "redis", null, null, null);
        redisServer.startAsync().awaitRunning();

        authServer = new DockerContainer(dockerClient, "comaqa-auth-server", null, null,
                ImmutableMap.<String, String>builder()
                        .put("spring.redis.appPort", AuthServerDockerModule.DEFAULT_REDIS_PORT)
                        .put("spring.redis.host", redisServer.getInternalIP())
                        .build(), null);

        authServer.startAsync().awaitRunning();
        appPort = authServer.getExposedPorts().get(8181);
        AwaitUtils.waitForUp("http://localhost:" + appPort);
    }

    @AfterClass
    public void killApp() {
        authServer.stopAsync().awaitTerminated();
    }

    @Test
    public void loginTest() throws InterruptedException {
        redisServer.stopAsync().awaitTerminated();

        tokenRequest.get()
                .post("http://localhost:" + appPort + "/oauth/token")
                .then().log().all().statusCode(503)
                .body("error_description", containsString("Horrible exception"));

        redisServer = new DockerContainer(dockerClient, "redis:alpine", "redis", null, null, null);
        redisServer.startAsync().awaitRunning();


        /* redis is superfact but it would be better to wait here */
        tokenRequest.get()
                .post("http://localhost:" + appPort + "/oauth/token")
                .then().log().all().statusCode(200).body("access_token", not(empty()));
    }
}
