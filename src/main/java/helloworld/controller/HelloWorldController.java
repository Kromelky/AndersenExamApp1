package main.java.helloworld.controller;

import main.java.helloworld.model.HelloWorld;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloWorldController {
	
	@RequestMapping("/")
    public HelloWorld getGreetings()
    {
        HelloWorld helloWorld = new HelloWorld(1, "HelloWorld from java");
		return helloWorld;
    }

}
