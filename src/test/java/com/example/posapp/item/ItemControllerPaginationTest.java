package com.example.posapp.item;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void searchCanFilterLowStockItems() throws Exception {
        int[] quantities = {0, 2, 4, 5, 8};
        IntStream.range(0, quantities.length).forEach(i -> repository.save(new Item(
                "Item " + i,
                "SKU-" + i,
                "BATCH-" + i,
                "Description " + i,
                new BigDecimal("10.00"),
                quantities[i],
                "Category " + i,
                LocalDate.now().plusDays(i)
        )));

        mockMvc.perform(get("/item/search").param("lowStockOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

            @Test
            void addInitializesCapsuleStockFromWholeStrips() throws Exception {
            mockMvc.perform(post("/item/add")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"New medicine",
                      "mfgName":"Acme Pharma",
                      "serialNumber":"SKU-NEW",
                      "price":100.00,
                      "quantity":2,
                      "capsulesPerStrip":10
                    }
                    """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.mfgName").value("Acme Pharma"))
                                .andExpect(jsonPath("$.scheduleH1").value(false))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.capsuleQuantity").value(20))
                .andExpect(jsonPath("$.looseCapsules").value(0));
            }

            @Test
            void updateConfiguresCapsuleStockFromWholeStrips() throws Exception {
            Item item = repository.saveAndFlush(new Item(
                "Existing medicine", "SKU-EXISTING", "BATCH-1", "", new BigDecimal("100.00"), 4,
                "Medicine", LocalDate.now().plusDays(30)));

            mockMvc.perform(put("/item/update/{id}", item.getId())
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name":"Existing medicine",
                          "serialNumber":"SKU-EXISTING",
                          "batchNumber":"BATCH-1",
                          "description":"",
                          "price":100.00,
                          "quantity":4,
                          "category":"Medicine",
                          "expireDate":"%s",
                                                    "capsulesPerStrip":10,
                                                    "scheduleH1":true
                        }
                        """.formatted(LocalDate.now().plusDays(30))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4))
                .andExpect(jsonPath("$.capsulesPerStrip").value(10))
                .andExpect(jsonPath("$.scheduleH1").value(true))
                                .andExpect(jsonPath("$.capsuleQuantity").value(40))
                                .andExpect(jsonPath("$.looseCapsules").value(0));
            }

            @Test
            void updateRequiresUnitsPerStrip() throws Exception {
            Item item = repository.saveAndFlush(new Item(
                "Existing medicine", "SKU-EXISTING", "BATCH-1", "", new BigDecimal("100.00"), 4,
                "Medicine", LocalDate.now().plusDays(30)));

            mockMvc.perform(put("/item/update/{id}", item.getId())
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name":"Existing medicine",
                          "serialNumber":"SKU-EXISTING",
                          "price":100.00,
                          "quantity":4
                        }
                        """))
                .andExpect(status().isBadRequest());
            }

            @Test
            void updatePreservesRemainingUnitsAfterPartialSale() throws Exception {
            Item item = repository.saveAndFlush(new Item(
                "Existing medicine", "SKU-EXISTING", "BATCH-1", "", new BigDecimal("100.00"), 4,
                "Medicine", LocalDate.now().plusDays(30)));
            item.configureCapsuleStock(10, 42);
            item = repository.saveAndFlush(item);

            mockMvc.perform(put("/item/update/{id}", item.getId())
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name":"Existing medicine",
                          "serialNumber":"SKU-EXISTING",
                          "price":100.00,
                          "quantity":4,
                                                    "capsulesPerStrip":10
                        }
                        """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.capsuleQuantity").value(42))
                                .andExpect(jsonPath("$.looseCapsules").value(2));
            }
}
