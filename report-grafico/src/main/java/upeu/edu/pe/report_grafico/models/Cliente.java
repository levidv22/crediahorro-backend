package upeu.edu.pe.report_grafico.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class Cliente {
    private Long id;
    private String nombre;
    private String dni;
    private String direccion;
    private String telefonoWhatsapp;
    private String correoElectronico;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaCreacion;
    private List<Prestamo> prestamos;

    public Cliente() {
    }

    public Cliente(Long id, String nombre, String dni, String direccion, String telefonoWhatsapp, String correoElectronico, LocalDate fechaCreacion, List<Prestamo> prestamos) {
        this.id = id;
        this.nombre = nombre;
        this.dni = dni;
        this.direccion = direccion;
        this.telefonoWhatsapp = telefonoWhatsapp;
        this.correoElectronico = correoElectronico;
        this.fechaCreacion = fechaCreacion;
        this.prestamos = prestamos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefonoWhatsapp() {
        return telefonoWhatsapp;
    }

    public void setTelefonoWhatsapp(String telefonoWhatsapp) {
        this.telefonoWhatsapp = telefonoWhatsapp;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public static class Builder {
        private Long id;
        private String nombre;
        private String dni;
        private String direccion;
        private String telefonoWhatsapp;
        private String correoElectronico;
        private LocalDate fechaCreacion;
        private List<Prestamo> prestamos;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder dni(String dni) {
            this.dni = dni;
            return this;
        }

        public Builder direccion(String direccion) {
            this.direccion = direccion;
            return this;
        }

        public Builder telefonoWhatsapp(String telefonoWhatsapp) {
            this.telefonoWhatsapp = telefonoWhatsapp;
            return this;
        }

        public Builder correoElectronico(String correoElectronico) {
            this.correoElectronico = correoElectronico;
            return this;
        }

        public Builder fechaCreacion(LocalDate fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public Builder prestamos(List<Prestamo> prestamos) {
            this.prestamos = prestamos;
            return this;
        }

        public Cliente build() {
            return new Cliente(id, nombre, dni, direccion, telefonoWhatsapp, correoElectronico, fechaCreacion, prestamos);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}