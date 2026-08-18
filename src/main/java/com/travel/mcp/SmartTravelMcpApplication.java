package com.travel.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * 智慧旅游AI助手 - 启动类
 */
@SpringBootApplication
public class SmartTravelMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTravelMcpApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:8080";
        try {
            // Windows: cmd /c start 最可靠
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
            System.out.println("🌐 浏览器已自动打开: " + url);
        } catch (Exception e1) {
            try {
                // 备用方案: rundll32
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                System.out.println("🌐 浏览器已自动打开: " + url);
            } catch (Exception e2) {
                System.out.println("💡 请手动打开浏览器访问: " + url);
            }
        }
    }
}
