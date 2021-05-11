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

    public static String abstractType(ChangeType changeType){
        switch (changeType){
            case MODIFY:
                return "Modifica";
            case DELETE:
                return "Cancellazione";
            default: throw new IllegalArgumentException();
        }
    }
}
