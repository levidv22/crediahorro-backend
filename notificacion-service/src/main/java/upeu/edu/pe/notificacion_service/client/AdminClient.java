package upeu.edu.pe.notificacion_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import upeu.edu.pe.notificacion_service.model.Cliente;

@FeignClient(name = "admin-service", url = "https://admin-service-production-1c43.up.railway.app")
public interface AdminClient {
    @GetMapping(path = "admin-service/clientes")
    List<Cliente> obtenerClientes();
}

