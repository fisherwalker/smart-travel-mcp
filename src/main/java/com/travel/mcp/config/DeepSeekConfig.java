package com.travel.mcp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * RestTemplate 配置 — 自动检测 JVM 代理配置
 * 通过 -Dhttps.proxyHost / -Dhttps.proxyPort 设置代理
 */
@Slf4j
@Configuration
public class DeepSeekConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(120000);

        String proxyHost = System.getProperty("https.proxyHost");
        String proxyPort = System.getProperty("https.proxyPort");

        if (proxyHost != null && !proxyHost.isBlank() && proxyPort != null) {
            int port = Integer.parseInt(proxyPort);
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port)));
            log.info("🔌 RestTemplate 代理已配置: {}:{}", proxyHost, port);
        } else {
            log.info("🔌 RestTemplate 直连模式");
        }

        return new RestTemplate(factory);
    }
}
