package upeu.edu.pe.notificacion_service.client;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import upeu.edu.pe.notificacion_service.beans.LoadBalancerConfiguration;

@FeignClient(name = "auth-service")
@LoadBalancerClient(name = "auth-service", configuration = LoadBalancerConfiguration.class)
public interface AuthClient {
    @GetMapping(path = "auth-service/auth/admin-exists")
    boolean adminExists();

    @GetMapping(path = "auth-service/auth/admin-email")
    String obtenerAdminEmail();
}
