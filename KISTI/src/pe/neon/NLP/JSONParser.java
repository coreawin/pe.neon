package pe.neon.NLP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JSONParser {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // JSON 파일 읽기
            File jsonFile = new File("path/to/your/file.json");
            // JSON 데이터 파싱
            JsonNode rootNode = objectMapper.readTree(jsonFile);

            // 파싱된 데이터 사용 예시
            String name = rootNode.get("name").asText();
            int age = rootNode.get("age").asInt();
            JsonNode hobbiesNode = rootNode.get("hobbies");
            // ...

            System.out.println("Name: " + name);
            System.out.println("Age: " + age);
            System.out.println("Hobbies: " + hobbiesNode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}