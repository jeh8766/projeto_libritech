drop database if exists db_libritech;
CREATE DATABASE db_libritech;
USE db_libritech;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf CHAR(11) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    tipo ENUM('ALUNO','GERENTE','BIBLIOTECARIO','ESTAGIARIO') NOT NULL
);

CREATE TABLE enderecos (
    id_endereco INT AUTO_INCREMENT PRIMARY KEY,
    logradouro VARCHAR(150),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf CHAR(2),
    id_usuario_fk INT NOT NULL unique,
    CONSTRAINT fk_endereco_usuario FOREIGN KEY(id_usuario_fk) REFERENCES usuarios(id_usuario)
);

CREATE TABLE livros (
    id_livro INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    preco_custo DECIMAL(10,2) not null,
    quantidade_estoque INT NOT NULL default 0,
	status ENUM('DISPONIVEL','INDISPONIVEL', 'RESERVADO') DEFAULT 'DISPONIVEL');

CREATE TABLE emprestimos (
    id_emprestimo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_fk INT NOT NULL,
    id_livro_fk INT NOT NULL,
    data_saida DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_prevista DATE NOT NULL,
    data_devolucao DATETIME NULL,
    FOREIGN KEY(id_usuario_fk) REFERENCES usuarios(id_usuario),
    FOREIGN KEY(id_livro_fk) REFERENCES livros(id_livro)
);

CREATE TABLE multas (
    id_multa INT AUTO_INCREMENT PRIMARY KEY,
    id_emprestimo_fk INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    pago BOOLEAN DEFAULT FALSE not null,
    FOREIGN KEY(id_emprestimo_fk) REFERENCES emprestimos(id_emprestimo)
);

CREATE TABLE reservas (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_fk INT NOT NULL,
    id_livro_fk INT NOT NULL,
    data_reserva DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ATIVA','ATENDIDA','CANCELADA') DEFAULT 'ATIVA',
    FOREIGN KEY (id_usuario_fk) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_livro_fk) REFERENCES livros(id_livro)
);

