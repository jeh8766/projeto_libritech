package model;

import model.enums.TipoUsuario;

public class Funcionario extends Usuario {

    public Funcionario(String nome, String cpf, String email, String senha, TipoUsuario tipo) {
        super(nome, cpf, email, senha, tipo);
    }

    public Funcionario(Integer idUsuario, String nome, String cpf, String email, String senha, TipoUsuario tipo) {
        super(idUsuario, nome, cpf, email, senha, tipo);
    }

    @Override
    public int getDiasPrazoEmprestimo() {
        return 14;
    }
}
