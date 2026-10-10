package com.example.posapp.item;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.IntStream;

import com.example.posapp.entity.Item;
import com.example.posapp.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ItemControllerPaginationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ItemRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void searchReturnsPagedResultsWithDefaultPageSize() throws Exception {
        IntStream.rangeClosed(1, 12).forEach(i -> repository.save(new Item(
                "Item " + i,
                "SKU-" + String.format("%02d", i),
                "BATCH-" + i,
                "Description " + i,
                new BigDecimal("10.00"),
                i,
                "Category " + i,
                LocalDate.now().plusDays(i)
        )));

        mockMvc.perform(get("/item/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
}
