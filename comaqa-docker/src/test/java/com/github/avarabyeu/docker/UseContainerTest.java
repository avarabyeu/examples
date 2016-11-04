package com.github.avarabyeu.docker;

import com.github.dockerjava.api.DockerClient;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * @author Andrei Varabyeu
 */
@Test
@Guice(modules = { AuthServerDockerModule.class })
public class UseContainerTest {

    @Inject
    private DockerClient dockerClient;

    @Test
    public void startDefaultServer() throws InterruptedException {
        DockerContainer authServer = new DockerContainer(dockerClient, "comaqa-auth-server", null, null, null, null);
        authServer.startAsync().awaitRunning();
        Integer port = authServer.getExposedPorts().get(8181);
        AwaitUtils.waitForUp("http://localhost:" + port);
        authServer.stopAsync().awaitTerminated();
    }

    @Test
    public void startServerWithRedis() throws InterruptedException {
        DockerContainer redis = new DockerContainer(dockerClient, "redis:alpine", "redis", null, null, null);
        redis.startAsync().awaitRunning();

        DockerContainer authServer = new DockerContainer(dockerClient, "comaqa-auth-server", null, null,
                ImmutableMap.<String, String>builder()
                        .put("spring.redis.port", AuthServerDockerModule.DEFAULT_REDIS_PORT)
                        .put("spring.redis.host", redis.getInternalIP())
                        .build(), null);

        authServer.startAsync().awaitRunning();
        Integer port = authServer.getExposedPorts().get(8181);
        AwaitUtils.waitForUp("http://localhost:" + port);
        authServer.stopAsync().awaitTerminated();
    }
}
