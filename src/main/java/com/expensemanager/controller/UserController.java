package com.expensemanager.controller;

import com.expensemanager.dto.UserViewDto;
import com.expensemanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserViewDto> getCurrentUserProfile() {
        UserViewDto userProfile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(userProfile);
    }
}
