package model;

import model.enums.TipoUsuario;

public class Aluno extends Usuario {

    public Aluno(String nome, String cpf, String email, String senha) {
        super(nome, cpf, email, senha, TipoUsuario.ALUNO);
    }

    public Aluno(Integer idUsuario, String nome, String cpf, String email, String senha, TipoUsuario tipo) {
        super(idUsuario, nome, cpf, email, senha, tipo);
    }

    @Override
    public int getDiasPrazoEmprestimo() {
        return 7;
    }
}
