package com.antonyukV516.model;

public enum Tag {
    COFFEE,
    WALK,
    SPORT,
    FOOD,
    MOVIE,
    GAMES,
    STUDY,
    WORK,
    MUSIC,
    ART,
    TRAVEL,
    BUSINESS,
    TECH,
    BOOKS,
    BAR,
    LANGUAGE;

    public static String getAllTagsAsString() {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : values()) {
            sb.append("• ").append(tag.name().toLowerCase()).append("\n");
        }
        return sb.toString();
    }
}
