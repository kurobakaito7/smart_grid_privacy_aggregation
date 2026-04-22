package com.smartgrid.repository;

import com.smartgrid.entity.Aggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AggregationRepository extends JpaRepository<Aggregation, Long> {
    List<Aggregation> findByWindowStartBetween(LocalDateTime start, LocalDateTime end);
}
