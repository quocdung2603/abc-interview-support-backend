package com.auth.service.client;


import com.auth.service.dto.LoginRequest;
import com.auth.service.dto.RegisterRequest;
import com.auth.service.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PostMapping("/users/login")
    UserDto login(@RequestBody LoginRequest request);
    @PostMapping("/users")
    UserDto create(@RequestBody RegisterRequest request);

    @GetMapping("/users/verify")
    UserDto verify(@RequestParam("token") String token);
}