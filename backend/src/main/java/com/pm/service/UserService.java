package com.pm.service;

import com.pm.model.entity.Role;
import com.pm.model.entity.User;
import com.pm.repository.RoleRepository;
import com.pm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Handle Roles
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            user.getRoles().forEach(role -> {
                Role dbRole = roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(dbRole);
            });
            user.setRoles(roles);
        } else {
            // Default role
            Role userRole = roleRepository.findByName("team_member")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        user.setFullName(userDetails.getFullName());
        user.setPosition(userDetails.getPosition());
        user.setActive(userDetails.isActive());
        
        // Handle Role Updates if provided
        if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            userDetails.getRoles().forEach(role -> {
                Role dbRole = roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(dbRole);
            });
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }
}