CREATE TABLE log_auditoria (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    tabela_afetada VARCHAR(50),
    acao VARCHAR(50),
    usuario_responsavel VARCHAR(100),
    dados_antigos LONGTEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CRIAÇÃO DE VIEWS -------------------------------------------------------

CREATE VIEW vw_acervo_publico AS
SELECT titulo, autor, isbn, status
FROM livros;


CREATE VIEW vw_livros_atrasados AS
SELECT e.id_emprestimo, u.nome, u.email, l.titulo, e.data_prevista
FROM emprestimos e INNER JOIN usuarios u
ON e.id_usuario_fk = u.id_usuario
INNER JOIN livros l ON e.id_livro_fk = l.id_livro
WHERE e.data_devolucao IS NULL
AND e.data_prevista < CURDATE();


CREATE VIEW vw_ranking_leitura AS
SELECT l.id_livro, l.titulo, COUNT(e.id_emprestimo) AS total_emprestimos
FROM livros l INNER JOIN emprestimos e
ON l.id_livro = e.id_livro_fk
GROUP BY l.id_livro, l.titulo
ORDER BY total_emprestimos DESC LIMIT 10;


CREATE VIEW vw_dashboard_financeiro AS
SELECT COUNT(*) AS quantidade_multas_pagas, SUM(valor) AS total_arrecadado
FROM multas WHERE pago = TRUE;


CREATE VIEW vw_meus_emprestimos AS
SELECT 
    e.id_emprestimo,l.titulo AS livro,e.data_saida,e.data_prevista,
    e.data_devolucao,e.id_usuario_fk
FROM emprestimos e
JOIN livros l ON e.id_livro_fk = l.id_livro;


-- CRIAÇÃO DE TRIGGERS --------------------------------------------------------

DELIMITER $$
CREATE TRIGGER trg_trava_horario_comercial_insert
BEFORE INSERT ON livros
FOR EACH ROW
BEGIN
    IF HOUR(NOW()) < 8 OR  
    HOUR(NOW()) >= 18 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Operacao permitida apenas entre 08h e 18h';
    END IF;
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_trava_horario_comercial_update
BEFORE UPDATE ON livros
FOR EACH ROW
BEGIN
	IF HOUR(NOW()) < 8 OR 
    HOUR(NOW()) >= 18 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Operacao permitida apenas entre 08h e 18h';
    END IF;
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_auditoria_delecao_livros
AFTER DELETE ON livros
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
    VALUES('livros', 'DELETE', SESSION_USER(),
    CONCAT('ID=', OLD.id_livro,', TITULO=', OLD.titulo,', AUTOR=', OLD.autor,', ISBN=', OLD.isbn)
    );
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_auditoria_usuario_insert
AFTER INSERT ON usuarios
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
    VALUES('usuarios', 'INSERT', SESSION_USER(), CONCAT('ID=', NEW.id_usuario, ', EMAIL=', NEW.email));
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_auditoria_usuario_delete
AFTER DELETE ON usuarios
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
    VALUES('usuarios', 'DELETE', SESSION_USER(), CONCAT('ID=', OLD.id_usuario, ', EMAIL=', OLD.email));
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_auditoria_usuario_update
AFTER UPDATE ON usuarios
FOR EACH ROW
BEGIN
    IF OLD.tipo <> NEW.tipo OR OLD.email <> NEW.email THEN
        INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
        VALUES('usuarios', 'UPDATE', session_user(), 
        CONCAT('ID=', OLD.id_usuario, ', TIPO_ANTIGO=', OLD.tipo, ', TIPO_NOVO=', NEW.tipo));
    END IF;
END$$
DELIMITER ;

DELIMITER $$
-- Update no pagamento da multa
CREATE TRIGGER trg_auditoria_multa_update
AFTER UPDATE ON multas
FOR EACH ROW
BEGIN
    IF OLD.pago = FALSE AND NEW.pago = TRUE THEN
        INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
        VALUES('multas', 'PAGAMENTO', SESSION_USER(), CONCAT('ID_MULTA=', OLD.id_multa, ', VALOR=', OLD.valor));
    END IF;
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_auditoria_livro_update
AFTER UPDATE ON livros
FOR EACH ROW
BEGIN
    IF OLD.quantidade_estoque <> NEW.quantidade_estoque OR OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
        VALUES('livros', 'UPDATE', SESSION_USER(), 
        CONCAT('ID=', OLD.id_livro, ', ESTOQUE_ANTIGO=', OLD.quantidade_estoque, ', ESTOQUE_NOVO=', NEW.quantidade_estoque));
    END IF;
END$$
DELIMITER ;


DELIMITER $$
-- Criação de novo empréstimo
CREATE TRIGGER trg_auditoria_emprestimo_insert
AFTER INSERT ON emprestimos
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
    VALUES('emprestimos', 'INSERT', SESSION_USER(), CONCAT('ID_EMPRESTIMO=', NEW.id_emprestimo, ', USUARIO=', NEW.id_usuario_fk));
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_auditoria_emprestimo_devolucao
AFTER UPDATE ON emprestimos
FOR EACH ROW
BEGIN
    -- Auditoria apenas quando uma devolução ocorre (data_devolucao mudou de NULL para algo)
    IF OLD.data_devolucao IS NULL AND NEW.data_devolucao IS NOT NULL THEN
        INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
        VALUES('emprestimos', 'DEVOLUCAO', session_user(), 
        CONCAT('ID_EMPRESTIMO=', OLD.id_emprestimo, ', LIVRO_ID=', OLD.id_livro_fk));
    END IF;
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_auditoria_multa_insert
AFTER INSERT ON multas
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
    VALUES('multas', 'INSERT', session_user(), 
    CONCAT('ID_MULTA=', NEW.id_multa, ', VALOR=', NEW.valor, ', EMPRESTIMO_ID=', NEW.id_emprestimo_fk));
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_limite_emprestimos
BEFORE INSERT ON emprestimos
FOR EACH ROW
BEGIN
    DECLARE quantidade INT;
    SELECT COUNT(*)
    INTO quantidade
    FROM emprestimos
    WHERE id_usuario_fk = NEW.id_usuario_fk
    AND data_devolucao IS NULL;
    IF quantidade >= 3 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Usuario atingiu o limite de 3 emprestimos';
    END IF;
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_auditoria_livro_insert
AFTER INSERT ON livros
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
    VALUES('livros', 'INSERT', SESSION_USER(), 
    CONCAT('ID=', NEW.id_livro, ', TITULO=', NEW.titulo, ', ISBN=', NEW.isbn));
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER trg_preventiva_estoque
BEFORE UPDATE ON livros
FOR EACH ROW
BEGIN
    IF NEW.quantidade_estoque < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Estoque nao pode ser negativo';
    END IF;
END$$
DELIMITER ;


-- CRIANDO PROCEDURES  --------------------------------------------------------


DELIMITER $$
CREATE PROCEDURE sp_transacao_cadastro_completo(
    IN p_nome VARCHAR(150), IN p_cpf CHAR(11), IN p_email VARCHAR(100), IN p_senha VARCHAR(255), IN p_tipo VARCHAR(20),
    IN p_logradouro VARCHAR(150), IN p_bairro VARCHAR(100), IN p_cidade VARCHAR(100), IN p_uf CHAR(2))
BEGIN
    DECLARE v_id_usuario INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    INSERT INTO usuarios(nome, cpf, email, senha, tipo)
    VALUES(p_nome, p_cpf, p_email, p_senha, p_tipo);
    
    SET v_id_usuario = LAST_INSERT_ID();

    INSERT INTO enderecos(logradouro, bairro, cidade, uf, id_usuario_fk)
    VALUES(p_logradouro, p_bairro, p_cidade, p_uf, v_id_usuario);

    COMMIT;
END$$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE sp_calcular_multa( IN p_id_emprestimo INT, OUT valor_multa DECIMAL(10,2))
BEGIN
    DECLARE v_data_prevista DATE;
    DECLARE v_data_devolucao DATETIME;
    DECLARE v_dias_atraso INT;
    
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		ROLLBACK;
		RESIGNAL; 
	END;

    IF NOT EXISTS (
		SELECT 1 FROM emprestimos WHERE id_emprestimo = p_id_emprestimo
	) THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'Emprestimo nao encontrado';
	END IF;
    
    SELECT data_prevista, data_devolucao
    INTO v_data_prevista, v_data_devolucao
    FROM emprestimos
    WHERE id_emprestimo = p_id_emprestimo;
    IF v_data_devolucao IS NULL THEN
        SET v_dias_atraso = DATEDIFF(CURDATE(), v_data_prevista);
    ELSE
        SET v_dias_atraso = DATEDIFF(DATE(v_data_devolucao), v_data_prevista);
    END IF;
    IF v_dias_atraso > 0 THEN
        SET valor_multa = v_dias_atraso * 2.00;
    ELSE
        SET valor_multa = 0;
    END IF;
END$$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE sp_excluir_livro(IN p_id_livro INT)
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		ROLLBACK;
		RESIGNAL; 
	END;
	if not exists(SELECT 1 FROM livros WHERE id_livro = p_id_livro) then
		SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Livro não encontrado';
    END IF;
    IF EXISTS (SELECT 1 FROM emprestimos WHERE id_livro_fk = p_id_livro) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Impossivel excluir: este livro possui histórico de emprestimos.';
    END IF;
start transaction;
    DELETE FROM livros WHERE id_livro = p_id_livro;
COMMIT;
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE sp_cadastrar_livro(
    IN p_titulo VARCHAR(150),IN p_autor VARCHAR(100),IN p_isbn VARCHAR(20),
    IN p_preco_custo DECIMAL(10,2),IN p_quantidade INT)
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		ROLLBACK;
		RESIGNAL; 
	END;
	if p_quantidade <=0 then
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'Quantidade deve ser maior que 0';
	END IF;
    INSERT INTO livros (titulo, autor, isbn, preco_custo, quantidade_estoque, status)
    VALUES (p_titulo, p_autor, p_isbn, p_preco_custo, p_quantidade, 'DISPONIVEL');
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE sp_renovar_emprestimo(IN p_id_emprestimo INT)
BEGIN
    DECLARE v_id_livro INT;
    DECLARE v_data_devolucao DATETIME;
    declare v_data_prevista date;
    DECLARE v_id_usuario INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    IF NOT EXISTS (
        SELECT 1
        FROM emprestimos
        WHERE id_emprestimo = p_id_emprestimo
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Emprestimo nao encontrado';
    END IF;

    SELECT id_livro_fk, id_usuario_fk, data_devolucao
	INTO v_id_livro, v_id_usuario, v_data_devolucao
    FROM emprestimos
    WHERE id_emprestimo = p_id_emprestimo;

    IF v_data_devolucao IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Emprestimo ja encerrado';
    END IF;

	IF EXISTS(
		SELECT 1
		FROM reservas r
		WHERE r.id_livro_fk = v_id_livro
		AND r.id_usuario_fk <> v_id_usuario
        AND r.status = 'ATIVA'
	) THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT =
		'Livro reservado por outro usuário';
	END IF;
    
    SELECT data_prevista
    INTO v_data_prevista
    FROM emprestimos
	where id_emprestimo = p_id_emprestimo;
    if v_data_prevista < current_date() then
		SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Não pode ser renovado pois passou do prazo de devolução';
    END IF;
    UPDATE emprestimos
    SET data_prevista = DATE_ADD(current_date(), INTERVAL 7 DAY)
    WHERE id_emprestimo = p_id_emprestimo;
    COMMIT;
END$$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE sp_transacao_emprestimo(IN p_usuario INT, IN p_livro INT)
BEGIN
    DECLARE v_estoque INT;
    DECLARE v_multa_pendente INT;
    DECLARE v_qtd_reservas INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    SELECT COUNT(*) INTO v_multa_pendente
    FROM multas m
    JOIN emprestimos e ON m.id_emprestimo_fk = e.id_emprestimo
    WHERE e.id_usuario_fk = p_usuario AND m.pago = FALSE;

    IF v_multa_pendente > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Emprestimo negado: voce possui multas pendentes.';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = p_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Usuario nao encontrado';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM livros WHERE id_livro = p_livro) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Livro nao encontrado';
    END IF;

    SELECT quantidade_estoque INTO v_estoque FROM livros WHERE id_livro = p_livro;
    IF v_estoque <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Livro sem estoque';
    END IF;

    UPDATE reservas 
    SET status = 'ATENDIDA' 
    WHERE id_usuario_fk = p_usuario AND id_livro_fk = p_livro AND status = 'ATIVA';

    INSERT INTO emprestimos(id_usuario_fk, id_livro_fk, data_prevista)
    VALUES(p_usuario, p_livro, DATE_ADD(CURDATE(), INTERVAL 7 DAY));

    UPDATE livros 
    SET quantidade_estoque = quantidade_estoque - 1
    WHERE id_livro = p_livro;
    
    SELECT COUNT(*) INTO v_qtd_reservas FROM reservas WHERE id_livro_fk = p_livro AND status = 'ATIVA';
    
    UPDATE livros
    SET status = CASE
        WHEN quantidade_estoque > 0 AND v_qtd_reservas > 0 THEN 'RESERVADO'
        WHEN quantidade_estoque > 0 THEN 'DISPONIVEL'
        ELSE 'INDISPONIVEL'
    END
    WHERE id_livro = p_livro;

    COMMIT;
END$$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE sp_obter_id_usuario(IN p_email VARCHAR(100))
SQL SECURITY DEFINER 
BEGIN
    SELECT id_usuario FROM usuarios WHERE email = p_email;
END $$
DELIMITER ;


DELIMITER $$
CREATE DEFINER = CURRENT_USER 
PROCEDURE sp_meus_emprestimos()
SQL SECURITY DEFINER 
BEGIN
    DECLARE v_id INT;
    DECLARE v_nome_usuario VARCHAR(100);

    SET v_nome_usuario = SUBSTRING_INDEX(SESSION_USER(), '@', 1);

    SELECT id_usuario INTO v_id 
    FROM usuarios 
    WHERE email LIKE CONCAT(v_nome_usuario, '%') 
    LIMIT 1;

    IF v_id IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Usuário não encontrado na tabela de usuários.';
    END IF;

    SELECT * FROM vw_meus_emprestimos WHERE id_usuario_fk = v_id;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE sp_reservar_livro(
    IN p_id_usuario INT, IN p_id_livro INT)
BEGIN
    DECLARE v_qtd_emprestimos INT;
    DECLARE v_qtd_reservas INT;
    DECLARE v_ja_reservou INT;
    
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		ROLLBACK;
		RESIGNAL; 
	END;

START transaction;
    SELECT COUNT(*) INTO v_qtd_emprestimos 
    FROM emprestimos 
    WHERE id_usuario_fk = p_id_usuario AND data_devolucao IS NULL;

    SELECT COUNT(*) INTO v_qtd_reservas 
    FROM reservas 
    WHERE id_usuario_fk = p_id_usuario AND status = 'ATIVA';

    IF (v_qtd_emprestimos + v_qtd_reservas) >= 3 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Limite de 3 livros (empréstimos + reservas) atingido.';
    END IF;

    SELECT COUNT(*) INTO v_ja_reservou 
    FROM reservas 
    WHERE id_usuario_fk = p_id_usuario AND id_livro_fk = p_id_livro AND status = 'ATIVA';

    IF v_ja_reservou > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Você já possui uma reserva ativa para este livro.';
    END IF;

    INSERT INTO reservas (id_usuario_fk, id_livro_fk, status)
    VALUES (p_id_usuario, p_id_livro, 'ATIVA');

    UPDATE livros 
    SET status = 'RESERVADO'
    WHERE id_livro = p_id_livro AND status = 'DISPONIVEL';
COMMIT;
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE sp_transacao_devolucao(IN p_id_emprestimo INT, OUT p_valor_multa DECIMAL(10,2))
BEGIN
    DECLARE v_id_livro INT;
    DECLARE v_data_prevista DATE;
    DECLARE v_data_devolucao DATETIME;
    DECLARE v_data_devolucao_existente DATETIME;
    DECLARE v_dias_atraso INT DEFAULT 0;
    DECLARE v_qtd_reservas INT DEFAULT 0;
    DECLARE v_multa DECIMAL(10,2) DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    IF NOT EXISTS (
        SELECT 1
        FROM emprestimos
        WHERE id_emprestimo = p_id_emprestimo
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Emprestimo nao encontrado';
    END IF;
    
	SELECT id_livro_fk, data_prevista, data_devolucao
	INTO v_id_livro, v_data_prevista, v_data_devolucao_existente
	FROM emprestimos
	WHERE id_emprestimo = p_id_emprestimo;

	SELECT COUNT(*)
	INTO v_qtd_reservas
	FROM reservas
	WHERE id_livro_fk = v_id_livro
	AND status = 'ATIVA';
    
    IF v_data_devolucao_existente IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Emprestimo ja devolvido';
    END IF;

    SET v_data_devolucao = NOW();

    UPDATE emprestimos
    SET data_devolucao = v_data_devolucao
    WHERE id_emprestimo = p_id_emprestimo;

    UPDATE livros
	SET quantidade_estoque = quantidade_estoque + 1
	WHERE id_livro = v_id_livro;
    
    UPDATE livros
	SET status =
		CASE
			WHEN quantidade_estoque > 0 AND v_qtd_reservas = 0
			THEN 'DISPONIVEL'

			WHEN quantidade_estoque > 0 AND v_qtd_reservas > 0
			THEN 'RESERVADO'

			ELSE 'INDISPONIVEL'
		END
	WHERE id_livro = v_id_livro;
    
	SET p_valor_multa = 0.00;
    IF DATE(v_data_devolucao) > v_data_prevista THEN
        SET v_dias_atraso = DATEDIFF(DATE(v_data_devolucao), v_data_prevista);
        SET p_valor_multa = v_dias_atraso * 2.00; 
        
        INSERT INTO multas (id_emprestimo_fk, valor, pago)
        VALUES (p_id_emprestimo, p_valor_multa, FALSE);
    END IF;
    COMMIT;
END$$
DELIMITER ;


-- CRIANDO ROLES. Ao invés de criar users, preferi criar roles e após cadastrar usuário, criar user e definir a role de acordo com o tipo de usuário.
drop role if exists 'rl_gerente', 'rl_bibliotecario', 'rl_estagiario', 'rl_aluno';

create Role 'rl_gerente', 'rl_bibliotecario', 'rl_estagiario', 'rl_aluno';

-- GARANTINDO PRIVILÉGIOS --------------------------------------------------------

-- GERENTE
-- Dei permissão total para que a gerente pudesse cadastrar novos usuários e definir roles pra esses usuários, bem como garantir permissões.

GRANT ALL PRIVILEGES
ON *.*
TO 'rl_gerente';

-- BIBLIOTECARIO

GRANT SELECT, INSERT, UPDATE
ON db_libritech.livros
TO 'rl_bibliotecario';
GRANT SELECT, INSERT, UPDATE
ON db_libritech.emprestimos
TO 'rl_bibliotecario';
GRANT SELECT
ON db_libritech.multas
TO 'rl_bibliotecario';
GRANT SELECT
ON db_libritech.log_auditoria
TO 'rl_bibliotecario';
GRANT SELECT 
on db_libritech.vw_dashboard_financeiro
to 'rl_bibliotecario';
grant select 
on db_libritech.vw_ranking_leitura
to 'rl_bibliotecario';
REVOKE DELETE ON db_libritech.log_auditoria 
FROM 'rl_bibliotecario';
GRANT EXECUTE ON PROCEDURE db_libritech.sp_calcular_multa
TO 'rl_bibliotecario';
grant execute on procedure db_libritech.sp_renovar_emprestimo
to 'rl_bibliotecario';
grant execute on procedure db_libritech.sp_transacao_devolucao
to 'rl_bibliotecario';
grant execute on procedure db_libritech.sp_transacao_emprestimo
to 'rl_bibliotecario';
grant select on db_libritech.vw_livros_atrasados
to 'rl_bibliotecario';
grant execute on procedure db_libritech.sp_reservar_livro
to 'rl_bibliotecario';
GRANT EXECUTE ON PROCEDURE 
db_libritech.sp_obter_id_usuario 
TO 'rl_bibliotecario';

-- ESTAGIÁRIO

GRANT SELECT
ON db_libritech.livros
TO 'rl_estagiario';
GRANT select, INSERT
ON db_libritech.emprestimos
TO 'rl_estagiario';
grant select 
on db_libritech.vw_ranking_leitura
to 'rl_estagiario';
GRANT DELETE ON db_libritech.* 
TO 'rl_estagiario';
REVOKE DELETE ON db_libritech.* 
FROM 'rl_estagiario';
grant execute on procedure db_libritech.sp_transacao_emprestimo
to 'rl_estagiario';
grant select on db_libritech.vw_livros_atrasados
to 'rl_estagiario';
grant execute on procedure db_libritech.sp_reservar_livro
to 'rl_estagiario';
GRANT EXECUTE ON PROCEDURE 
db_libritech.sp_obter_id_usuario 
TO 'rl_estagiario';

-- ALUNO
GRANT SELECT
ON db_libritech.vw_acervo_publico
TO 'rl_aluno';
GRANT SELECT
ON db_libritech.vw_ranking_leitura
TO 'rl_aluno';
GRANT SELECT 
ON db_libritech.vw_meus_emprestimos 
TO 'rl_aluno';
GRANT EXECUTE ON PROCEDURE 
`db_libritech`.`sp_meus_emprestimos` 
TO rl_aluno;
Grant execute on procedure 
db_libritech.sp_reservar_livro
to rl_aluno;
GRANT EXECUTE ON PROCEDURE 
db_libritech.sp_obter_id_usuario 
TO 'rl_aluno';
flush privileges;


-- CRIANDO USERS --------------------------------------------------------

drop user if exists 'ana@email.com', 'bruno@email.com','carla@email.com', 'daniel@email.com', 'fernanda@email.com';

create User 'ana@email.com'@'%' identified by '123';
create User 'bruno@email.com'@'%' identified by '123';
create User 'carla@email.com'@'%' identified by '123';
create User 'daniel@email.com'@'%' identified by '123';
create User 'fernanda@email.com'@'%' identified by '123';

-- DEFININDO OS ROLES AOS USUÁRIOS --------------------------------------------------------
GRANT 'rl_aluno' to 'ana@email.com'@'%', 'bruno@email.com'@'%', 'carla@email.com'@'%';
GRANT 'rl_bibliotecario' to 'daniel@email.com'@'%';
GRANT 'rl_gerente' to 'fernanda@email.com'@'%';

SET DEFAULT ROLE 'rl_aluno' TO 'ana@email.com'@'%','bruno@email.com'@'%','carla@email.com'@'%';
SET DEFAULT ROLE 'rl_bibliotecario' to 'daniel@email.com'@'%';
set default role 'rl_gerente' to 'fernanda@email.com'@'%';

-- ADICIONANDO INSERTS PARA TESTE --------------------------------------------------------

INSERT INTO usuarios (nome, cpf, email, senha, tipo) VALUES
('Ana Silva', '11111111111', 'ana@email.com', '123', 'ALUNO'),
('Bruno Souza', '22222222222', 'bruno@email.com', '123', 'ALUNO'),
('Carla Mendes', '33333333333', 'carla@email.com', '123', 'ALUNO'),
('Daniel Lima', '44444444444', 'daniel@email.com', '123', 'BIBLIOTECARIO'),
('Fernanda Rocha', '55555555555', 'fernanda@email.com', '123', 'GERENTE');

INSERT INTO enderecos (logradouro, bairro, cidade, uf, id_usuario_fk) VALUES
('Rua A', 'Centro', 'Campina Grande', 'PB', 1),
('Rua B', 'Bairro Novo', 'Campina Grande', 'PB', 2),
('Rua C', 'Prata', 'Campina Grande', 'PB', 3),
('Av. Central', 'Centro', 'Campina Grande', 'PB', 4),
('Rua das Flores', 'Catolé', 'Campina Grande', 'PB', 5);

INSERT INTO livros (titulo, autor, isbn, preco_custo, quantidade_estoque, status) VALUES
('Clean Code', 'Robert C. Martin', '9780132350884', 80.00, 3, 'DISPONIVEL'),
('Database System Concepts', 'Silberschatz', '9780073523323', 120.00, 2, 'DISPONIVEL'),
('Estruturas de Dados', 'Mark Weiss', '9788576058813', 90.00, 1, 'DISPONIVEL'),
('Engenharia de Software', 'Ian Sommerville', '9780137035151', 100.00, 2, 'DISPONIVEL'),
('Algoritmos', 'Cormen', '9780262033848', 150.00, 1, 'DISPONIVEL');


-- empréstimo recente (sem multa)
INSERT INTO emprestimos (id_usuario_fk, id_livro_fk, data_saida, data_prevista, data_devolucao)
VALUES (1, 1, NOW(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), NULL);

-- empréstimo atrasado
INSERT INTO emprestimos (id_usuario_fk, id_livro_fk, data_saida, data_prevista, data_devolucao)
VALUES (2, 2, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 7 DAY), NULL);

-- empréstimo já devolvido
INSERT INTO emprestimos (id_usuario_fk, id_livro_fk, data_saida, data_prevista, data_devolucao)
VALUES (3, 3, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 7 DAY), NOW());


