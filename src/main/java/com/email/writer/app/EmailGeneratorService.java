//package com.email.writer.app;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.Map;
//
//@Service
//
//public class EmailGeneratorService {
//
//    private final WebClient webClient;
//
//    @Value("${gemini.api.url}")
//    private String geminiApiUrl;
//
//    @Value("${gemini.api.key}")
//    private String geminiApiKey;
//
//    //this is cunstructor of WebClient
//    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder.build();
//    }
//
//    public String generateEmailReply(EmailRequest emailRequest){
//        //this will build the prompt
//        String prompt = buildprompt(emailRequest);
//        //craft the request
//        Map<String, Object> requestBody = Map.of(
//                "contents", new Object[]{
//                        Map.of("parts",new Object[]{
//                                Map.of("text",prompt)
//                })
//        }
//
//        );
//
//        //do request and get response
//        String response =webClient.post()
//                .uri(geminiApiUrl + geminiApiKey)
//                .header("Content-Type","application/json")
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        //Extract response and return response
//        return extractResponseContent(response);
//    }
//
//    private String extractResponseContent(String response) {
//         try{
//             ObjectMapper mapper=new ObjectMapper();
//             JsonNode rootNode=mapper.readTree(response);
//             return rootNode.path("candidates")
//                     .get(0)
//                     .path("content")
//                     .path("parts")
//                     .get(0)
//                     .path("text")
//                     .asText();
//
//         }catch(Exception e){
//             return "Error processing request :"+ e.getMessage();
//         }
//    }
//
//    private String buildprompt(EmailRequest emailRequest) {
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line ");
//        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
//            prompt.append("use a ").append(emailRequest.getTone()).append(" tone");
//        }
//        prompt.append("\n Original email : \n ").append(emailRequest.getEmailContent());
//        return prompt.toString();
//    }
//}
package com.email.writer.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);

        // Construct request body
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            // Construct full URI with query param for key
            String fullUrl = UriComponentsBuilder
                    .fromHttpUrl(geminiApiUrl)
                    .queryParam("key", geminiApiKey)
                    .toUriString();

            // Send request and get response
            String response = webClient.post()
                    .uri(fullUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractResponseContent(response);
        } catch (Exception e) {
            return "Request failed: " + e.getMessage();
        }
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error processing response: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line.");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append(" Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("\nOriginal email:\n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
