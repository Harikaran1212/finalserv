package com.example.finalserv;

import com.example.finalserv.dto.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        // STEP 1 — Generate Webhook
        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        WebhookRequest request = new WebhookRequest(
                "Harikaran",
                "12",
                "Harikaranrk2003@gmail.com"
        );

        ResponseEntity<WebhookResponse> response =
                restTemplate.postForEntity(generateUrl, request, WebhookResponse.class);

        WebhookResponse data = response.getBody();

        System.out.println("Webhook URL: " + data.getWebhook());
        System.out.println("Token: " + data.getAccessToken());


        // STEP 2 — PUT YOUR FINAL SQL QUERY HERE
        String sqlQuery = "SELECT * FROM table_name;";


        // STEP 3 — Submit Answer
        String submitUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", data.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        FinalQueryRequest finalQuery = new FinalQueryRequest(sqlQuery);

        HttpEntity<FinalQueryRequest> entity = new HttpEntity<>(finalQuery, headers);

        ResponseEntity<String> submitResponse =
                restTemplate.postForEntity(submitUrl, entity, String.class);

        System.out.println("Submission Response: " + submitResponse.getBody());
    }
}
