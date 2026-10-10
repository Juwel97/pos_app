package com.example.posapp.devutils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.posapp.entity.Item;
import com.example.posapp.repository.ItemRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class ItemDataSeeder implements ApplicationRunner {

    private static final String[] MEDICAL_NAMES = {
        "Paracetamol 500mg",
        "Amoxicillin 250mg",
        "Ibuprofen 200mg",
        "Cetrizine 10mg",
        "Metformin 500mg",
        "Omeprazole 20mg",
        "Salbutamol Inhaler",
        "Vitamin C 1000mg",
        "ORS Sachet",
        "Amlodipine 5mg"
    };

    private static final String[] CATEGORIES = {
        "Analgesic",
        "Antibiotic",
        "Anti-inflammatory",
        "Allergy",
        "Diabetes",
        "Gastrointestinal",
        "Respiratory",
        "Vitamin",
        "Hydration",
        "Cardiac"
    };

    private final ItemRepository itemRepository;

    public ItemDataSeeder(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (itemRepository.count() > 0) {
            return;
        }

        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            String name = MEDICAL_NAMES[(i - 1) % MEDICAL_NAMES.length];
            String category = CATEGORIES[(i - 1) % CATEGORIES.length];
            String serial = String.format("MED-%04d", i);
            String batch = String.format("BATCH-%04d", i);
            String description = "Medical supply for " + category.toLowerCase() + " support.";
            BigDecimal price = BigDecimal.valueOf(35 + ((i * 13) % 140) + ((i % 5) * 4.75));
            int quantity = 15 + (i % 24);
            LocalDate expireDate = LocalDate.now().plusDays(120L + i);

            items.add(new Item(name, serial, batch, description, price, quantity, category, expireDate));
        }

        itemRepository.saveAll(items);
    }
}
