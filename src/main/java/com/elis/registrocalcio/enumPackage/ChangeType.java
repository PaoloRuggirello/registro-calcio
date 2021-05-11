package com.elis.registrocalcio.enumPackage;

public enum ChangeType {
    MODIFY,
    DELETE;

    @Override
    public String toString() {
        switch (this){
            case MODIFY:
                return "modificato";
            case DELETE:
                return "cancellato";
            default: throw new IllegalArgumentException();
        }
    }
}
