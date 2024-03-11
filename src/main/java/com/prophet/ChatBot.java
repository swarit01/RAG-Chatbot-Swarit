package com.prophet;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Swarit Joshipura
 * @company Prophet Security
 *
 * This class contains the business logic for a terminal based chatbot, Raj.
 *
 */
public class ChatBot {
    private static String prevQuery = null;
    private static String prevAnswer = null;
    private static final String OPENAI_API_KEY= "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";


    /**
     * Serves as the entry point for the Prophet Security chatbot application. This method initializes the chatbot and
     * handles the user interaction loop. It welcomes the user, then continuously prompts for questions until the user
     * decides to exit. For each query, it generates an embedding using OpenAI, performs a semantic search with ChromaDB,
     * and generates a response based on the search results and the original query.
     *
     * @param args The command-line arguments passed to the program (not used).
     * @throws IOException If an I/O error occurs during the handling of user queries.
     */

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Prophet Security! I am Raj and I will be assisting you today.");
        while (true) {
            System.out.println("Please ask more questions, or type 'exit' to exit the system.");
            String userQuery = scanner.nextLine();
            if (userQuery.equalsIgnoreCase("Exit")) {
                System.out.println("Thank you for using Prophet GPT!");
                break;
            }
            JsonNode userQueryEmbedding = OpenAIEmbeddingsHelper.generateEmbedding(userQuery);
            String ragOutput = ChromaCRUDHelper.performSemanticSearch(userQueryEmbedding);
            String resp = generateResponse(userQuery, ragOutput);
            System.out.println(resp);
        }
    }

    /**
     * Generates a response to the user's query using the OpenAI GPT model, incorporating the output from ChromaDB
     * (RAG output) for enhanced contextual relevance. This method constructs a JSON payload with the chat history,
     * including system messages (such as RAG output and previous interactions) and the latest user query. It then
     * sends this payload to the OpenAI API to generate a conversational response. The method maintains a history
     * of the previous query and response to inform subsequent responses, enriching the conversation context.
     *
     * @param userQuery The current user query as a String.
     * @param ragOutput The output from ChromaDB representing the retrieval-augmented generation component, used to
     *                  enhance the context of the conversation.
     * @return A String containing the generated response from the OpenAI API, based on the user query and the
     *         context provided by the RAG output and any previous interaction.
     * @throws IOException If an I/O error occurs when sending the request or receiving the response from the OpenAI API.
     *                     This could be due to network issues, problems with the OpenAI service, or issues with reading
     *                     the response.
     *
     * Note: This method utilizes the OkHttpClient for HTTP requests and the Jackson library for JSON processing. It also
     * relies on two static variables, `prevQuery` and `prevAnswer`, to store the history of the previous query and response
     * for context. Ensure that the `OPENAI_API_KEY` is correctly set with your OpenAI API key for authentication.
     */

    private static String generateResponse(String userQuery, String ragOutput) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.openai.com/v1/chat/completions";

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("model", "gpt-3.5-turbo");
        ArrayNode messagesNode = objectMapper.createArrayNode();
        ObjectNode systemMessageNode = objectMapper.createObjectNode();
        systemMessageNode.put("role", "system");
        if (prevQuery == null) {
            systemMessageNode.put("content", "Rag output is: " + ragOutput);
        }
        else {
            systemMessageNode.put("content", " The previous user query was:  " + prevQuery + "and you responded" + prevAnswer + "Rag output is:" + ragOutput);
        }
        prevQuery = userQuery;
        messagesNode.add(systemMessageNode);
        ObjectNode userMessageNode = objectMapper.createObjectNode();
        userMessageNode.put("role", "user");
        userMessageNode.put("content", userQuery);
        messagesNode.add(userMessageNode);

        rootNode.set("messages", messagesNode);
        String requestBody = objectMapper.writeValueAsString(rootNode);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode2 = mapper.readTree(response.body().string());
                JsonNode firstChoice = rootNode2.path("choices").get(0);
                String content = firstChoice.path("message").path("content").asText();
                prevAnswer = content;
                return content;
            } else {
                throw new IOException("Unexpected response code: " + response.code() + ", " + response.body().string());
            }
        }
    }
}