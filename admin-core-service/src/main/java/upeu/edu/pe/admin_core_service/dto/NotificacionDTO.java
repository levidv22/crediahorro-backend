package upeu.edu.pe.admin_core_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NotificacionDTO {
    private String telefono;
    private String nombre;
    private Long prestamoId;            // <--- Nuevo
    private double monto_prestamo;       // <--- Nuevo (opcional, si quieres mostrar el monto del préstamo)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;
    private  double monto_cuota;;// Mejor usar Double, no String

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(Long prestamoId) {
        this.prestamoId = prestamoId;
    }

    public double getMonto_prestamo() {
        return monto_prestamo;
    }

    public void setMonto_prestamo(double monto_prestamo) {
        this.monto_prestamo = monto_prestamo;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public double getMonto_cuota() {
        return monto_cuota;
    }

    public void setMonto_cuota(double monto_cuota) {
        this.monto_cuota = monto_cuota;
    }

    @Override
    public String toString() {
        return "NotificacionDto{" +
                "telefono='" + telefono + '\'' +
                ", nombre='" + nombre + '\'' +
                ", prestamoId=" + prestamoId +
                ", monto_prestamo='" + monto_prestamo + '\'' +
                ", fechaPago=" + fechaPago +
                ", monto_cuota='" + monto_cuota + '\'' +
                '}';
    }
}
