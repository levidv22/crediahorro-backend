package upeu.edu.pe.admin_core_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.service.ClienteService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // GET /clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }

    // GET /clientes/{id}
    @GetMapping(path = "{id}")
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long id) {
        Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(id);
        return clienteOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/buscar")
    public List<String> buscarClientes(@RequestParam String nombre) {
        return clienteService.buscarClientesPorNombre(nombre)
                .stream()
                .map(Cliente::getNombre) // solo devolver el nombre
                .toList();
    }

    // POST /clientes
    @PostMapping
    public ResponseEntity<Cliente> guardarCliente(@RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.guardarCliente(cliente);

        return ResponseEntity.ok(nuevoCliente);
    }

    // PUT /clientes/{id}
    @PutMapping(path = "{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id, @RequestBody Cliente clienteActualizado) {
        Optional<Cliente> clienteOpt = clienteService.actualizarCliente(id, clienteActualizado);
        return clienteOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /clientes/{id}
    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}