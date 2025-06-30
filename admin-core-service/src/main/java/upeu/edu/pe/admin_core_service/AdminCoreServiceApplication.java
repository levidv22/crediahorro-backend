package upeu.edu.pe.admin_core_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient //habilitar el registro en el registry-server
@EnableFeignClients
@EnableScheduling
public class AdminCoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminCoreServiceApplication.class, args);
	}

}
