package com.smartcampus.service;

import com.smartcampus.dto.UserDto;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.User;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto createUser(UserDto dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email address is required");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BadRequestException("User name is required");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required for new users");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("User email already exists: " + dto.getEmail());
        }

        Role userRole = Role.STUDENT;
        if (dto.getRole() != null) {
            try {
                userRole = Role.valueOf(dto.getRole().toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException("Invalid user role: " + dto.getRole());
            }
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(userRole)
                .enabled(dto.getEnabled() != null ? dto.getEnabled() : true)
                .build();

        return mapToDto(userRepository.save(user));
    }

    public Page<UserDto> getUsers(String search, String role, Boolean enabled, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), searchPattern);
                predicates.add(cb.or(nameMatch, emailMatch));
            }

            if (role != null && !role.trim().isEmpty()) {
                try {
                    Role r = Role.valueOf(role.toUpperCase());
                    predicates.add(cb.equal(root.get("role"), r));
                } catch (Exception ignored) {}
            }

            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName().trim());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getRole() != null) {
            try {
                user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            } catch (Exception e) {
                throw new BadRequestException("Invalid user role: " + dto.getRole());
            }
        }
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }

        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public UserDto toggleUserStatus(Long id, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(enabled != null ? enabled : !user.isEnabled());
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
