package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum DbType {
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    SQLSERVER("SQLServer");

    private final String label;

    DbType(String label) {
        this.label = label;
    }
}
