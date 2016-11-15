package org.pg6100.rest.swarm;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 * Created by arcuri82 on 11/15/2016.
 */
public class SwarmRestIT {

    private static Process process;


    @BeforeClass
    public static void startJar() throws Exception {

        /*
            Here, we are going to start the RESTful web service directly
            from the packaged fat jar.
            As the jar has to be built first, these tests have to be run
            as integration tests (recall the order of the Maven phases)
         */

        String version = "0.0.1-SNAPSHOT"; //NOTE: those could be system properties
        //note: the plugin adds "-swarm" to the name of the uberjar
        String jar = "wildflySwarmExample-" + version + "-swarm.jar";
        String jarLocation = "target" + File.separator + jar;

        if (!Files.exists(Paths.get(jarLocation))) {
            throw new AssertionError("Jar file was not created at: " + jarLocation);
        }

        String[] command = new String[]{"java", "-jar", jarLocation};

        /*
            This will execute

              java -jar target/wildflySwarmExample-0.0.1-SNAPSHOT-swarm.jar

             on a new process, starting a new JVM
         */
        process = new ProcessBuilder().command(command).start();

        //make sure that the process is stopped, by adding a hook
        //in the shutdown of this JVM running the tests
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopProcess();
            }
        });

        assertTrue(process.isAlive());

        //keep reading the output until we are sure that the server is properly started
        Scanner in = new Scanner(new BufferedInputStream(process.getInputStream()));
        while (in.hasNext()) {
            String line = in.nextLine();
            System.out.println(line);
            if (line.contains("WildFly Swarm is Ready")) {
                break;
            }
        }
    }

    @AfterClass
    public static void stopJar() {
        stopProcess();
    }

    private static void stopProcess() {
        if (process != null && process.isAlive()) {
            process.destroy();
            process = null;
        }
    }


    @Test
    public void testGetMessage(){

        given().get("/swarm/api/example")
                .then()
                .statusCode(200)
                .body(containsString(SwarmRest.MESSAGE));
    }
}