package com.github.avarabyeu.docker;

import io.restassured.RestAssured;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Created by avarabyeu on 11/4/16.
 */
public class AwaitUtils {

    public static final long DEFAULT_WAIT_TIMEOUT = 5L;
    public static final int DEFAULT_POOL_DELAY = 10;

    /**
     * Waits for spring boot app to bootstrap.
     * Make sure default health endpoint is available and not secured
     *
     * @param springBootUrl URL of Spring Boot application
     */
    public static void waitForUp(String springBootUrl) {
        await()
                .atMost(DEFAULT_WAIT_TIMEOUT, TimeUnit.MINUTES)
                .pollDelay(DEFAULT_POOL_DELAY, TimeUnit.SECONDS)
                .ignoreException(ConnectException.class)
                .until(() -> RestAssured.get(springBootUrl + "/health").then()
                        .extract().jsonPath().get("status").equals("UP"));
    }
}
