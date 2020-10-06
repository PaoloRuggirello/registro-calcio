package com.example.registrocalcio.other;

public enum Category {
    CALCIO_A_5,
    CALCIO_A_7,
    CALCIO_A_11;

    @Override
    public String toString() {
        switch (this){
            case CALCIO_A_5:
                return "Calcio a 5";

            case CALCIO_A_7:
                return "Calcio a 7";

            case CALCIO_A_11:
                return "Calcio a 11";
            default: throw new IllegalArgumentException();
        }
    }
}
