package com.jptechgenius.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class PayrollApplication {

    public static void main(String[] args) {
        // Application context
        ConfigurableApplicationContext context = SpringApplication.run(PayrollApplication.class, args);

        // Environment থেকে ডাটা নেওয়া
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "900");
        String appName = env.getProperty("app.name", "Payroll System");
        String teamName = env.getProperty("app.team", "Axiom Devs");
        String version = env.getProperty("app.version", "1.0.0");

        String url = "http://localhost:" + port;

        // কনসোলে ফিক্সড সাইজের বক্স প্রিন্ট করা
        System.out.println("\n+==========================================================+");
        System.out.printf("| %-56s |%n", appName.toUpperCase() + " (" + version + ")");
        System.out.printf("| %-56s |%n", "Powered by: " + teamName);
        System.out.println("|----------------------------------------------------------|");
        System.out.printf("| %-56s |%n", "✅ Application is running successfully!");
        System.out.printf("| %-56s |%n", "🔗 Dashboard: " + url);
        System.out.println("+==========================================================+\n");
    }
}