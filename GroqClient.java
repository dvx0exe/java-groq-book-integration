import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

public class GroqClient {

    private static final String GROQ_API_KEY = System.getenv("GROQ_API_KEY");
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final String DB_URL = "jdbc:mysql://localhost:3306/biblioteca";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    public static void main(String[] args) {
        if (GROQ_API_KEY == null || DB_PASSWORD == null) {
            System.err.println("ERRO: Configure as variáveis de ambiente GROQ_API_KEY e DB_PASSWORD antes de executar.");
            return;
        }

        try (Scanner leia = new Scanner(System.in)) {
            System.out.println("Entre com o nome de um autor:");
            String nomeDoAutor = leia.nextLine();

            String promptDoUsuario = String.format(
                    "Liste 5 principais livros do autor '%s'. " +
                            "Retorne APENAS o JSON cru, sem markdown. " +
                            "Estrutura: objeto com lista 'bibliografia'. " +
                            "Campos: nome_do_livro, ano_publicacao (apenas numeros), editora_classica, numero_paginas_estimado, genero_literario, resumo_sinopse.",
                    nomeDoAutor
            );

            String jsonBody = """
                    {
                        "model": "llama-3.1-8b-instant",
                        "messages": [
                            {
                                "role": "system",
                                "content": "Você é um bibliotecário especialista. Responda apenas com JSON."
                            },
                            {
                                "role": "user",
                                "content": "%s"
                            }
                        ],
                        "response_format": { "type": "json_object" },
                        "temperature": 0.2
                    }
                    """.formatted(promptDoUsuario.replace("\"", "\\\"")); // Escape simples para aspas

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Authorization", "Bearer " + GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            System.out.println("Consultando API da Groq para: " + nomeDoAutor + "...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // 1. Extração do JSON
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                String conteudoDaMensagem = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();

                System.out.println("Resposta recebida. Processando...");

                Gson gson = new GsonBuilder().setLenient().create();
                Bibliografia dados = gson.fromJson(conteudoDaMensagem, Bibliografia.class);

                if (dados != null && dados.getBibliografia() != null) {
                    System.out.println("\n--- Livros Encontrados: " + dados.getBibliografia().size() + " ---");
                    salvarNoBanco(dados.getBibliografia());
                } else {
                    System.out.println("A lista 'bibliografia' veio vazia.");
                }

            } else {
                System.err.println("Erro na API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void salvarNoBanco(List<Livro> livros) {
        System.out.println("Conectando ao banco de dados 'biblioteca'...");
        String sql = "INSERT INTO livros (nome_do_livro, ano_publicacao, editora, paginas, genero, sinopse) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Livro livro : livros) {
                stmt.setString(1, livro.getNomeDoLivro());

                int ano = 0;
                try {
                    // Limpeza básica do ano
                    String anoLimpo = livro.getAnoPublicacao() != null ? livro.getAnoPublicacao().replaceAll("\\D", "") : "0";
                    if (!anoLimpo.isEmpty()) ano = Integer.parseInt(anoLimpo);
                } catch (Exception e) {
                    System.err.println("Aviso: Erro ao converter ano: " + livro.getAnoPublicacao());
                }
                stmt.setInt(2, ano);
                stmt.setString(3, livro.getEditoraClassica());
                stmt.setInt(4, livro.getNumeroPaginasEstimado());
                stmt.setString(5, livro.getGeneroLiterario());
                stmt.setString(6, livro.getResumoSinopse());

                stmt.executeUpdate();
                System.out.println("Livro salvo: " + livro.getNomeDoLivro());
            }
            System.out.println("Processo concluído com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro no banco de dados: " + e.getMessage());
        }
    }

    public static class Bibliografia {
        private List<Livro> bibliografia;
        public List<Livro> getBibliografia() { return bibliografia; }
        public void setBibliografia(List<Livro> bibliografia) { this.bibliografia = bibliografia; }
    }

    public static class Livro {
        @SerializedName("nome_do_livro") private String nomeDoLivro;
        @SerializedName("ano_publicacao") private String anoPublicacao;
        @SerializedName("editora_classica") private String editoraClassica;
        @SerializedName("numero_paginas_estimado") private int numeroPaginasEstimado;
        @SerializedName("genero_literario") private String generoLiterario;
        @SerializedName("resumo_sinopse") private String resumoSinopse;

        public String getNomeDoLivro() { return nomeDoLivro; }
        public String getAnoPublicacao() { return anoPublicacao; }
        public String getEditoraClassica() { return editoraClassica; }
        public int getNumeroPaginasEstimado() { return numeroPaginasEstimado; }
        public String getGeneroLiterario() { return generoLiterario; }
        public String getResumoSinopse() { return resumoSinopse; }
    }
}