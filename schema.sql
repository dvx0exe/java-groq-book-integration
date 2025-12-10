DROP DATABASE biblioteca;
CREATE DATABASE IF NOT EXISTS biblioteca;
USE biblioteca;

CREATE DATABASE IF NOT EXISTS biblioteca;
USE biblioteca;
DROP TABLE IF EXISTS livros;
CREATE TABLE livros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_do_livro VARCHAR(255),
    ano_publicacao INT,
    editora VARCHAR(100),
    paginas INT,
    genero VARCHAR(100),
    sinopse TEXT
);