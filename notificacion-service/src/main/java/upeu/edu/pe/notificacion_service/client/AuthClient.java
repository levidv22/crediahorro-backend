package upeu.edu.pe.notificacion_service.client;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import upeu.edu.pe.notificacion_service.beans.LoadBalancerConfiguration;

@FeignClient(name = "auth-service", url = "https://auth-service-production-b27b.up.railway.app")
@LoadBalancerClient(name = "auth-service", configuration = LoadBalancerConfiguration.class)
public interface AuthClient {
    @GetMapping(path = "auth-service/auth/admin-exists")
    boolean adminExists();

    @GetMapping(path = "auth-service/auth/admin-email/{username}")
    String obtenerEmailPorUsername(@PathVariable("username") String username);
}
