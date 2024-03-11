package com.prophet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

/**
 * @author Swarit Joshipura
 *
 * This class contains logic to store embeddings into a given collection in ChromaDB.
 */
public class ChromaCRUDHelper {
    private static final String collectionID = "f04a71d2-6beb-4159-81a7-f57665bf1381";

    /**
     * The main method of this class is responsible to generate embeddings and store them in the collection provided.

     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        List<String> parsedText = parseTextFromDataSet();
        for (int i = 0; i < parsedText.size(); i++) {
            List<String> cleanData = ProcessText.cleanData(parsedText.get(i));
            for (int j = 0; j < cleanData.size(); j++) {
                JsonNode embedding = OpenAIEmbeddingsHelper.generateEmbedding(cleanData.get(j));
                if(embedding != null) {
                    ChromaCRUDHelper.persistEmbeddingsToChromaDB(cleanData.get(j), embedding, collectionID);
                }
            }
        }
    }

    /**
     * Retrieves the first rows of a specified dataset from the Hugging Face datasets server and extracts text content
     * from each row. This method constructs a URL with query parameters specifying the dataset, its configuration, and
     * the desired data split (e.g., training set). It then sends an HTTP GET request to the server. If the request is
     * successful, the method parses the JSON response to extract text from each row and returns a list of these text
     * strings. If the request fails or an IOException occurs, the method will either return null or print an error
     * message indicating the failure.
     *
     * @return A List of Strings, where each String is the text content from a row in the dataset. Returns null if the
     *         request fails or if an IOException is caught during the request execution or response handling.
     *
     * Usage example:
     * To parse text from the "pubmed_other" dataset provided by "TaylorAI" on the Hugging Face datasets server, this
     * method can be invoked directly without any arguments. It is preset to query this specific dataset in its "default"
     * configuration and "train" split:
     *
     * List<String> datasetTexts = parseTextFromDataSet();
     *
     * Note 1: This method is designed to work specifically with the "https://datasets-server.huggingface.co/first-rows"
     * endpoint and expects the JSON response to contain a "rows" array. It may need adjustments to work with other
     * datasets or endpoints. The URL and dataset parameters are hardcoded for demonstration purposes and might need
     * to be parameterized or configured externally for more generic use.
     *
     * Note 2: I purposely modified the baseURL to use first-rows, so it does not bloat the system trying to fetch the dataset.
     */

