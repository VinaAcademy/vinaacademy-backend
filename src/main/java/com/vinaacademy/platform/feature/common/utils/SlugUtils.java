package com.vinaacademy.platform.feature.common.utils;

import java.text.Normalizer;

public class SlugUtils {

    /**
     * Generate a slug from a given string.
     *
     * @param input The input string to be converted into a slug.
     * @return The generated slug.
     */
    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Convert to lower case
        String slug = input.toLowerCase();

        // Replace Vietnamese 'đ' with 'd' before normalization
        slug = slug.replace('đ', 'd');

        // Normalize the string and remove accents (for Vietnamese characters)
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = slug.replaceAll("\\p{M}", "");

        // Replace spaces and special characters with hyphens
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");  // Replace spaces with hyphens
        slug = slug.replaceAll("-+", "-");    // Remove consecutive hyphens

        // Remove hyphens at the start and end
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }
}
