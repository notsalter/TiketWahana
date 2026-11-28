package com.mycompany.tiketwahana.core;

import java.util.EnumSet;
import java.util.Set;

public enum Role {
    ADMIN("Administrator", EnumSet.allOf(Permission.class)),
    MANAGER("Manager", EnumSet.of(Permission.SELL, Permission.VOID, Permission.REFUND,
            Permission.MANAGE_WAHANA, Permission.VIEW_REPORT, Permission.REDEEM, Permission.SHIFT, Permission.AUDIT)),
    KASIR("Kasir", EnumSet.of(Permission.SELL, Permission.REDEEM, Permission.SHIFT));

    private final String label;
    private final Set<Permission> permissions;

    Role(String label, Set<Permission> permissions) {
        this.label = label;
        this.permissions = permissions;
    }

    public String label() {
        return label;
    }

    public boolean can(Permission permission) {
        return permissions.contains(permission);
    }

    public static Role of(String raw) {
        if (raw == null) {
            return KASIR;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return KASIR;
        }
    }

    public enum Permission {
        SELL, VOID, REFUND, REDEEM, MANAGE_WAHANA, VIEW_REPORT, SHIFT, AUDIT, MANAGE_USERS, SETTINGS
    }
}
