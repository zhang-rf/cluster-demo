package me.rfprojects.clusterdemo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("cluster")
@Data
public class ClusterProperties {

    private List<String> hosts;
    private int electionIntervalMs = 200;
    private int heartbeatIntervalMs;
}
