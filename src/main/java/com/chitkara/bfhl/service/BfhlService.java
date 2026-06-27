package com.chitkara.bfhl.service;

import com.chitkara.bfhl.dto.BfhlRequest;
import com.chitkara.bfhl.dto.BfhlResponse;

/**
 * Service interface defining the contract for BFHL data processing.
 * Any implementation must provide logic to process the input array
 * and return the categorised response.
 */
public interface BfhlService {

    /**
     * Processes the incoming request, categorises the elements in the
     * data array, and builds the response DTO.
     *
     * @param request the validated request containing the input data array
     * @return a fully populated {@link BfhlResponse}
     */
    BfhlResponse process(BfhlRequest request);
}
