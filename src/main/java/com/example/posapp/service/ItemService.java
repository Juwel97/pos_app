package com.example.posapp.service;

import com.example.posapp.dto.ItemPageResponse;
import com.example.posapp.dto.ItemRequest;
import com.example.posapp.entity.Item;
import com.example.posapp.exception.DuplicateSerialNumberException;
import com.example.posapp.exception.ItemNotFoundException;
import com.example.posapp.repository.ItemRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public Item add(ItemRequest request) {
        Item item = new Item(request.name().trim(), request.serialNumber().trim(), request.batchNumber(), request.description(),
            request.price(), request.quantity(), request.category(), request.expireDate(), request.mfgName(),
            Boolean.TRUE.equals(request.scheduleH1()));
        item.configureCapsuleStock(request.capsulesPerStrip(), capsuleStock(request));
        return save(item);
    }

    public Item update(Long id, ItemRequest request) {
        Item item = repository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
        int existingLooseCapsules = item.isCapsuleTracked() ? item.getLooseCapsules() : 0;
        item.update(request.name().trim(), request.mfgName(), request.serialNumber().trim(), request.batchNumber(), request.description(),
            request.price(), request.quantity(), request.category(), request.expireDate(), Boolean.TRUE.equals(request.scheduleH1()));
        int capsuleQuantity = Math.addExact(capsuleStock(request), existingLooseCapsules);
        item.configureCapsuleStock(request.capsulesPerStrip(), capsuleQuantity);
        return save(item);
    }

    private int capsuleStock(ItemRequest request) {
        return Math.multiplyExact(request.quantity(), request.capsulesPerStrip());
    }

    @Transactional(readOnly = true)
    public ItemPageResponse search(String term, int page, int size, boolean lowStockOnly) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = size <= 0 ? 10 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
        Page<Item> itemPage = repository.search(
            term == null ? "" : term.trim(),
            lowStockOnly,
            pageable
        );

        return new ItemPageResponse(
            itemPage.getContent(),
            itemPage.getNumber(),
            itemPage.getSize(),
            itemPage.getTotalElements(),
            itemPage.getTotalPages(),
            itemPage.hasPrevious(),
            itemPage.hasNext()
        );
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private Item save(Item item) {
        try {
            return repository.saveAndFlush(item);
        } catch (DataIntegrityViolationException exception) {
                throw new DuplicateSerialNumberException();
        }
    }
}