package com.evcharging.gridservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grid_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GridZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String zoneName; // e.g., "Downtown-North"

    @Column(nullable = false)
    private Integer transformerCapacityKw; // Max power this zone can handle

    @Column(nullable = false)
    private Integer currentLoadKw = 0; // Current power being used
}