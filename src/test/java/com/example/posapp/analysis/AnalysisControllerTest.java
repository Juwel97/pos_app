package com.example.posapp.analysis;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.posapp.entity.Item;
import com.example.posapp.repository.ItemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AnalysisControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ItemRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.save(newItem("SKU-1", "10.00", 3));
        repository.save(newItem("SKU-2", "5.00", 4));
        repository.save(newItem("SKU-3", "2.00", 5));
    }

    @Test
    void analysisEndpointsReturnInventoryTotals() throws Exception {
        mockMvc.perform(get("/analysis/total-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        mockMvc.perform(get("/analysis/total-value"))
                .andExpect(status().isOk())
                .andExpect(content().string("60.00"));

        mockMvc.perform(get("/analysis/limited-stock"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

            @Test
            void analysisValuesConfiguredCapsulesAndComparesStockToFiveStrips() throws Exception {
            Item configured = newItem("SKU-CAPSULES", "100.00", 4);
            configured.configureCapsuleStock(6, 25);
            repository.saveAndFlush(configured);

            mockMvc.perform(get("/analysis/total-value"))
                .andExpect(status().isOk())
                .andExpect(content().string("476.67"));

            mockMvc.perform(get("/analysis/limited-stock"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
            }

    private Item newItem(String sku, String price, int quantity) {
        return new Item("Medicine " + sku, sku, null, null, new BigDecimal(price), quantity, null,
                LocalDate.now().plusDays(30));
    }
}
