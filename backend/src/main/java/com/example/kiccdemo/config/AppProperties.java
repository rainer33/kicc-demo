package com.example.kiccdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String frontendUrl;
    private Payment payment = new Payment();
    private Security security = new Security();

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class Payment {
        private Kicc kicc = new Kicc();

        public Kicc getKicc() {
            return kicc;
        }

        public void setKicc(Kicc kicc) {
            this.kicc = kicc;
        }
    }

    public static class Kicc {
        private String merchantId;
        private String merchantKey;
        private String payUrl;
        private boolean useMockApprove;
        private boolean callbackSignatureRequired;

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getMerchantKey() {
            return merchantKey;
        }

        public void setMerchantKey(String merchantKey) {
            this.merchantKey = merchantKey;
        }

        public String getPayUrl() {
            return payUrl;
        }

        public void setPayUrl(String payUrl) {
            this.payUrl = payUrl;
        }

        public boolean isUseMockApprove() {
            return useMockApprove;
        }

        public void setUseMockApprove(boolean useMockApprove) {
            this.useMockApprove = useMockApprove;
        }

        public boolean isCallbackSignatureRequired() {
            return callbackSignatureRequired;
        }

        public void setCallbackSignatureRequired(boolean callbackSignatureRequired) {
            this.callbackSignatureRequired = callbackSignatureRequired;
        }
    }

    public static class Security {
        private String adminToken;

        public String getAdminToken() {
            return adminToken;
        }

        public void setAdminToken(String adminToken) {
            this.adminToken = adminToken;
        }
    }
}
