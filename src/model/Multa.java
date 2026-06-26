package model;

import java.math.BigDecimal;

public class Multa {
    private Integer idMulta;
    private Integer idEmprestimo;
    private BigDecimal valor;
    private boolean pago;

    public Multa(Integer idEmprestimo, BigDecimal valor, boolean pago) {
        this.idEmprestimo = idEmprestimo;
        this.valor = valor;
        this.pago = pago;
    }

    public Multa(Integer idMulta, Integer idEmprestimo, BigDecimal valor, boolean pago) {
        this.idMulta = idMulta;
        this.idEmprestimo = idEmprestimo;
        this.valor = valor;
        this.pago = pago;
    }

    public Integer getIdMulta() {
        return idMulta;
    }

    public void setIdMulta(Integer idMulta) {
        this.idMulta = idMulta;
    }

    public Integer getIdEmprestimo() {
        return idEmprestimo;
    }

    public void setIdEmprestimo(Integer idEmprestimo) {
        this.idEmprestimo = idEmprestimo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor da multa não pode ser negativo.");
        }
        this.valor = valor;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

}
