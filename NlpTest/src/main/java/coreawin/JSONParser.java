package main.java.coreawin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Iterator;

public class JSONParser {

    public static String checkNull(String src){
        if(src==null) return "";
        if("".equalsIgnoreCase(src)) return "";
        if("null".equalsIgnoreCase(src)) return "";
        return src.replaceAll("\t", "").trim();
    }
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        File dir = new File("g:\\022.민원(콜센터) 질의-응답 데이터\\01.데이터\\3.jsondata\\");

        String name = "D:\\data\\AI_DATAHUB\\conversion.tsv";
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(name)), "UTF-8"))) {
            // JSON 파일 읽기
            File jsonFile = new File("D:\\data\\AI_DATAHUB\\sample.json.json");

            // UTF-8로 읽기
            byte[] jsonData = java.nio.file.Files.readAllBytes(jsonFile.toPath());
            String jsonString = new String(jsonData, "EUC-KR");

            // JSON 데이터 파싱
//            JsonNode rootNode = objectMapper.readTree(jsonFile);
            JsonNode rootNode = objectMapper.readTree(jsonString);

            // 파싱된 데이터 사용 예시
//            String name = rootNode.toPrettyString();
            Iterator<JsonNode> iter = rootNode.iterator();
            StringBuilder buf = new StringBuilder();
            final String TAB = "\t";
            final String ENTER = "\n";
            buf.append("도메인");
            buf.append(TAB);
            buf.append("카테고리");
            buf.append(TAB);
            buf.append("카테고리");
            buf.append(TAB);
            buf.append("대화셋일련번호");
            buf.append(TAB);
            buf.append("화자");
            buf.append(TAB);
            buf.append("문장번호");
            buf.append(TAB);
            buf.append("고객의도");
            buf.append(TAB);
            buf.append("상담사의도");
            buf.append(TAB);
            buf.append("QA");
            buf.append(TAB);
            buf.append("고객질문");
            buf.append(TAB);
            buf.append("상담사질문");
            buf.append(TAB);
            buf.append("고객답변");
            buf.append(TAB);
            buf.append("상담사답변");
            buf.append(TAB);
            buf.append("개체명");
            buf.append(TAB);
            buf.append("용어사전");
            buf.append(TAB);
            buf.append("지식베이스");
            buf.append(ENTER);
            writer.write(buf.toString());
            while (iter.hasNext()) {
                JsonNode node = iter.next();
                buf.setLength(0);
                String 도메인 = checkNull(node.get("도메인").asText());
                String 카테고리 = checkNull(node.get("카테고리").asText());
                String 대화셋일련번호 = checkNull(node.get("대화셋일련번호").asText());
                String 화자 = checkNull(node.get("화자").asText());
                String 문장번호 = checkNull(node.get("문장번호").asText());
                String 고객의도 = checkNull(node.get("고객의도").asText());
                String 상담사의도 = checkNull(node.get("상담사의도").asText());
                String QA = checkNull(node.get("QA").asText());
                String 고객질문 = checkNull(node.get("고객질문(요청)").asText());
                String 상담사질문 = checkNull(node.get("상담사질문(요청)").asText());
                String 고객답변 = checkNull(node.get("고객답변").asText());
                String 상담사답변 = checkNull(node.get("상담사답변").asText());
                String 개체명 = checkNull(node.get("개체명 ").asText());
                String 용어사전 = checkNull(node.get("용어사전").asText());
                String 지식베이스 = checkNull(node.get("지식베이스").asText());

                buf.append(도메인);
                buf.append(TAB);
                buf.append(카테고리);
                buf.append(TAB);
                buf.append(카테고리);
                buf.append(TAB);
                buf.append(대화셋일련번호);
                buf.append(TAB);
                buf.append(화자);
                buf.append(TAB);
                buf.append(문장번호);
                buf.append(TAB);
                buf.append(고객의도);
                buf.append(TAB);
                buf.append(상담사의도);
                buf.append(TAB);
                buf.append(QA);
                buf.append(TAB);
                buf.append(고객질문);
                buf.append(TAB);
                buf.append(상담사질문);
                buf.append(TAB);
                buf.append(고객답변);
                buf.append(TAB);
                buf.append(상담사답변);
                buf.append(TAB);
                buf.append(개체명);
                buf.append(TAB);
                buf.append(용어사전);
                buf.append(TAB);
                buf.append(지식베이스);
                buf.append(ENTER);
                writer.write(buf.toString());
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}