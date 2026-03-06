package com.example.kiccdemo;

import com.example.kiccdemo.entity.Payment;
import com.example.kiccdemo.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentSecurityAndFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void callbackRequiresSignature() throws Exception {
        String orderId = createReadyOrder(null, "sig-check-1");

        mockMvc.perform(post("/api/payments/kicc/callback")
                        .param("oid", orderId)
                        .param("resCd", "0000")
                        .param("tXid", "TX-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminApiRequiresValidToken() throws Exception {
        mockMvc.perform(get("/api/admin/payments")
                        .header("X-Admin-Token", "wrong-token"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/payments")
                        .header("X-Admin-Token", "test-admin-token-1234"))
                .andExpect(status().isOk());
    }

    @Test
    void callbackDoesNotOverrideCanceledPayment() throws Exception {
        String orderId = createReadyOrder(null, "cancel-safe-1");

        mockMvc.perform(post("/api/payments/{orderId}/mock-approve", orderId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/payments/{orderId}/mock-cancel", orderId))
                .andExpect(status().isOk());

        String signature = sign(orderId + "|0000|TX-LATE", "TEST_KEY");

        mockMvc.perform(post("/api/payments/kicc/callback")
                        .param("oid", orderId)
                        .param("resCd", "0000")
                        .param("tXid", "TX-LATE")
                        .header("X-KICC-SIGNATURE", signature))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(payment.getStatus().name()).isEqualTo("CANCELED");
    }

    @Test
    void idempotencyKeyReturnsSameOrderForSameRequest() throws Exception {
        String first = mockMvc.perform(post("/api/payments/ready")
                        .header("Idempotency-Key", "idem-test-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderName\":\"idem-order\",\"buyerName\":\"user\",\"amount\":1000}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String second = mockMvc.perform(post("/api/payments/ready")
                        .header("Idempotency-Key", "idem-test-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderName\":\"idem-order\",\"buyerName\":\"user\",\"amount\":1000}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String firstOrderId = objectMapper.readTree(first).get("orderId").asText();
        String secondOrderId = objectMapper.readTree(second).get("orderId").asText();

        assertThat(secondOrderId).isEqualTo(firstOrderId);
    }

    private String createReadyOrder(String idemKey, String suffix) throws Exception {
        var request = post("/api/payments/ready")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderName\":\"order-" + suffix + "\",\"buyerName\":\"buyer-" + suffix + "\",\"amount\":1000}");

        if (idemKey != null) {
            request.header("Idempotency-Key", idemKey);
        }

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("orderId").asText();
    }

    private String sign(String payload, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
