package com.refugio.repository;

import com.refugio.JPA.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByTipo(Plan.TipoPlan tipo);
}

