package com.tappr.finance.tapprbackend.transaction.controllers;

import com.tappr.finance.tapprbackend.security.JwtUtil;
import com.tappr.finance.tapprbackend.user.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final UserService userService;
    private final JwtUtil jwtUtil;


}
