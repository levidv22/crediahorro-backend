package upeu.edu.pe.registry_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer //config para que levante eureka server
public class RegistryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegistryServerApplication.class, args);
	}

}
