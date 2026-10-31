package com.mb.modulith.commerce.enums;

import java.util.List;

public enum UserRole {
    ROLE_USER,
    ROLE_MODERATOR,
    ROLE_ADMIN,
    CREATE_CLIENT,
    UPDATE_CLIENT,
    GET_CLIENT,
    DELETE_CLIENT,
    CREATE_ORDER,
    UPDATE_ORDER,
    GET_ORDER,
    DELETE_ORDER;

    public static List<UserRole> getDefaultRoles() {
        return List.of(UserRole.CREATE_CLIENT, UserRole.UPDATE_CLIENT,
                UserRole.GET_CLIENT, UserRole.DELETE_CLIENT,
                UserRole.CREATE_ORDER, UserRole.UPDATE_ORDER,
                UserRole.GET_ORDER, UserRole.DELETE_ORDER);
    }
}
