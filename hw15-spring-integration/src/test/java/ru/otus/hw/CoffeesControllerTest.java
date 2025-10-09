package ru.otus.hw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoffeesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldProcessEspressoOrder() throws Exception {
        mockMvc.perform(post("/order-coffee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"espresso\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("espresso_roasted_ground"))
                .andExpect(jsonPath("$.status").value("Espresso Ready"));
    }

    @Test
    void shouldProcessCappuccinoOrder() throws Exception {
        mockMvc.perform(post("/order-coffee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"cappuccino\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("cappuccino_roasted_ground"))
                .andExpect(jsonPath("$.status").value("Cappuccino Ready"));
    }

    @Test
    void shouldHandleUnknownOrder() throws Exception {
        mockMvc.perform(post("/order-coffee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("unknown"))
                .andExpect(jsonPath("$.status").value("Error: Unable to process order"));
    }
}