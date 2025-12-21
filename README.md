# Java Groq Integration: Gerador de Bibliografia

Este projeto é uma implementação em Java puro que demonstra como integrar a API da **Groq** (usando o modelo `llama-3.1-8b-instant`) para gerar dados estruturados.

O foco principal é solicitar informações em formato JSON estrito (JSON Mode) e deserializá-las automaticamente para objetos Java (POJOs) usando a biblioteca **Jackson**, eliminando a necessidade de parsing manual de strings.

## 🚀 Funcionalidades

* **Conexão Nativa HTTP**: Utiliza `java.net.http.HttpClient` (Java 11+), sem dependências pesadas de clientes HTTP externos.
* **Structured Outputs**: Configura a API para retornar apenas JSON válido (`response_format: { "type": "json_object" }`).
* **Mapeamento de Objetos (ORM para IA)**: Converte a resposta da IA diretamente para as classes `Bibliografia` e `Livro`.
* **Engenharia de Prompt**: Prompt otimizado para extrair dados bibliográficos (título, ano, editora, gênero, sinopse).

## 📋 Pré-requisitos

* **Java 11** ou superior.
* **Maven** (ou Gradle) para gerenciar a dependência do Jackson.
* Uma **API Key da Groq**.

## 🛠️ Configuração e Instalação

### 1. Dependências

Este projeto utiliza **Jackson Databind** para processar o JSON. Adicione ao seu `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version> </dependency>
</dependencies>

```

### 2. Variável de Ambiente

Por segurança, a chave da API não é hardcoded. Configure-a no seu sistema operacional:

**Linux/Mac:**

```bash
export GROQ_API_KEY="gsk_sua_chave_aqui..."

```

**Windows (PowerShell):**

```powershell
$env:GROQ_API_KEY="gsk_sua_chave_aqui..."

```

## 💻 Como Usar

1. Clone o repositório ou baixe o arquivo `GroqClient.java`.
2. O código atualmente busca livros de **"Lewis Carroll"** (hardcoded para testes rápidos).
* *Para mudar o autor:* Descomente a linha `Scanner` no método `main` e comente a definição estática da variável `nomeDoAutor`.


3. Compile e execute a classe `GroqClient`.

### Exemplo de Saída no Console

```text
Consultando API da Groq para: Lewis Carroll...
Resposta recebida....

--- Livros Encontrados (Objetos Java) ---
------------------------------------------------
Título: Alice no País das Maravilhas
Ano: 1865
Gênero: Fantasia, Literatura Infantil
Editora: Macmillan
Páginas: 200
Sinopse: A história de uma menina chamada Alice que cai numa toca de coelho...
------------------------------------------------
Título: Alice Através do Espelho
Ano: 1871
...

```

## 🧩 Estrutura das Classes

O projeto mapeia o JSON para as seguintes estruturas internas:

* **`Bibliografia`**: Contém uma lista de livros.
* **`Livro`**: Objeto com os detalhes:
* `nome_do_livro`
* `ano_publicacao`
* `editora_classica`
* `numero_paginas_estimado`
* `genero_literario`
* `resumo_sinopse`



## ⚠️ Notas Importantes

* **Modelo Utilizado**: O código está configurado para usar o `llama-3.1-8b-instant`, que é extremamente rápido e barato, ideal para tarefas de formatação JSON simples.
* **Tratamento de Erros**: O `ObjectMapper` está configurado com `FAIL_ON_UNKNOWN_PROPERTIES = false` para evitar quebras caso a IA alucine campos extras.
