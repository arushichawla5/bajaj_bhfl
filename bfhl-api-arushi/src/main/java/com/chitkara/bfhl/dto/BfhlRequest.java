package com.chitkara.bfhl.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for the /bfhl endpoint.
 * Accepts a JSON array of strings under the key "data".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BfhlRequest {

    @NotNull(message = "Input 'data' array must not be null")
    private List<String> data;
}
