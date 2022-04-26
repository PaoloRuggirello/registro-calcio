package com.elis.registrocalcio.enumPackage;

public enum Role {
    ADMIN(0),
    USER(1),
    EXTERNAL(2);

    private int permissionLevel;

    Role(int level) {
        this.permissionLevel = level;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }
}
