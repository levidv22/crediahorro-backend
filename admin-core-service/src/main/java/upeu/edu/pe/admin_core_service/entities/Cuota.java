package upeu.edu.pe.admin_core_service.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "cuotas")
public class Cuota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;
    private double montoCuota;
    private double capital;
    private double interes;
    private String estado; // PENDIENTE o PAGADA
    private String tipoPago; // Pagó Capital, Pagó Interés, Pagó Completo
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPagada;

    public Cuota() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getMontoCuota() {
        return montoCuota;
    }

    public void setMontoCuota(double montoCuota) {
        this.montoCuota = montoCuota;
    }

    public double getCapital() {
        return capital;
    }

    public void setCapital(double capital) {
        this.capital = capital;
    }

    public double getInteres() {
        return interes;
    }

    public void setInteres(double interes) {
        this.interes = interes;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public LocalDate getFechaPagada() {
        return fechaPagada;
    }

    public void setFechaPagada(LocalDate fechaPagada) {
        this.fechaPagada = fechaPagada;
    }

    @Override
    public String toString() {
        return "Cuota{" +
                "id=" + id +
                ", fechaPago=" + fechaPago +
                ", montoCuota=" + montoCuota +
                ", capital=" + capital +
                ", interes=" + interes +
                ", estado='" + estado + '\'' +
                ", tipoPago='" + tipoPago + '\'' +
                ", fechaPagada=" + fechaPagada +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Cuota cuota = (Cuota) o;
        return Double.compare(montoCuota, cuota.montoCuota) == 0 && Double.compare(capital, cuota.capital) == 0 && Double.compare(interes, cuota.interes) == 0 && Objects.equals(id, cuota.id) && Objects.equals(fechaPago, cuota.fechaPago) && Objects.equals(estado, cuota.estado) && Objects.equals(tipoPago, cuota.tipoPago) && Objects.equals(fechaPagada, cuota.fechaPagada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fechaPago, montoCuota, capital, interes, estado, tipoPago, fechaPagada);
    }
}