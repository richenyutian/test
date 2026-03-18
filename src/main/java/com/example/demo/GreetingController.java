package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

	@GetMapping("/api/hello")
	public GreetingResponse hello(@RequestParam(defaultValue = "World") String name) {
		return new GreetingResponse("Hello, " + name + "!", "Spring Boot");
	}
}
