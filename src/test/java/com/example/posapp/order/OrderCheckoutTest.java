package com.example.posapp.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.posapp.entity.Item;
import com.example.posapp.repository.ItemRepository;
import com.example.posapp.repository.OrderHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class OrderCheckoutTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderHistoryRepository orderRepository;

    private MockMvc mockMvc;
    private Item item;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        item = itemRepository.saveAndFlush(new Item(
                "Checkout item",
                "CHECKOUT-001",
                "BATCH-001",
                "Test item",
                new BigDecimal("100.00"),
                5,
                "Test",
                LocalDate.now().plusDays(30)));
    }

    @Test
    void checkoutSavesOrderCalculatesDiscountAndReducesStock() throws Exception {
        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"discountPercent":10,"items":[{"itemId":%d,"quantity":2}]}
                                """.formatted(item.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.discountPercent").value(10))
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(200.0))
                .andExpect(jsonPath("$.totalOfferPrice").value(180.0))
                .andExpect(jsonPath("$.items[0].itemName").value("Checkout item"))
                .andExpect(jsonPath("$.items[0].offerPrice").value(90.0));

        org.assertj.core.api.Assertions.assertThat(itemRepository.findById(item.getId()).orElseThrow().getQuantity())
                .isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void insufficientStockDoesNotSaveOrderOrChangeInventory() throws Exception {
        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"discountPercent":0,"items":[{"itemId":%d,"quantity":6}]}
                                """.formatted(item.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Checkout item has only 5 unit(s) available"));

        org.assertj.core.api.Assertions.assertThat(itemRepository.findById(item.getId()).orElseThrow().getQuantity())
                .isEqualTo(5);
        org.assertj.core.api.Assertions.assertThat(orderRepository.count()).isZero();
    }
}
