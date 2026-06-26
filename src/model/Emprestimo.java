package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Emprestimo {

    private Integer idEmprestimo;
    private Integer idUsuario;
    private Integer idLivro;
    private String tituloLivro;
    private LocalDateTime dataSaida;
    private LocalDate dataPrevista;
    private LocalDateTime dataDevolucao;

    public Emprestimo(Integer idUsuario, Integer idLivro) {
        this.idUsuario = idUsuario;
        this.idLivro = idLivro;
    }

    public Emprestimo() {
    }

    public Emprestimo(Integer idEmprestimo, Integer idLivro, LocalDateTime dataSaida, LocalDate dataPrevista) {
        this.idEmprestimo = idEmprestimo;
        this.idLivro = idLivro;
        this.dataSaida = dataSaida;
        this.dataPrevista = dataPrevista;
    }

    public Emprestimo(Integer idEmprestimo, Integer idUsuario, String titulo, LocalDateTime dataSaida,
            LocalDate dataPrevista, LocalDateTime dataDevolucao) {
        this.idEmprestimo = idEmprestimo;
        this.idUsuario = idUsuario;
        this.tituloLivro = titulo;
        this.dataSaida = dataSaida;
        this.dataPrevista = dataPrevista;
    }

    public Integer getIdEmprestimo() {
        return idEmprestimo;
    }

    public void setIdEmprestimo(Integer idEmprestimo) {
        this.idEmprestimo = idEmprestimo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdLivro() {
        return idLivro;
    }

    public void setIdLivro(Integer idLivro) {
        this.idLivro = idLivro;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDateTime dataSaida) {
        this.dataSaida = dataSaida;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalDateTime getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDateTime dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public void setTituloLivro(String tituloLivro) {
        this.tituloLivro = tituloLivro;
    }

}
