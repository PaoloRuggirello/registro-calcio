package com.elis.registrocalcio.enumPackage;

public enum Team {
    WHITE,
    BLACK;

    public static Team getTeamFromString(String categoryString){
        if(categoryString.equals(WHITE.toString()))
            return WHITE;
        else if(categoryString.equals(BLACK.toString()))
            return BLACK;
        else throw new IllegalArgumentException("TEAM_NOT_FOUND");
    }
}