    private static List<String> parseTextFromDataSet() {
        OkHttpClient client = new OkHttpClient();

        String baseUrl = "https://datasets-server.huggingface.co/first-rows";

        // Define query parameters
        String dataset = "TaylorAI/pubmed_other";
        String config = "default";
        String split = "train";

        // Construct the URL with query parameters
        String url = String.format("%s?dataset=%s&config=%s&split=%s", baseUrl, dataset, config, split);

        // Create a request
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            // Execute the request
            Response response = client.newCall(request).execute();
            ObjectMapper mapper = new ObjectMapper();
            // Check if the request was successful (status code 200)
            if (response.isSuccessful()) {
                JsonNode jsonResponse = mapper.readTree(response.body().string());

                // Extract the "rows" array
                JsonNode rows = jsonResponse.get("rows");
                List<String> listOfRows = new ArrayList<>();
                // Iterate over each row
                for (JsonNode row : rows) {
                    String text = row.get("row").get("text").asText();
                    listOfRows.add(text);
                }
                return listOfRows;
            }
            else {
                // Print an error message if the request failed
                System.out.println("Failed to fetch data. Status code: " + response.code());
                return null;
            }
        } catch (IOException e) {
            System.out.println("An error occured while trying to fetch data from Hugging Face!" + e.getMessage());
            return null;
        }
    }

    /**
     * Persists a given embedding and its associated metadata to a specific collection in ChromaDB. This method constructs
     * a JSON payload containing the embedding, metadata, document data, URIs, and automatically generated IDs, then sends
     * this payload as a POST request to the ChromaDB API to add the data to the specified collection. The method allows for
     * the structured storage of embeddings and their related information in ChromaDB, facilitating later retrieval and
     * analysis.
     *
     * @param cleanData The textual data associated with the embedding. This data is what the embedding represents,
     *                  and is stored as part of the document data in the database.
     * @param embedding The embedding to be persisted, represented as a JsonNode. This should be the numerical vector
     *                  representation of the provided text data.
     * @param collectionID The ID of the collection in ChromaDB where the embedding and its metadata should be added.
     *                     This ID specifies the target collection for the new data entry.
     * @return A String containing the response body from ChromaDB upon successfully adding the data to the collection.
     *         The response typically includes confirmation of the data addition or details of any errors encountered.
     * @throws IOException If an error occurs during the HTTP request execution. This could be due to network issues,
     *                     problems forming the request, or issues encountered while processing the response from ChromaDB.
     *
     * Example usage:
     * String response = persistEmbeddingsToChromaDB("This is sample text", embeddingJsonNode, "myCollectionId");
     * This would add the embedding for "This is sample text" along with its metadata to the collection identified by
     * "myCollectionId" in ChromaDB.
     *
     * Note: The method constructs the payload by converting the embedding JsonNode into an array format suitable for
     * ChromaDB, associating it with minimal metadata, document data, and URIs for completeness. This implementation assumes
     * that the ChromaDB instance is running and accessible at "http://localhost:8000/api/v1/collections/" and that the
     * collection specified by `collectionID` already exists. The method will throw a RuntimeException if any IOException
     * is caught during execution, indicating that the persistence operation has failed.
     */

    public static String persistEmbeddingsToChromaDB(String cleanData, JsonNode embedding, String collectionID) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "http://localhost:8000/api/v1/collections/" + collectionID + "/add";

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("embeddings",  convertJsonNodeToEmbeddingsArray(embedding));
        bodyMap.put("metadatas", new Object[]{new HashMap<>(), null});
        bodyMap.put("documents", new String[]{cleanData, null});
        bodyMap.put("uris", new String[]{"string", null});
        bodyMap.put("ids", new String[]{"string" + UUID.randomUUID().toString()});

        String requestBodyJson = objectMapper.writeValueAsString(bodyMap);

        RequestBody body = RequestBody.create(requestBodyJson, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a semantic search against a specified collection in ChromaDB to find the nearest neighbors for a given query embedding.
     * This method constructs a JSON payload containing the user query embedding and the desired number of results, then sends a POST
     * request to the ChromaDB query endpoint. The method parses the response to extract and return the documents closest to the query
     * embedding based on semantic similarity.
     *
     * @param userQueryEmbedding The embedding of the user query as a JsonNode, which serves as the input for the semantic search.
     * @return A String representation of the list of documents that are the nearest neighbors to the given query embedding. If the
     *         search fails or an exception occurs, the method prints the stack trace and returns null.
     * @throws RuntimeException If an IOException occurs while making the HTTP request or processing the response, encapsulated as a
     *         RuntimeException for simplicity.
     *
     */

    public static String performSemanticSearch(JsonNode userQueryEmbedding) {
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            String jsonString = constructJsonRequestBody(convertJsonNodeToEmbeddingsArray(userQueryEmbedding), 10);
            RequestBody body = RequestBody.create(jsonString, JSON);

            Request request = new Request.Builder()
                    .url("http://localhost:8000/api/v1/collections/" + collectionID + "/query")
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> documentList = new ArrayList<>();
                JsonNode rootNode = objectMapper.readTree(response.body().string());
                JsonNode documentsNode = rootNode.path("documents");

                if (documentsNode.isArray()) {
                    for (JsonNode docArray : documentsNode) {
                        for (JsonNode doc : docArray) {
                            documentList.add(doc.asText());
                        }
                    }
                }
                return documentList.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method constructs the body of the JSON object needed to send a POST request to ChromaDB.
     * @param embeddings
     * @param nResults
     * @return
     * @throws Exception
     */
    private static String constructJsonRequestBody(double[][] embeddings, int nResults) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        var rootNode = objectMapper.createObjectNode();
        rootNode.putPOJO("query_embeddings", embeddings); // Add embeddings
        rootNode.putObject("where");
        rootNode.putObject("where_document");
        rootNode.put("n_results", nResults);
        rootNode.putArray("include").add("metadatas").add("documents").add("distances");

        return objectMapper.writeValueAsString(rootNode);
    }

    /**
     * Converts a JsonNode containing embedding vectors into a 2D double array. This method is designed to work with a JsonNode
     * that represents a single-dimensional array of numeric values, encapsulating it into a 2D array where the first and only
     * row contains the embedding vector. It is primarily used to prepare embedding data for processing or storage operations
     * that require a specific array format.
     *
     * @param embeddingsNode A JsonNode containing a single-dimensional array of embedding values.
     * @return A 2D double array where the first row contains the embedding vector from the JsonNode.
     * @throws IllegalArgumentException If the input JsonNode is not a single-dimensional array.
     */

    private static double [][] convertJsonNodeToEmbeddingsArray(JsonNode embeddingsNode) {
        double[][] embeddingsArray = new double[1][];
        JsonNode innerArrayNode = embeddingsNode;
        if (innerArrayNode.isArray()) {
            double[] innerArray = new double[innerArrayNode.size()];
            for (int j = 0; j < innerArrayNode.size(); j++) {
                JsonNode valueNode = innerArrayNode.get(j);
                innerArray[j] = valueNode.asDouble();
            }
            embeddingsArray[0] = innerArray;
        } else {
            throw new IllegalArgumentException("The provided JsonNode is not an array of arrays as expected.");
        }
        return embeddingsArray;
    }
}
