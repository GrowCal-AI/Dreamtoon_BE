package com.dreamtoon.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Polar.sh 결제 연동 설정 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "polar")
public class PolarProperties {

    private String apiKey;
    private String webhookSecret;
    private String baseUrl = "https://api.polar.sh/v1";
    private boolean sandbox = false;
    private String successUrl;
    private String portalReturnUrl;

    private Products products = new Products();
    private Prices prices = new Prices();

    @Getter
    @Setter
    public static class Products {
        private String plus;
        private String pro;
        private String ultra;
    }

    @Getter
    @Setter
    public static class Prices {
        private String plus;
        private String pro;
        private String ultra;
    }
}
