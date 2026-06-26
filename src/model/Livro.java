package model;

import java.math.BigDecimal;
import model.enums.StatusLivro;

public class Livro {

    private Integer idLivro;
    private String titulo;
    private String autor;
    private String isbn;
    private BigDecimal precoCusto;
    private int quantidadeEstoque;
    private StatusLivro status;

    public Livro(Integer idLivro, String titulo, String autor, String isbn, BigDecimal precoCusto,
            int quantidadeEstoque, StatusLivro status) {
        this.idLivro = idLivro;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.precoCusto = precoCusto;
        this.quantidadeEstoque = quantidadeEstoque;
        this.status = status;
    }

    public Livro(String titulo, String autor, String isbn, BigDecimal precoCusto, int quantidadeEstoque,
            StatusLivro status) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.precoCusto = precoCusto;
        this.quantidadeEstoque = quantidadeEstoque;
        this.status = status;
    }

    

    public Livro() {
    }

    public Integer getIdLivro() {
        return idLivro;
    }

    public void setIdLivro(Integer idLivro) {
        this.idLivro = idLivro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }

    public void setPrecoCusto(BigDecimal precoCusto) {
        if (precoCusto==null || precoCusto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço de custo não pode ser negativo.");
        }
        this.precoCusto = precoCusto;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public StatusLivro getStatus() {
        return status;
    }

    public void setStatus(StatusLivro status) {
        this.status = status;
    }

}