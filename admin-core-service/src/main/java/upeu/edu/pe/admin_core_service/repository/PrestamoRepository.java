package upeu.edu.pe.admin_core_service.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.admin_core_service.entities.Prestamo;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @Query("SELECT p FROM Prestamo p JOIN Cliente c ON p MEMBER OF c.prestamos " +
            "WHERE c.nombre = :nombre AND p.estado = :estado")
    List<Prestamo> findPrestamosByClienteDniAndEstado(@Param("nombre") String nombre,
                                                      @Param("estado") String estado);

    @EntityGraph(attributePaths = "cuotas")
    Optional<Prestamo> findById(Long id);

    @Query("SELECT p FROM Prestamo p")
    List<Prestamo> findAllPrestamos();
    // Si no tienes, crea uno así:
    @Query(value = "SELECT id_clientes FROM prestamos WHERE id = :prestamoId", nativeQuery = true)
    Long findClienteIdByPrestamoId(@Param("prestamoId") Long prestamoId);

}

