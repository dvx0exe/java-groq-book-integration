import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GroqClient {

    private static final String GROQ_API_KEY = System.getenv("GROQ_API_KEY");
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public static void main(String[] args) {
        try (Scanner leia = new Scanner(System.in)) {
            System.out.println("Entre com o nome de um autor:");
            String nomeDoAutor =("Lewis Carroll") ;//leia.nextLine();

            String promptDoUsuario = String.format(
                    "Liste os 5 livros do autor '%s'. " +
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
                        "temperature": 0.0
                    }
                    """.formatted(promptDoUsuario.replace("\"", "\\\""));

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

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                JsonNode rootNode = mapper.readTree(responseBody);

                String conteudoDaMensagem = rootNode
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

                System.out.println("Resposta recebida....");

                Bibliografia dados = mapper.readValue(conteudoDaMensagem, Bibliografia.class);
                if (dados != null && dados.getBibliografia() != null) {
                    System.out.println("\n--- Livros Encontrados (Objetos Java) ---");

                    for (Livro livro : dados.getBibliografia()) {
                        System.out.println("------------------------------------------------");
                        System.out.println("Título: " + livro.getNomeDoLivro());
                        System.out.println("Ano: " + livro.getAnoPublicacao());
                        System.out.println("Gênero: " + livro.getGeneroLiterario());
                        System.out.println("Editora: " + livro.getEditoraClassica());
                        System.out.println("Páginas: " + livro.getNumeroPaginasEstimado());
                        System.out.println("Sinopse: " + livro.getResumoSinopse());
                    }
                    System.out.println("------------------------------------------------");
                } else {
                    System.out.println("A lista 'bibliografia' veio vazia ou o JSON estava incorreto.");
                }

            } else {
                System.err.println("Erro na API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class Bibliografia {
        private List<Livro> bibliografia;

        public List<Livro> getBibliografia() { return bibliografia; }
        public void setBibliografia(List<Livro> bibliografia) { this.bibliografia = bibliografia; }
    }

    public static class Livro {

        @JsonProperty("nome_do_livro")
        private String nomeDoLivro;

        @JsonProperty("ano_publicacao")
        private String anoPublicacao;

        @JsonProperty("editora_classica")
        private String editoraClassica;

        @JsonProperty("numero_paginas_estimado")
        private int numeroPaginasEstimado;

        @JsonProperty("genero_literario")
        private String generoLiterario;

        @JsonProperty("resumo_sinopse")
        private String resumoSinopse;

        public String getNomeDoLivro() { return nomeDoLivro; }
        public String getAnoPublicacao() { return anoPublicacao; }
        public String getEditoraClassica() { return editoraClassica; }
        public int getNumeroPaginasEstimado() { return numeroPaginasEstimado; }
        public String getGeneroLiterario() { return generoLiterario; }
        public String getResumoSinopse() { return resumoSinopse; }
    }
}
