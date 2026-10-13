package com.example.posapp.controller;

import com.example.posapp.dto.ItemPageResponse;
import com.example.posapp.dto.ItemRequest;
import com.example.posapp.entity.Item;
import com.example.posapp.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping("/item/add")
    public ResponseEntity<Item> add(@Valid @RequestBody ItemRequest request) {
        Item item = service.add(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/item/update/{id}")
    public Item update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/item/search")
    public ItemPageResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return service.search(q, page, size, lowStockOnly);
    }

    @DeleteMapping("/item/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}