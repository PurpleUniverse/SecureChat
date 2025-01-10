package SecurityChat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SecureChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureChatApplication.class, args);
    }
}
