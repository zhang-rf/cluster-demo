package me.rfprojects.clusterdemo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("server")
@Data
public class ServerProperties {

    private String address = "localhost";
    private int port = 8080;
}
