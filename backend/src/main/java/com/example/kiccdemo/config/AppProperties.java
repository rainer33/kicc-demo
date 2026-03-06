package com.example.kiccdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
/**
 * application.yml 값을 타입 안전하게 바인딩하는 설정 객체입니다. 결제/보안 옵션을 한곳에서 관리합니다.
 */
public class AppProperties {

    private Redis redis = new Redis();
    private String frontendUrl;
    private Payment payment = new Payment();
    private Security security = new Security();

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

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

    public static class Redis {
        private String host;
        private int port;
        private String username;
        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Security {
        private String adminToken;
        private boolean allowGeneratedAdminToken;

        public String getAdminToken() {
            return adminToken;
        }

        public void setAdminToken(String adminToken) {
            this.adminToken = adminToken;
        }

        public boolean isAllowGeneratedAdminToken() {
            return allowGeneratedAdminToken;
        }

        public void setAllowGeneratedAdminToken(boolean allowGeneratedAdminToken) {
            this.allowGeneratedAdminToken = allowGeneratedAdminToken;
        }
    }
}
