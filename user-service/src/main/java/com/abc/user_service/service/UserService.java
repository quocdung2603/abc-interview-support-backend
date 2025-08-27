package com.abc.user_service.service;

import com.abc.user_service.dto.request.*;
import com.abc.user_service.dto.response.UserResponse;
import com.abc.user_service.entity.*;
import com.abc.user_service.mapper.UserMapper;
import com.abc.user_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EloHistoryRepository eloHistoryRepository;
    private final UserMapper userMapper;

    public UserResponse create(UserRequest request) {
        User user = userMapper.toEntity(request);
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        user.setStatus(UserStatus.PENDING);
        user.setEloScore(0);
        user.setEloRank(EloRank.NEWBIE);
        user.setCreatedAt(LocalDateTime.now());
        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .filter(u -> u.getPassword().equals(request.getPassword()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        return userMapper.toResponse(user);
    }

    public UserResponse verify(VerifyRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
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
}