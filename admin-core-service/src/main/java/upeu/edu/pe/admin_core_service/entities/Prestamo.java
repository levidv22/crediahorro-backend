package upeu.edu.pe.admin_core_service.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "prestamos")
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double monto;
    private double tasaInteresMensual;
    private int numeroCuotas;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaCreacion;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;
    private String estado; // ACTIVO o PAGADO

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "id_prestamos", referencedColumnName = "id")
    //@JsonIgnore
    private List<Cuota> cuotas;

    public Prestamo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public double getTasaInteresMensual() {
        return tasaInteresMensual;
    }

    public void setTasaInteresMensual(double tasaInteresMensual) {
        this.tasaInteresMensual = tasaInteresMensual;
    }

    public int getNumeroCuotas() {
        return numeroCuotas;
    }

    public void setNumeroCuotas(int numeroCuotas) {
        this.numeroCuotas = numeroCuotas;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<Cuota> getCuotas() {
        return cuotas;
    }

    public void setCuotas(List<Cuota> cuotas) {
        this.cuotas = cuotas;
    }


    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", monto=" + monto +
                ", tasaInteresMensual=" + tasaInteresMensual +
                ", numeroCuotas=" + numeroCuotas +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaInicio=" + fechaInicio +
                ", estado='" + estado + '\'' +
                ", cuotas=" + cuotas +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Prestamo prestamo = (Prestamo) o;
        return Double.compare(monto, prestamo.monto) == 0 && Double.compare(tasaInteresMensual, prestamo.tasaInteresMensual) == 0 && numeroCuotas == prestamo.numeroCuotas && Objects.equals(id, prestamo.id) && Objects.equals(fechaCreacion, prestamo.fechaCreacion) && Objects.equals(fechaInicio, prestamo.fechaInicio) && Objects.equals(estado, prestamo.estado) && Objects.equals(cuotas, prestamo.cuotas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, monto, tasaInteresMensual, numeroCuotas, fechaCreacion, fechaInicio, estado, cuotas);
    }
}