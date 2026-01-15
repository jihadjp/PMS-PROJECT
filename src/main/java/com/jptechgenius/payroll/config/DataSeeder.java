package com.jptechgenius.payroll.config;

import com.jptechgenius.payroll.model.Designation;
import com.jptechgenius.payroll.model.User;
import com.jptechgenius.payroll.repository.DesignationRepository;
import com.jptechgenius.payroll.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

/**
 * [DataSeeder Class]
 * -------------------
 * Ei class tar kaj holo Application jokhon prothombar run hobe, tokhon Database e
 * kichu "Default Data" (jemon: Designation list, Admin user) automatic save kora.
 * * Ete kore bar bar manual vabe database e data dhukate hoy na.
 * @Configuration annotation ti Spring ke bole dey je eta ekta config class.
 */

@Configuration
public class DataSeeder {

    /**
     * @Bean annotation thakar karone Spring Boot start howar sathe sathei ei method ta run kore.
     * Ekhane amra Repository gula (Designation, User) and PasswordEncoder ke Inject korechi.
     */
    @Bean
    CommandLineRunner initDatabase(DesignationRepository designationRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {

            // ==========================================
            // 1. Default Designations Setup
            // ==========================================
            // Check korchi database e kono designation ache kina.
            // Jodi count 0 hoy (mane table khali), sudhu tokhon e nicher data gula insert hobe.
            // Eita duplicate data entry thekanor jonno kora hoyeche.
            if (designationRepository.count() == 0) {

                // Kichu common job title create kora holo
                Designation d1 = new Designation(); d1.setTitle("Intern"); d1.setFixedSalary(20000.0);
                Designation d2 = new Designation(); d2.setTitle("Junior Developer"); d2.setFixedSalary(35000.0);
                Designation d3 = new Designation(); d3.setTitle("Software Engineer"); d3.setFixedSalary(60000.0);
                Designation d4 = new Designation(); d4.setTitle("Senior Engineer"); d4.setFixedSalary(90000.0);
                Designation d5 = new Designation(); d5.setTitle("HR Manager"); d5.setFixedSalary(70000.0);
                Designation d6 = new Designation(); d6.setTitle("Project Manager"); d6.setFixedSalary(120000.0);

                // saveAll() method ekbare sob gula list akare save kore dey
                designationRepository.saveAll(Arrays.asList(d1, d2, d3, d4, d5, d6));

                System.out.println("✅ Default Designations Added Successfully!");
            }

            // ==========================================
            // 2. Default Admin Users Setup
            // ==========================================
            // Check korchi User table khali kina. Khali thakle Admin create korbo.
            if (userRepository.count() == 0) {

                // --- Admin User Create ---
                User admin = new User();
                admin.setUsername("admin");
                // Password ta plain text e na rekhe 'BCrypt' diye encrypt kora hocche.
                // Database e "1234" er bodole ekta encrypted string thakbe.
                admin.setPassword(passwordEncoder.encode("1234"));
                admin.setRole("ADMIN"); // Role set kora holo
                admin.setFullName("System Admin");
                admin.setEmail("admin@axiomdevs.com");

                userRepository.save(admin); // Database e save

                // --- Super Admin User Create ---
                User superAdmin = new User();
                superAdmin.setUsername("superadmin");
                superAdmin.setPassword(passwordEncoder.encode("1234"));
                superAdmin.setRole("SUPER_ADMIN"); // Highest permission role
                superAdmin.setFullName("Super Administrator");
                superAdmin.setEmail("superadmin@axiomdevs.com");

                userRepository.save(superAdmin); // Database e save

                // Console e log print kora jate developer bujhte pare kaj hoyeche
                System.out.println("✅ Security Users Created:");
                System.out.println("   👉 User: admin | Pass: 1234 | Email: admin@axiomdevs.com");
                System.out.println("   👉 User: superadmin | Pass: 1234 | Email: superadmin@axiomdevs.com");
            }
        };
    }
}