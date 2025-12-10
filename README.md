# Integração Java + Groq AI + MySQL

Este projeto é uma aplicação Java que consulta a API da Groq (Llama 3) para obter recomendações de livros de um autor específico e salva os resultados automaticamente em um banco de dados MySQL.

## 🛠 Tecnologias Utilizadas
* Java 17
* Maven
* MySQL 8
* Groq API (Llama-3.1-8b-instant)
* Gson (JSON Parsing)

## 📋 Pré-requisitos

1.  **MySQL**: Tenha o MySQL rodando na porta `3306`.
2.  **Banco de Dados**: Execute o script `schema.sql` para criar o banco e a tabela.
3.  **API Key**: Obtenha uma chave gratuita em [console.groq.com](https://console.groq.com).

## 🚀 Como Configurar e Rodar

### 1. Variáveis de Ambiente
O projeto exige as seguintes variáveis de ambiente configuradas no seu sistema operacional ou na configuração de execução da sua IDE:

* `GROQ_API_KEY`: Sua chave da API Groq.
* `DB_PASSWORD`: A senha do seu usuário `root` do MySQL.

### 2. Executando o projeto
Se estiver usando terminal:

```bash
mvn clean package
# Configure as variáveis antes de rodar o jar ou a classe main
java -cp target/sua-aplicacao.jar com.exemplo.GroqClient
