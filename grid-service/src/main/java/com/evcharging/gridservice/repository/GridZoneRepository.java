package com.evcharging.gridservice.repository;

import com.evcharging.gridservice.model.GridZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GridZoneRepository extends JpaRepository<GridZone, Long> {
}