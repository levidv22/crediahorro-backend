package upeu.edu.pe.admin_core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.admin_core_service.entities.Cliente;

import java.time.LocalDate;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT(:nombre, '%')) ORDER BY c.nombre ASC")
    List<Cliente> findByNombreStartingWith(@Param("nombre") String nombre);

}
