package com.prophet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Swarit Joshipura
 *
 * This class preprocesses data and prepares it for vectorization.
 */
public class ProcessText {

    /**
     * Cleans the provided text by removing stop words and then chunks it into segments of up to 1000 characters.
     * This method is a high-level utility for preparing text data, making it more suitable for text analysis or
     * machine learning tasks by reducing noise and managing size constraints.
     *
     * @param text The text to be cleaned and chunked.
     * @return A list of cleaned and chunked text segments, each up to 1000 characters in length.
     *         The cleaning process removes common stop words, and the resulting cleaned text is then
     *         divided into chunks.
     */
    public static List<String> cleanData(String text) {
        return chunkText(removeStopWords(text), 1000);
    }
    public static String removeStopWords(String text) {
        List<String> stopWords = Arrays.asList("the", "and", "of", "a", "to", "in", "is", "that", "it", "was", ",", ".");

        // Split the text into words
        String[] words = text.split("\\s+");

        // Remove stop words
        StringBuilder filteredText = new StringBuilder();
        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                filteredText.append(word).append(" ");
            }
        }
        return filteredText.toString().trim();
    }

    /**
     * Divides a given string into smaller chunks of a specified size. This utility method is useful for processing
     * large texts by breaking them down into manageable pieces, such as when preparing data for machine learning
     * models that have input size limitations. The context window in our case does not permit more than 8193 tokens.
     *
     * @param text The text to be chunked.
     * @param chunkSize The maximum size of each chunk.
     * @return A list of string chunks, each of which is up to chunkSize characters long. The last chunk may be shorter
     *         if the text does not divide evenly by chunkSize.
     */

    private static List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, length);
            chunks.add(text.substring(i, endIndex));
        }
        return chunks;
    }

}
