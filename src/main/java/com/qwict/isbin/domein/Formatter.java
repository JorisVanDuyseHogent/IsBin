package com.qwict.isbin.domein;

public class Formatter {
    public static String formatISBNToString(String isbn) {
        // Remove spaces and dashes
        return isbn.replaceAll("[\\s\\-()]", "");
    }
}
