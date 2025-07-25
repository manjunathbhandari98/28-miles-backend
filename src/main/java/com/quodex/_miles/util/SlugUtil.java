package com.quodex._miles.util;

public class SlugUtil {

    public static String toSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // remove special chars
                .replaceAll("\\s+", "-")        // replace spaces with -
                .replaceAll("-{2,}", "-")       // remove duplicate hyphens
                .replaceAll("^-|-$", "");       // remove leading/trailing hyphens
    }
}
