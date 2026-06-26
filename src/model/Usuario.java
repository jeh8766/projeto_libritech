package model;

import model.enums.TipoUsuario;

public class Usuario {

    private Integer idUsuario;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private TipoUsuario tipo;

    public Usuario(String nome, String cpf, String email, String senha, TipoUsuario tipo) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
    }

    public Usuario(Integer idUsuario, String nome, String cpf, String email, String senha, TipoUsuario tipo) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome inválido");
        }
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        if (cpf.length() != 11 || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF inválido");
        }
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email inválido");
        }
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public int getDiasPrazoEmprestimo() {
        switch (this.tipo) {
            case ALUNO:
                return 15;
            case ESTAGIARIO:
                return 30;
            case BIBLIOTECARIO:
                return 60;
            default:
                return 7;
        }
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

}