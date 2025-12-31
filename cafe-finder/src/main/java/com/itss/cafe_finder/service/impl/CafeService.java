package com.itss.cafe_finder.service.impl;

import com.itss.cafe_finder.model.Cafe;
import com.itss.cafe_finder.repository.CafeRepository;
import dto.CafeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CafeService {

    @Autowired
    private CafeRepository cafeRepository;

    public ResponseEntity<Page<CafeDTO>> searchCafes(
            String keyword,
            Double minRating,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {

        String searchName = Optional.ofNullable(keyword)
                .map(String::trim)
                .orElse("");

        BigDecimal minRatingBd = minRating != null ? BigDecimal.valueOf(minRating) : null;

        boolean hasKeyword = !searchName.isEmpty();
        boolean hasRating = minRatingBd != null;

        Sort sort = Sort.unsorted();

        if (sortBy != null && !sortBy.isBlank()) {

            Sort.Direction direction =
                    "desc".equalsIgnoreCase(sortDirection)
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;

            switch (sortBy) {
                case "rating":
                    sort = Sort.by(direction, "rating");
                    break;

                default:
                    sort = Sort.by(direction, sortBy);
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Cafe> cafes;

        if (hasKeyword && hasRating) {
            cafes = cafeRepository
                    .findByNameContainingIgnoreCaseAndRatingGreaterThanEqual(
                            searchName, minRatingBd, pageable);

        } else if (hasKeyword) {
            cafes = cafeRepository
                    .findByNameContainingIgnoreCase(searchName, pageable);

        } else if (hasRating) {
            cafes = cafeRepository
                    .findByRatingGreaterThanEqual(minRatingBd, pageable);

        } else {
            cafes = cafeRepository.findAll(pageable);
        }

        return ResponseEntity.ok(cafes.map(this::toDTO));
    }

    public ResponseEntity<Page<CafeDTO>> getAllCafe(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cafe> cafes = cafeRepository.findAll(pageable);
        return ResponseEntity.ok(cafes.map(this::toDTO));
    }

    private CafeDTO toDTO(Cafe c) {
        CafeDTO dto = new CafeDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setAddress(c.getAddress());
        dto.setRating(c.getRating());
        dto.setLat(c.getLat());
        dto.setLng(c.getLng());
        dto.setDescription(c.getDescription());
        dto.setImage(c.getImage());
        dto.setStatus(c.getStatus() != null ? c.getStatus().toString() : "opening");
        return dto;
    }
}