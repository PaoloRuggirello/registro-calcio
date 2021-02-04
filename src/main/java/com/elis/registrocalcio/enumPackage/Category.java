package com.elis.registrocalcio.enumPackage;

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

    public int numberOfAllowedPlayers() {
        switch (this){
            case CALCIO_A_5:
                return 10;

            case CALCIO_A_7:
                return 14;

            case CALCIO_A_11:
                return 22;
            default: throw new IllegalArgumentException();
        }
    }

    public static Category getCategoryFromString(String categoryString){
        if(categoryString.equals(CALCIO_A_5.toString()))
            return CALCIO_A_5;
        else if(categoryString.equals(CALCIO_A_7.toString()))
            return CALCIO_A_7;
        else if(categoryString.equals(CALCIO_A_11.toString()))
            return CALCIO_A_11;
        else throw new IllegalArgumentException("CATEGORY_NOT_FOUND");
    }
}
