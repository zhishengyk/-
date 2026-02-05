package com.zxbs.logstats.service;

import com.zxbs.logstats.dto.UserRequest;
import com.zxbs.logstats.entity.User;
import com.zxbs.logstats.entity.UserRole;
import com.zxbs.logstats.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    public User create(UserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.valueOf(req.getRole().toUpperCase()));
        user.setEnabled(req.isEnabled());
        return userRepository.save(user);
    }

    public User update(Long id, UserRequest req) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUsername(req.getUsername());
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        user.setRole(UserRole.valueOf(req.getRole().toUpperCase()));
        user.setEnabled(req.isEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
