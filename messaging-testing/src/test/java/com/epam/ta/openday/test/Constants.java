package com.epam.ta.openday.test;

import java.time.Duration;

/**
 * Constants for tests
 *
 * @author Andrei Varabyeu
 */
class Constants {
    private Constants() {
        //statics only
    }

    static final String EXPECTED_MESSAGE = "hello world-1";
    static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

}
