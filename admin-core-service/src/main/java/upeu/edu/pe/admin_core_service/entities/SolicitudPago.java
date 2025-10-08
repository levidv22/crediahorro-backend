package upeu.edu.pe.admin_core_service.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "solicitudes_pago")
public class SolicitudPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cuotaId;
    private Long clienteId;

    private String tipoSolicitud; // PAGO_COMPLETO, PAGO_PARCIAL
    private String estado; // PENDIENTE, ACEPTADO, RECHAZADO
    private Double montoParcial;

    private String mensajeCliente; // mensaje o comentario del cliente
    private String mensajeAdministrador; // motivo del rechazo o comentario del admin

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] comprobante;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaSolicitud;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRespuesta;

    public SolicitudPago() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCuotaId() {
        return cuotaId;
    }

    public void setCuotaId(Long cuotaId) {
        this.cuotaId = cuotaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Double getMontoParcial() {
        return montoParcial;
    }

    public void setMontoParcial(Double montoParcial) {
        this.montoParcial = montoParcial;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensajeCliente() {
        return mensajeCliente;
    }

    public void setMensajeCliente(String mensajeCliente) {
        this.mensajeCliente = mensajeCliente;
    }

    public byte[] getComprobante() {
        return comprobante;
    }

    public void setComprobante(byte[] comprobante) {
        this.comprobante = comprobante;
    }

    public String getMensajeAdministrador() {
        return mensajeAdministrador;
    }

    public void setMensajeAdministrador(String mensajeAdministrador) {
        this.mensajeAdministrador = mensajeAdministrador;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    @Override
    public String toString() {
        return "SolicitudPago{" +
                "id=" + id +
                ", cuotaId=" + cuotaId +
                ", clienteId=" + clienteId +
                ", tipoSolicitud='" + tipoSolicitud + '\'' +
                ", estado='" + estado + '\'' +
                ", montoParcial=" + montoParcial +
                ", mensajeCliente='" + mensajeCliente + '\'' +
                ", mensajeAdministrador='" + mensajeAdministrador + '\'' +
                ", comprobante=" + Arrays.toString(comprobante) +
                ", fechaSolicitud=" + fechaSolicitud +
                ", fechaRespuesta=" + fechaRespuesta +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SolicitudPago that = (SolicitudPago) o;
        return Objects.equals(id, that.id) && Objects.equals(cuotaId, that.cuotaId) && Objects.equals(clienteId, that.clienteId) && Objects.equals(tipoSolicitud, that.tipoSolicitud) && Objects.equals(estado, that.estado) && Objects.equals(montoParcial, that.montoParcial) && Objects.equals(mensajeCliente, that.mensajeCliente) && Objects.equals(mensajeAdministrador, that.mensajeAdministrador) && Objects.deepEquals(comprobante, that.comprobante) && Objects.equals(fechaSolicitud, that.fechaSolicitud) && Objects.equals(fechaRespuesta, that.fechaRespuesta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cuotaId, clienteId, tipoSolicitud, estado, montoParcial, mensajeCliente, mensajeAdministrador, Arrays.hashCode(comprobante), fechaSolicitud, fechaRespuesta);
    }
}

