package upeu.edu.pe.report_grafico.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Cuota {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;
    private double montoCuota;
    private String estado; // PENDIENTE o PAGADA

    public Cuota() {
    }

    public Cuota(Long id, LocalDate fechaPago, double montoCuota, String estado) {
        this.id = id;
        this.fechaPago = fechaPago;
        this.montoCuota = montoCuota;
        this.estado = estado;
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

    public static class Builder {
        private Long id;
        private LocalDate fechaPago;
        private double montoCuota;
        private String estado;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder fechaPago(LocalDate fechaPago) {
            this.fechaPago = fechaPago;
            return this;
        }

        public Builder montoCuota(double montoCuota) {
            this.montoCuota = montoCuota;
            return this;
        }

        public Builder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public Cuota build() {
            return new Cuota(id, fechaPago, montoCuota, estado);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}