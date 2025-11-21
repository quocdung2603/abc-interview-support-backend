package com.abc.user_service.service;

import com.abc.user_service.dto.request.*;
import com.abc.user_service.dto.response.EloHistoryResponse;
import com.abc.user_service.dto.response.UserResponse;
import com.abc.user_service.entity.*;
import com.abc.user_service.mapper.UserMapper;
import com.abc.user_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EloHistoryRepository eloHistoryRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(UserRequest request) {
        // Create user manually to avoid mapper issues
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setFullName(request.getFullName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setIsStudying(request.getIsStudying());
        
        Role role;
        if (request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
        } else if (request.getRoleName() != null) {
            RoleName roleName;
            try {
                roleName = RoleName.valueOf(request.getRoleName().toUpperCase());
            } catch (Exception e) {
                throw new RuntimeException("Invalid role name");
            }
            role = roleRepository.findAll().stream()
                    .filter(r -> r.getRoleName() == roleName)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Role not found"));
        } else {
            throw new RuntimeException("Role is required");
        }
        user.setRole(role);
        user.setStatus(UserStatus.PENDING);
        user.setEloScore(0);
        user.setEloRank(EloRank.NEWBIE);
        user.setVerifyToken(java.util.UUID.randomUUID().toString());
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        emailService.sendVerificationEmailHtml(saved.getEmail(), saved.getVerifyToken());
        return userMapper.toResponse(saved);
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.abc.user_service.exception.ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse verify(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing token");
        }

        User user = userRepository.findByVerifyToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token"));

        if (user.getStatus() == UserStatus.VERIFIED) {
            // có thể trả luôn trạng thái đã xác minh
            return userMapper.toResponse(user);
        }

        user.setVerifyToken(null);
        user.setStatus(UserStatus.VERIFIED);
        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse updateRole(Long userId, RoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse updateStatus(Long userId, StatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(request.getStatus());
        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse applyElo(EloApplyRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        int score = user.getEloScore() == null ? 0 : user.getEloScore();
        score += request.getPoints();
        user.setEloScore(score);
        user.setEloRank(calculateRank(score));

        EloHistory history = new EloHistory();
        history.setUser(user);
        history.setAction(request.getAction());
        history.setPoints(request.getPoints());
        history.setDescription(request.getDescription());
        history.setCreatedAt(LocalDateTime.now());
        eloHistoryRepository.save(history);

        return userMapper.toResponse(userRepository.save(user));
    }

    private EloRank calculateRank(int score) {
        if (score < 100) return EloRank.NEWBIE;
        if (score < 200) return EloRank.LEARNER;
        if (score < 400) return EloRank.CONTRIBUTOR;
        if (score < 700) return EloRank.SOLVER;
        if (score < 1100) return EloRank.EXPERT;
        if (score < 1600) return EloRank.SENIOR_EXPERT;
        if (score < 2100) return EloRank.MASTER;
        return EloRank.LEGEND;
    }

    // Additional CRUD methods
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(request.getFullName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setIsStudying(request.getIsStudying());
        return userMapper.toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    public Page<UserResponse> getUsersByRole(Long roleId, Pageable pageable) {
        return userRepository.findByRoleId(roleId, pageable).map(userMapper::toResponse);
    }

    public Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable).map(userMapper::toResponse);
    }

    // Internal methods for Auth Service
    public Boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserResponse getByEmail(String email) {
        log.info("=== UserService.getByEmail called with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElse(null);
        log.info("=== User found: {}", user != null);
        if (user != null) {
            log.info("=== User details - id: {}, email: {}, roleId: {}", user.getId(), user.getEmail(), user.getRole() != null ? user.getRole().getId() : null);
        }
        UserResponse response = user != null ? userMapper.toResponse(user) : null;
        log.info("=== UserResponse created: {}", response != null);
        if (response != null) {
            log.info("=== Response details - id: {}, email: {}, roleId: {}", response.getId(), response.getEmail(), response.getRoleId());
        }
        return response;
    }

    public Boolean validatePassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) return false;
        return passwordEncoder.matches(password, user.getPassword());
    }

    public UserResponse verifyToken(String token) {
        User user = userRepository.findByVerifyToken(token)
                .orElse(null);
        if (user == null) return null;
        
        // Update status to ACTIVE
        user.setStatus(UserStatus.ACTIVE);
        user.setVerifyToken(null);
        userRepository.save(user);
        
        return userMapper.toResponse(user);
    }

    public java.util.List<com.abc.user_service.dto.response.RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(role -> com.abc.user_service.dto.response.RoleResponse.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .description(role.getDescription())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    // Elo History methods
    public Page<EloHistoryResponse> getEloHistory(Long userId, Pageable pageable) {
        return eloHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(userMapper::toEloHistoryResponse);
    }

    public List<EloHistoryResponse> getRecentEloHistory(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        List<EloHistory> histories = limit == 10 
            ? eloHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
            : eloHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        return histories.stream()
                .map(userMapper::toEloHistoryResponse)
                .collect(Collectors.toList());
    }

    public List<EloHistoryResponse> getAllEloHistory(Long userId) {
        return eloHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(userMapper::toEloHistoryResponse)
                .collect(Collectors.toList());
    }
}