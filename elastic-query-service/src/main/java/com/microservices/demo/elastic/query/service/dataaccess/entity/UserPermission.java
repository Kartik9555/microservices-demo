package com.microservices.demo.elastic.query.service.dataaccess.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class UserPermission {

    @Id
    @NotNull
    private UUID id;
    @NotNull
    private String username;
    @NotNull
    private String documentId;
    @NotNull
    private String permissionType;
}
