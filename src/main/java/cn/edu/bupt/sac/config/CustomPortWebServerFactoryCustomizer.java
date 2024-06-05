package cn.edu.bupt.sac.config;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.stereotype.Component;
import java.net.ServerSocket;

@Component
public class CustomPortWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        int port = 8081;
        while (isPortInUse(port)) {
            port++;
        }
        factory.setPort(port);
    }

    private static boolean isPortInUse(int port) {
        try {
            new ServerSocket(port).close();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
