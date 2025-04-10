package com.crewmeister.currencybackend.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "currencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @Column(name = "code", length = 3)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "country")
    private String country;

    @Column(name = "active")
    private boolean active;

    @OneToMany(mappedBy = "currency")
    private Set<ExchangeRate> exchangeRates = new HashSet<>();

    // Constructor without exchangeRates for builder pattern convenience
    public Currency(String code, String name, String country, boolean active) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.active = active;
    }
}
