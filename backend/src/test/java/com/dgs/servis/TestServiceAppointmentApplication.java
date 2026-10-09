package com.dgs.servis;

import org.springframework.boot.SpringApplication;

public class TestServiceAppointmentApplication {

	public static void main(String[] args) {
		SpringApplication.from(ServiceAppointmentApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
