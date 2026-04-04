package com.mikehenry.dialogue_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.mikehenry.dialogue_engine.config")
public class DialogueEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(DialogueEngineApplication.class, args);
	}
}
