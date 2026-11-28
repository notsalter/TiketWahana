package com.mycompany.tiketwahana.core;

public record SessionUser(int id, String username, String fullName, Role role, boolean mustChangePassword) {

    public boolean can(Role.Permission permission) {
        return role.can(permission);
    }
}
