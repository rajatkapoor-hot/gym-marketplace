package com.gymnetwork.common.exception;

import com.gymnetwork.auth.security.JwtAuthenticationFilter;
import com.gymnetwork.config.SecurityConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ErrorResponseControllerTest.TestController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ErrorResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void passThroughJwtFilter() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser
    void validationErrorReturnsConsistent400Envelope() throws Exception {
        mockMvc.perform(post("/test/validation").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.status").value(400))
                .andExpect(jsonPath("$.errors.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors.path").value("/test/validation"))
                .andExpect(jsonPath("$.errors.details.name").value("name is required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unauthenticatedRequestReturnsConsistent401Envelope() throws Exception {
        mockMvc.perform(get("/test/authenticated"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.errors.status").value(401))
                .andExpect(jsonPath("$.errors.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.errors.path").value("/test/authenticated"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void unauthorizedRoleReturnsConsistent403Envelope() throws Exception {
        mockMvc.perform(get("/test/admin-only"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied: Insufficient permissions"))
                .andExpect(jsonPath("$.errors.status").value(403))
                .andExpect(jsonPath("$.errors.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.errors.path").value("/test/admin-only"));
    }

    @Test
    @WithMockUser
    void notFoundReturnsConsistent404Envelope() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Resource missing"))
                .andExpect(jsonPath("$.errors.status").value(404))
                .andExpect(jsonPath("$.errors.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errors.path").value("/test/not-found"));
    }

    @Test
    @WithMockUser
    void conflictReturnsConsistent409Envelope() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Business rule conflict"))
                .andExpect(jsonPath("$.errors.status").value(409))
                .andExpect(jsonPath("$.errors.code").value("CONFLICT"))
                .andExpect(jsonPath("$.errors.path").value("/test/conflict"));
    }

    @Test
    @WithMockUser
    void unexpectedErrorReturnsConsistent500Envelope() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.errors.status").value(500))
                .andExpect(jsonPath("$.errors.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.errors.path").value("/test/error"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @PostMapping("/validation")
        void validation(@Valid @RequestBody TestRequest request) {}

        @GetMapping("/authenticated")
        void authenticated() {}

        @GetMapping("/admin-only")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        void adminOnly() {}

        @GetMapping("/not-found")
        void notFound() { throw new ResourceNotFoundException("Resource missing"); }

        @GetMapping("/conflict")
        void conflict() { throw new ConflictException("Business rule conflict"); }

        @GetMapping("/error")
        void error() { throw new IllegalStateException("Boom"); }
    }

    record TestRequest(@NotBlank(message = "name is required") String name) {}
}
