package upeu.edu.pe.report_grafico.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Cuota {
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

    public Cuota(Long id, LocalDate fechaPago, double montoCuota, double capital, double interes, String estado, String tipoPago, LocalDate fechaPagada) {
        this.id = id;
        this.fechaPago = fechaPago;
        this.montoCuota = montoCuota;
        this.capital = capital;
        this.interes = interes;
        this.estado = estado;
        this.tipoPago = tipoPago;
        this.fechaPagada = fechaPagada;
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

    public static class Builder {
        private Long id;
        private LocalDate fechaPago;
        private double montoCuota;
        private double capital;
        private double interes;
        private String estado;
        private String tipoPago;
        private LocalDate fechaPagada;

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

        public Builder capital(double capital) {
            this.capital = capital;
            return this;
        }

        public Builder interes(double interes) {
            this.interes = interes;
            return this;
        }

        public Builder tipoPago(String tipoPago) {
            this.tipoPago = tipoPago;
            return this;
        }

        public Builder fechaPagada(LocalDate fechaPagada) {
            this.fechaPagada = fechaPagada;
            return this;
        }

        public Cuota build() {
            return new Cuota(id, fechaPago, montoCuota, capital, interes, tipoPago, estado, fechaPagada);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}