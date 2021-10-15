package helloworld.controller;

import java.util.ArrayList;
import java.util.List;

import helloworld.model.HelloWorld;
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
