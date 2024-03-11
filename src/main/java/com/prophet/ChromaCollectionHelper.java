package com.prophet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Swarit Joshipura
 *
 * This class contains the business logic to create a collection in ChromaDB
 */
public class ChromaCollectionHelper {


    public static void main(String[] args) {
        createCollectionForChromaDB();
        System.out.println("Created collection for Chroma DB!");
    }

    /**
     * Creates a new collection in ChromaDB with a unique name. This method initiates an HTTP POST request to the ChromaDB
     * collections API endpoint to create a new collection. Each collection is given a unique name by concatenating "HuggingFace"
     * with a randomly generated UUID, ensuring that each collection name is distinct.
     *
     * The method uses OkHttpClient to manage the HTTP request and Jackson's ObjectMapper to construct the JSON payload
     * required by the ChromaDB API. Upon successful creation of the collection, a success message is printed to the console.
     * If the attempt to create the collection fails, the method prints an error message detailing the failure. This includes
     * failures due to issues with the HTTP request itself or an unsuccessful response from the ChromaDB API.
     *
     * Usage example:
     * Assuming ChromaDB is running locally and accessible, this method can be called directly to create a new collection
     * without needing to specify any parameters. It's useful for preparing the database environment before storing or
     * retrieving embeddings.
     *
     * Exceptions:
     * - IOException: If an error occurs during the HTTP request execution, such as a network failure or interruption.
     *   This exception is caught within the method and results in an error message being printed to the console.
     *
     * Note: This method assumes that ChromaDB is running and accessible via "http://localhost:8000".
     * If ChromaDB is hosted at a different URL, the `chromaDBUrl` variable should be updated accordingly.
     */

    public static void createCollectionForChromaDB() {
        OkHttpClient client = new OkHttpClient();
        String chromaDBUrl = "http://localhost:8000/api/v1/collections";

        ObjectMapper objectMapper = new ObjectMapper();
        // Construct Chroma Vector DB format
        JsonNode chromaJson = objectMapper.createObjectNode()
                .put("name", "HuggingFace" + UUID.randomUUID().toString());

        RequestBody body = RequestBody.
                create(MediaType.parse("application/json; charset=utf-8")
                        , chromaJson.toString());

        Request request = new Request.Builder()
                .url(chromaDBUrl)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println("Embedding persisted successfully to Chroma Vector DB!");
            } else {
                System.err.println("Failed to persist embedding to Chroma Vector DB. Response: " + response.body().string());
            }
        } catch (IOException e) {
            System.err.println("Failed to make HTTP request: " + e.getMessage());
        }
    }
}
