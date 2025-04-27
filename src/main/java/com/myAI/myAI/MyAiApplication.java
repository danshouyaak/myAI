package com.myAI.myAI;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

//@CrossOrigin(origins = "http://192.168.43.1:5173/", allowCredentials = "true")
@MapperScan("com.myAI.myAI.mapper")
@SpringBootApplication(scanBasePackages = "com.myAI.myAI")
@EnableScheduling
public class MyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyAiApplication.class, args);
    }


    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(QwenEmbeddingModel qwenEmbeddingModel,
                                                       EmbeddingStore embeddingStore) throws URISyntaxException {

        return args -> {
            Document document = ClassPathDocumentLoader.loadDocument("rag/math.txt", new TextDocumentParser());

            DocumentByLineSplitter splitter = new DocumentByLineSplitter(
                    500,
                    200
            );
            List<TextSegment> segments = splitter.split(document);

            // 向量化
            List<Embedding> embeddings = qwenEmbeddingModel.embedAll(segments).content();

            // 存入
            embeddingStore.addAll(embeddings,segments);

        };
    }
}
