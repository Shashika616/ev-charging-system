package com.evcharging.stationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "chargers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Charger implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private String plugType;

    @Column(nullable = false)
    private Integer maxKw;

    // Link this charger to a specific grid zone
    @Column(nullable = false)
    private Long gridZoneId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargerStatus status = ChargerStatus.AVAILABLE;

    public enum ChargerStatus {
        AVAILABLE, RESERVED, IN_USE, OUT_OF_ORDER
    }
}