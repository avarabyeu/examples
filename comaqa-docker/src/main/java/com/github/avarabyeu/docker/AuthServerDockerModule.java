package com.github.avarabyeu.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

/**
 * Describes Docker client and everything that needed for the tests
 *
 * @author Andrei Varabyeu
 */
public class AuthServerDockerModule extends AbstractModule {

    public static final String DEFAULT_REDIS_PORT = "6379";

    @Override
    protected void configure() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
        final DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        binder().bind(DockerClient.class).toInstance(dockerClient);

        binder().bind(DockerContainer.class).toProvider(() -> {
            //ImmutableMap.<Integer, Integer>builder().put(8181, 8181).build()
            return new DockerContainer(dockerClient, "comaqa-auth-server", null, null, null);
        }).in(Scopes.SINGLETON);

        binder().bind(Key.get(RequestSpecification.class, Names.named("TokenRequest"))).toProvider(
                () -> RestAssured.given()
                        .auth().basic("oauth", "oauth")
                        .with().params(ImmutableMap.<String, String>builder()
                                .put("grant_type", "password")
                                .put("username", "user")
                                .put("password", "password")
                                .build()));
    }
}
