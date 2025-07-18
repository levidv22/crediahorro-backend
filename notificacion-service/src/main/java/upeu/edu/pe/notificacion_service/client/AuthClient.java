package upeu.edu.pe.notificacion_service.client;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import upeu.edu.pe.notificacion_service.beans.LoadBalancerConfiguration;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/auth/admin-exists")
    boolean adminExists();

    @GetMapping("/auth/admin-email/{username}")
    String obtenerEmailPorUsername(@PathVariable("username") String username);
}
