package com.example.mhpractice.features.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.mhpractice.features.user.repository.UserRepository;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest // ← Load FULL Spring context
@AutoConfigureMockMvc // ← Create MockMvc for HTTP requests
@Transactional // ← Rollback after each test
class AuthControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void success_register_new_email() throws Exception {
        // ARRANGE: Prepare request
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        // ACT: Make request
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // ASSERT: Check response
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }
}
