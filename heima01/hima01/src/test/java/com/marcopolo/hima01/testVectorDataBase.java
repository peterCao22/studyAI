package com.marcopolo.hima01;


import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class testVectorDataBase {

    @Autowired
    VectorStore vectorStore;


    @Test
    void testMilvus(){

        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

        // Add the documents to Milvus Vector Store
        vectorStore.add(documents);

        // Retrieve documents similar to a query
        List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        results.forEach(System.out::println);
    }

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void testEmbdingModel(){
        float[] hellos = embeddingModel.embed("hello");
        for (float hello : hellos) {
            System.out.println(hello);
        }
    }
}
