package io.github.domgarlor.jobtracker;

import org.springframework.boot.SpringApplication;

public class TestJobTrackerApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(JobTrackerApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
