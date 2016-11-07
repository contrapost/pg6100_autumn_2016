package org.pg6100.dropwizard.newsdw;

import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.*;
import org.pg6100.dropwizard.newsdw.api.Formats;
import org.pg6100.dropwizard.newsdw.dto.NewsDto;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;

/*
    Note: as we do not need to package the jar to run the
    embedded Jetty, we can run this as a "unit" test in
    the "mvn test" phase, instead of the integration testing
    phase (which would had happened if class name ended with *IT
    instead of *Test).

    Note: although we run them as Maven unit tests, they are
    still integration/system tests.
    However, they do not tests if there is any wrong configuration
    in the generated jar file.
 */
public class NewsApplicationTest extends NewsApplicationTestBase{

    @ClassRule
    public static final DropwizardAppRule<NewsConfiguration> RULE =
            new DropwizardAppRule<>(NewsApplication.class);

}