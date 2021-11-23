package com.switchfully.order.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.UUID;

@javax.persistence.Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Entity {

    @Id
    @GeneratedValue
    private UUID id;

    public UUID getId() {
        return id;
    }

    protected void setId(UUID id) {
        this.id = id;
    }
}
