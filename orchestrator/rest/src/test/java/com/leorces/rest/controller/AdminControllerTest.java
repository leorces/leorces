package com.leorces.rest.controller;

import com.leorces.api.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    private AdminController subject;

    @BeforeEach
    void setUp() {
        subject = new AdminController(adminService);
    }

}