INSERT INTO multas (id_emprestimo_fk, valor, pago) VALUES
(2, 14.00, FALSE),
(3, 0.00, TRUE);

-- ANTES DA CRIAÇÃO DOS INDICES ---------------------------------------------
EXPLAIN
SELECT * FROM emprestimos
WHERE data_prevista < CURDATE();

EXPLAIN
SELECT * FROM livros
WHERE titulo = 'Dom Casmurro';

EXPLAIN
SELECT * FROM livros
WHERE status = 'DISPONIVEL';

-- CRIANDO INDICES --------------------------------------------------------

-- Criado para otimizar a busca no controle de atrasos, multa e relatorios
CREATE INDEX idx_emprestimo_data_prevista
ON emprestimos(data_prevista);

-- Criamos esse índice por que a pesquisa por título é a pesquisa mais comum ao procurar um livro na biblioteca/livraria
CREATE INDEX idx_livro_titulo
ON livros(titulo);

-- Criado para otimizar a consulta de livros disponiveis
CREATE INDEX idx_livro_status
ON livros(status);

-- EXPLAINS COM OS DEPOIS DOS INDEX ACIMA --------------------------------------------------------

EXPLAIN
SELECT * FROM emprestimos
WHERE data_prevista < CURDATE();

EXPLAIN
SELECT * FROM livros
WHERE titulo = 'Dom Casmurro';

EXPLAIN
SELECT * FROM livros
WHERE status = 'DISPONIVEL';