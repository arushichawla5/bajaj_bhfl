package com.chitkara.bfhl;

import com.chitkara.bfhl.dto.BfhlRequest;
import com.chitkara.bfhl.dto.BfhlResponse;
import com.chitkara.bfhl.service.BfhlServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for the BFHL API.
 *
 * Covers:
 *  - Unit tests for the service layer (Examples A, B, C from the spec)
 *  - Integration tests via MockMvc (happy path + error scenarios)
 *  - Health-check endpoint
 */
@SpringBootTest
@AutoConfigureMockMvc
class BfhlApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BfhlServiceImpl bfhlService;

    // Fix identity fields so tests are deterministic regardless of application.properties
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bfhlService, "fullName",   "john_doe");
        ReflectionTestUtils.setField(bfhlService, "dob",        "17091999");
        ReflectionTestUtils.setField(bfhlService, "email",      "john@xyz.com");
        ReflectionTestUtils.setField(bfhlService, "rollNumber", "ABCD123");
    }

    // ── Service-layer unit tests ──────────────────────────────────────────────

    @Test
    @DisplayName("Example A: mixed numbers, letters and special chars")
    void exampleA_service() {
        BfhlRequest req = new BfhlRequest(List.of("a", "1", "334", "4", "R", "$"));
        BfhlResponse res = bfhlService.process(req);

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getUserId()).isEqualTo("john_doe_17091999");
        assertThat(res.getEmail()).isEqualTo("john@xyz.com");
        assertThat(res.getRollNumber()).isEqualTo("ABCD123");

        assertThat(res.getOddNumbers()).containsExactly("1");
        assertThat(res.getEvenNumbers()).containsExactlyInAnyOrder("334", "4");
        assertThat(res.getAlphabets()).containsExactlyInAnyOrder("A", "R");
        assertThat(res.getSpecialCharacters()).containsExactly("$");

        assertThat(res.getSum()).isEqualTo("339");
        assertThat(res.getConcatString()).isEqualTo("Ra");
    }

    @Test
    @DisplayName("Example B: more numbers, multiple specials")
    void exampleB_service() {
        BfhlRequest req = new BfhlRequest(
                List.of("2", "a", "y", "4", "&", "-", "*", "5", "92", "b"));
        BfhlResponse res = bfhlService.process(req);

        assertThat(res.getOddNumbers()).containsExactly("5");
        assertThat(res.getEvenNumbers()).containsExactlyInAnyOrder("2", "4", "92");
        assertThat(res.getAlphabets()).containsExactlyInAnyOrder("A", "Y", "B");
        assertThat(res.getSpecialCharacters()).containsExactlyInAnyOrder("&", "-", "*");
        assertThat(res.getSum()).isEqualTo("103");
        assertThat(res.getConcatString()).isEqualTo("ByA");
    }

    @Test
    @DisplayName("Example C: only multi-char alphabetic tokens, no numbers")
    void exampleC_service() {
        BfhlRequest req = new BfhlRequest(List.of("A", "ABCD", "DOE"));
        BfhlResponse res = bfhlService.process(req);

        assertThat(res.getOddNumbers()).isEmpty();
        assertThat(res.getEvenNumbers()).isEmpty();
        assertThat(res.getAlphabets()).containsExactly("A", "ABCD", "DOE");
        assertThat(res.getSpecialCharacters()).isEmpty();
        assertThat(res.getSum()).isEqualTo("0");
        // A, A,B,C,D, D,O,E → all chars: A,A,B,C,D,D,O,E
        // reversed: E,O,D,D,C,B,A,A → alternating: E,o,D,d,C,b,A,a → "EoDdCbAa"
        assertThat(res.getConcatString()).isEqualTo("EoDdCbAa");
    }

    @Test
    @DisplayName("Empty data array returns zeroed response without error")
    void emptyData_service() {
        BfhlRequest req = new BfhlRequest(List.of());
        BfhlResponse res = bfhlService.process(req);

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getOddNumbers()).isEmpty();
        assertThat(res.getEvenNumbers()).isEmpty();
        assertThat(res.getAlphabets()).isEmpty();
        assertThat(res.getSpecialCharacters()).isEmpty();
        assertThat(res.getSum()).isEqualTo("0");
        assertThat(res.getConcatString()).isEmpty();
    }

    @Test
    @DisplayName("userId format is lowercase full_name_ddmmyyyy")
    void userId_format() {
        BfhlRequest req = new BfhlRequest(List.of());
        BfhlResponse res = bfhlService.process(req);
        assertThat(res.getUserId()).matches("[a-z_]+_\\d{8}");
    }

    // ── Integration tests (MockMvc) ───────────────────────────────────────────

    @Test
    @DisplayName("POST /bfhl returns 200 with correct body for Example A")
    void postBfhl_exampleA_integration() throws Exception {
        String body = """
                {"data": ["a", "1", "334", "4", "R", "$"]}
                """;

        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_success").value(true))
                .andExpect(jsonPath("$.user_id").value("john_doe_17091999"))
                .andExpect(jsonPath("$.sum").value("339"))
                .andExpect(jsonPath("$.concat_string").value("Ra"));
    }

    @Test
    @DisplayName("POST /bfhl returns 400 when 'data' field is missing")
    void postBfhl_missingData_returns400() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false));
    }

    @Test
    @DisplayName("POST /bfhl returns 400 for malformed JSON")
    void postBfhl_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false));
    }

    @Test
    @DisplayName("GET /health returns 200 with status UP")
    void getHealth_returns200() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
