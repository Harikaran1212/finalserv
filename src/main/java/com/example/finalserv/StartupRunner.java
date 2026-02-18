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
                "REG12312",
                "Harikaranrk2003@gmail.com"
        );

        ResponseEntity<WebhookResponse> response =
                restTemplate.postForEntity(generateUrl, request, WebhookResponse.class);

        WebhookResponse data = response.getBody();

        System.out.println("Webhook URL: " + data.getWebhook());
        System.out.println("Token: " + data.getAccessToken());


        String regNo = request.getRegNo();

// extract last 2 digits
        int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length()-2));

        System.out.println("Last 2 digits: " + lastTwoDigits);

        String sqlQuery;

        if (lastTwoDigits % 2 == 0) {

            System.out.println("EVEN → Executing Question 2");

            // QUESTION 2
            sqlQuery = "SELECT d.DEPARTMENT_NAME, AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURDATE())) AS AVERAGE_AGE, SUBSTRING_INDEX(GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) ORDER BY e.EMP_ID SEPARATOR ', '), ', ', 10) AS EMPLOYEE_LIST FROM DEPARTMENT d JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID WHERE p.AMOUNT > 70000 GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME ORDER BY d.DEPARTMENT_ID DESC;";

        } else {

            System.out.println("ODD → Executing Question 1");

            // QUESTION 1
            sqlQuery = "WITH salary_data AS ( SELECT d.DEPARTMENT_ID, d.DEPARTMENT_NAME, e.EMP_ID, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, SUM(p.AMOUNT) AS SALARY, ROW_NUMBER() OVER ( PARTITION BY d.DEPARTMENT_ID ORDER BY SUM(p.AMOUNT) DESC ) AS rn FROM DEPARTMENT d JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID WHERE DAY(p.PAYMENT_TIME) <> 1 GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME, e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB ) SELECT DEPARTMENT_NAME, SALARY, EMPLOYEE_NAME, AGE FROM salary_data WHERE rn = 1;";
        }

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
