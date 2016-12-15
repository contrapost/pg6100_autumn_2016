package org.pg6100.exam.gamerest;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.BufferedInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

/**
 * Created by arcuri82 on 06-Dec-16.
 */
public class GameRestIT extends GameRestTestBase{

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
        String jar = "gameRest-" + version + ".jar";
        String jarLocation = "target" + File.separator + jar;

        if (!Files.exists(Paths.get(jarLocation))) {
            throw new AssertionError("Jar file was not created at: " + jarLocation);
        }

        String[] command = new String[]{
                "java",
                "-D"+GameRest.QUIZ_URL_PROP+"="+ QUIZ_MOCKED_URL,
                "-jar",
                jarLocation,
                "server"};

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
            if (line.contains("Server: Started")) {
                break;
            }
        }

        assertTrue(process.isAlive());
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
}
