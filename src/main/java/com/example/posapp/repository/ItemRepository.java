package com.example.posapp.repository;

import com.example.posapp.entity.Item;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id = :id")
    Optional<Item> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(i.price * i.quantity), 0) from Item i")
    BigDecimal getTotalInventoryValue();

    @Query("select count(i) from Item i where i.quantity < 5")
    long countLimitedStock();

    @Query("select i from Item i where :term = '' or "
            + "lower(i.name) like lower(concat('%', :term, '%')) or "
            + "lower(i.serialNumber) like lower(concat('%', :term, '%')) or "
            + "lower(coalesce(i.batchNumber, '')) like lower(concat('%', :term, '%')) or "
            + "lower(coalesce(i.category, '')) like lower(concat('%', :term, '%')) or "
            + "lower(coalesce(i.description, '')) like lower(concat('%', :term, '%')) "
            + "order by i.id desc")
    Page<Item> search(@Param("term") String term, Pageable pageable);
}