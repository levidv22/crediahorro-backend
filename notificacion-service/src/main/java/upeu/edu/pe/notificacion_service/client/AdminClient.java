package upeu.edu.pe.notificacion_service.client;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import upeu.edu.pe.notificacion_service.beans.LoadBalancerConfiguration;
import upeu.edu.pe.notificacion_service.model.Cliente;

import java.util.List;

@FeignClient(name = "admin-service")
@LoadBalancerClient(name = "admin-service", configuration = LoadBalancerConfiguration.class)
public interface AdminClient {
    @GetMapping(path = "admin-service/clientes")
    List<Cliente> obtenerClientes();
}

