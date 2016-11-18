package org.pg6100.dropwizard.hystrix;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommandProperties;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.apache.commons.configuration.AbstractConfiguration;

/**
 * Created by arcuri82 on 18-Nov-16.
 */
public class HystrixExampleApplication extends Application<HystrixExampleConfiguration> {

    public static void main(String[] args) throws Exception {
        new HystrixExampleApplication().run(args);
    }

    @Override
    public String getName() {
        return "Counter written in DropWizard";
    }


    @Override
    public void run(HystrixExampleConfiguration configuration, Environment environment) {

        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(new XRest());
        environment.jersey().register(new YRest());

        //Hystrix configuration
        AbstractConfiguration conf = ConfigurationManager.getConfigInstance();
        // how long to wait before giving up a request?
        conf.setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", 500); //default is 1000
        // how many failures before activating the CB?
        conf.setProperty("hystrix.command.default.circuitBreaker.requestVolumeThreshold", 2); //default 20
        conf.setProperty("hystrix.command.default.circuitBreaker.errorThresholdPercentage", 50);
        //for how long should the CB stop requests? after this, 1 single request will try to check if remote server is ok
        conf.setProperty("hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds", 5000);

    }
}
