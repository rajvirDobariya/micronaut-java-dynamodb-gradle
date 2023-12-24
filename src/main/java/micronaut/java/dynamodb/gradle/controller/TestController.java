package micronaut.java.dynamodb.gradle.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/test")
public class TestController {

    @Get
    public String welcomeFile(){
        return "Welcome to micronaut project!";
    }
}
