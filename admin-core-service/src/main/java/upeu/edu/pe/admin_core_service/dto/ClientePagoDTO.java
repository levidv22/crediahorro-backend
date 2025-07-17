package upeu.edu.pe.admin_core_service.dto;

import java.time.LocalDate;

public class ClientePagoDTO {
    private String usernameAdministrador;
    private String nombreCliente;
    private LocalDate fechaUltimaCuotaPagada;
    private Double totalPagado;

    public ClientePagoDTO(String usernameAdministrador, String nombreCliente, LocalDate fechaUltimaCuotaPagada, Double totalPagado) {
        this.usernameAdministrador = usernameAdministrador;
        this.nombreCliente = nombreCliente;
        this.fechaUltimaCuotaPagada = fechaUltimaCuotaPagada;
        this.totalPagado = totalPagado;
    }

    public String getUsernameAdministrador() {
        return usernameAdministrador;
    }

    public void setUsernameAdministrador(String usernameAdministrador) {
        this.usernameAdministrador = usernameAdministrador;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDate getFechaUltimaCuotaPagada() {
        return fechaUltimaCuotaPagada;
    }

    public void setFechaUltimaCuotaPagada(LocalDate fechaUltimaCuotaPagada) {
        this.fechaUltimaCuotaPagada = fechaUltimaCuotaPagada;
    }

    public Double getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(Double totalPagado) {
        this.totalPagado = totalPagado;
    }
}
