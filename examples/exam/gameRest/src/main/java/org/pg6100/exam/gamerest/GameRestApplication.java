package org.pg6100.exam.gamerest;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
public class GameRestApplication extends Application<GameRestConfiguration> {

    public static void main(String[] args) throws Exception {
        new GameRestApplication().run(args);
    }

    @Override
    public String getName() {
        return "Game client written in DropWizard";
    }

    @Override
    public void initialize(Bootstrap<GameRestConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets", "/", null, "a"));
        bootstrap.addBundle(new AssetsBundle("/assets/css", "/css", null, "b"));
        bootstrap.addBundle(new AssetsBundle("/assets/fonts", "/fonts", null, "c"));
        bootstrap.addBundle(new AssetsBundle("/assets/images", "/images", null, "d"));
        bootstrap.addBundle(new AssetsBundle("/assets/lang", "/lang", null, "e"));
        bootstrap.addBundle(new AssetsBundle("/assets/lib", "/lib", null, "f"));
    }


    @Override
    public void run(GameRestConfiguration configuration, Environment environment) {

        environment.jersey().setUrlPattern("/game/api/*");
        environment.jersey().register(new GameRest());


        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/game/api");
        beanConfig.setResourcePackage("org.pg6100.exam.gamerest,org.pg6100.exam.gamecommands");
        beanConfig.setScan(true);

        //add further configuration to activate SWAGGER
        environment.jersey().register(new io.swagger.jaxrs.listing.ApiListingResource());
        environment.jersey().register(new io.swagger.jaxrs.listing.SwaggerSerializers());
    }
}
