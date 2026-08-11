package com.learningsp.expensetracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(scanBasePackages = "com.learningsp")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.learningsp.repo")
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "com.learningsp.entity")
@EnableScheduling
public class ExpensetrackerApplication {

    /** Keys the app cannot start without. Everything else has a sensible default in application.yml. */
    private static final String[] REQUIRED_KEYS = {"JWT_SECRET", "DB_PASSWORD"};

    public static void main(String[] args) {
        loadEnvironmentVariables();
        SpringApplication.run(ExpensetrackerApplication.class, args);
    }

    private static void loadEnvironmentVariables() {
        String workingDir = System.getProperty("user.dir");
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Load environment variables from .env file (a real OS env var always wins over .env)
        dotenv.entries().forEach(entry -> {
            if (System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        // Fail loudly and clearly here instead of letting Spring surface a confusing
        // "Unsatisfied dependency" / "Could not resolve placeholder" stack trace later.
        List<String> missing = new ArrayList<>();
        for (String key : REQUIRED_KEYS) {
            boolean present = System.getenv(key) != null
                    || (System.getProperty(key) != null && !System.getProperty(key).isBlank());
            if (!present) missing.add(key);
        }

        if (!missing.isEmpty()) {
            System.err.println();
            System.err.println("=======================================================================");
            System.err.println(" STARTUP FAILED: missing required configuration: " + String.join(", ", missing));
            System.err.println("-----------------------------------------------------------------------");
            System.err.println(" Looked for a .env file in: " + workingDir);
            System.err.println(" That is the JVM's working directory, NOT necessarily your project");
            System.err.println(" folder -- it depends on how you launched the app (IDE run config,");
            System.err.println(" `java -jar`, terminal cwd, etc).");
            System.err.println();
            System.err.println(" To fix this:");
            System.err.println("   1. Make sure a file literally named '.env' sits next to pom.xml");
            System.err.println("      (i.e. inside: " + workingDir + ")");
            System.err.println("   2. Make sure it defines: " + String.join(", ", missing));
            System.err.println("   OR set these as real environment variables / IDE run-config");
            System.err.println("      environment variables instead of relying on the .env file.");
            System.err.println("=======================================================================");
            System.err.println();
            System.exit(1);
        }
    }
}
