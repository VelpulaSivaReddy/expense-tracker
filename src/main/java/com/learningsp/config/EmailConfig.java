package com.learningsp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mail")
@Data
public class EmailConfig {
    private String from;
    private String fromName;
}
