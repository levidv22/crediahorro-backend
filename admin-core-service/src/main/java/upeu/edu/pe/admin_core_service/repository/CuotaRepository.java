package upeu.edu.pe.admin_core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.admin_core_service.entities.Cuota;

import java.time.LocalDate;
import java.util.List;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {
    @Query("SELECT c FROM Cuota c " +
            "WHERE c.id IN (" +
            "SELECT cu.id FROM Prestamo p JOIN p.cuotas cu " +
            "WHERE p.id IN (" +
            "SELECT pr.id FROM Cliente cl JOIN cl.prestamos pr " +
            "WHERE cl.nombre = :nombre)) " +
            "AND c.estado = :estado")
    List<Cuota> findCuotasByClienteDniAndEstado(@Param("nombre") String nombre,
                                                @Param("estado") String estado);

    @Query(value = "SELECT p.id " +
            "FROM prestamos p " +
            "JOIN cuotas c ON p.id = c.id_prestamos " +
            "WHERE c.id = :cuotaId", nativeQuery = true)
    Long findPrestamoIdByCuotaId(@Param("cuotaId") Long cuotaId);


    // Agrega este si no lo tienes para buscar por estado y fecha
    List<Cuota> findByEstadoAndFechaPago(String estado, LocalDate fechaPago);

}
