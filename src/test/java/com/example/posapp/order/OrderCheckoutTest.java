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
                LocalDate.now().plusDays(30),
                "Test Manufacturer",
                true));
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
                .andExpect(jsonPath("$.totalCgst").value(4.5))
                .andExpect(jsonPath("$.totalSgst").value(4.5))
                .andExpect(jsonPath("$.grandTotal").value(189.0))
                .andExpect(jsonPath("$.doctorName").doesNotExist())
                .andExpect(jsonPath("$.items[0].itemName").value("Checkout item"))
                .andExpect(jsonPath("$.items[0].mfgName").value("Test Manufacturer"))
                .andExpect(jsonPath("$.items[0].batchNumber").value("BATCH-001"))
                .andExpect(jsonPath("$.items[0].expireDate").value(LocalDate.now().plusDays(30).toString()))
                .andExpect(jsonPath("$.items[0].scheduleH1").value(true))
                .andExpect(jsonPath("$.items[0].lineCgst").value(4.5))
                .andExpect(jsonPath("$.items[0].lineSgst").value(4.5))
                .andExpect(jsonPath("$.items[0].offerPrice").value(90.0));

        org.assertj.core.api.Assertions.assertThat(itemRepository.findById(item.getId()).orElseThrow().getQuantity())
                .isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void checkoutSavesOptionalPatientAndDoctorDetails() throws Exception {
        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "discountPercent":0,
                                  "doctorName":"Dr. Sharma",
                                  "patientName":"Aarav Das",
                                  "patientPhone":"9876543210",
                                  "items":[{"itemId":%d,"quantity":1}]
                                }
                                """.formatted(item.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorName").value("Dr. Sharma"))
                .andExpect(jsonPath("$.patientName").value("Aarav Das"))
                .andExpect(jsonPath("$.patientPhone").value("9876543210"));
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

        @Test
        void capsuleSaleUsesDerivedPriceAndConsumesCapsuleStock() throws Exception {
                item.configureCapsuleStock(6, 25);
                itemRepository.saveAndFlush(item);

                mockMvc.perform(post("/orders/checkout")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"discountPercent":0,"items":[{"itemId":%d,"quantity":3,"saleUnit":"CAPSULE"}]}
                                                                """.formatted(item.getId())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.totalQuantity").value(3))
                                .andExpect(jsonPath("$.totalPrice").value(50.01))
                                .andExpect(jsonPath("$.items[0].unitPrice").value(16.67))
                                .andExpect(jsonPath("$.items[0].saleUnit").value("CAPSULE"))
                                .andExpect(jsonPath("$.items[0].capsulesPerStrip").value(6));

                Item updated = itemRepository.findById(item.getId()).orElseThrow();
                org.assertj.core.api.Assertions.assertThat(updated.getCapsuleQuantity()).isEqualTo(22);
                org.assertj.core.api.Assertions.assertThat(updated.getQuantity()).isEqualTo(3);
                org.assertj.core.api.Assertions.assertThat(updated.getLooseCapsules()).isEqualTo(4);
        }

        @Test
        void stripSaleConsumesPackSizeAndKeepsExactStripPrice() throws Exception {
                item.configureCapsuleStock(10, 25);
                itemRepository.saveAndFlush(item);

                mockMvc.perform(post("/orders/checkout")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"discountPercent":0,"items":[{"itemId":%d,"quantity":2,"saleUnit":"STRIP"}]}
                                                                """.formatted(item.getId())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.totalPrice").value(200.0))
                                .andExpect(jsonPath("$.items[0].unitPrice").value(100.0))
                                .andExpect(jsonPath("$.items[0].saleUnit").value("STRIP"));

                org.assertj.core.api.Assertions.assertThat(itemRepository.findById(item.getId()).orElseThrow().getCapsuleQuantity())
                                .isEqualTo(5);
        }

        @Test
        void legacyItemsRejectCapsuleSalesWithoutChangingStock() throws Exception {
                mockMvc.perform(post("/orders/checkout")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"discountPercent":0,"items":[{"itemId":%d,"quantity":1,"saleUnit":"CAPSULE"}]}
                                                                """.formatted(item.getId())))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Checkout item is not configured for capsule sales"));

                org.assertj.core.api.Assertions.assertThat(itemRepository.findById(item.getId()).orElseThrow().getQuantity())
                                .isEqualTo(5);
                org.assertj.core.api.Assertions.assertThat(orderRepository.count()).isZero();
        }
}
