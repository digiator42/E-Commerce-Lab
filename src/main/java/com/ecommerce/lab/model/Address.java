package com.ecommerce.lab.model;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Address))
            return false;

        Address that = (Address) o;

        return street.equals(that.street) &&
            city.equals(that.city) &&
            state.equals(that.state) &&
            zipCode.equals(that.zipCode) &&
            country.equals(that.country);
    }

    @Override
    public int hashCode() { return Objects.hash(street, city, state, zipCode, country); }
}
