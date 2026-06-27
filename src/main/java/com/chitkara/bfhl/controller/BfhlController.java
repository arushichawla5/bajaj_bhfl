package com.chitkara.bfhl.controller;

import com.chitkara.bfhl.dto.BfhlRequest;
import com.chitkara.bfhl.dto.BfhlResponse;
import com.chitkara.bfhl.service.BfhlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing the /bfhl endpoint.
 *
 * POST /bfhl – processes the input data array and returns categorised results.
 * GET  /health – lightweight health-check used by hosting providers.
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BfhlController {

    private final BfhlService bfhlService;

    /**
     * Main endpoint.
     * Accepts a JSON body with a "data" array and returns the processed response.
     *
     * @param request validated request body
     * @return 200 OK with {@link BfhlResponse}
     */
    @PostMapping("/bfhl")
    public ResponseEntity<BfhlResponse> handlePost(@Valid @RequestBody BfhlRequest request) {
        BfhlResponse response = bfhlService.process(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Simple health-check endpoint.
     * Returns 200 OK so hosting platforms (Railway / Render) know the app is alive.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
