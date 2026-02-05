package com.zxbs.logstats.config;

import com.zxbs.logstats.entity.User;
import com.zxbs.logstats.entity.UserRole;
import com.zxbs.logstats.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder encoder, AppProperties properties) {
        return args -> {
            if (!userRepository.existsByUsername(properties.getDefaultUsers().getAdminUsername())) {
                User admin = new User();
                admin.setUsername(properties.getDefaultUsers().getAdminUsername());
                admin.setPasswordHash(encoder.encode(properties.getDefaultUsers().getAdminPassword()));
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
            }
            if (!userRepository.existsByUsername(properties.getDefaultUsers().getUserUsername())) {
                User user = new User();
                user.setUsername(properties.getDefaultUsers().getUserUsername());
                user.setPasswordHash(encoder.encode(properties.getDefaultUsers().getUserPassword()));
                user.setRole(UserRole.USER);
                userRepository.save(user);
            }
        };
    }
}
