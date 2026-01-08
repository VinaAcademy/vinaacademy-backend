package com.vinaacademy.platform.feature.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.category.repository.CategoryRepository;
import com.vinaacademy.platform.feature.common.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryMigrationService {
    private final CategoryRepository categoryRepository;

    /**
     * Create categories from JSON and return a map of id to Category object
     */
    public Map<String, Category> createCategoriesFromJson(JsonNode categoriesNode) {
        Map<String, Category> categoryMap = new HashMap<>();
        Map<String, Category> tempMap = new HashMap<>();
        Set<String> existingSlugs = new HashSet<>(); // Keep track of existing slugs

        // First pass: create all categories without parent relationships
        for (JsonNode categoryNode : categoriesNode) {
            String id = categoryNode.get("id").asText();
            String name = categoryNode.get("name").asText();

            // Generate slug using SlugUtils if needed, or use the one from JSON
            String originalSlug;
            if (categoryNode.has("slug") && !categoryNode.get("slug").isNull()) {
                originalSlug = categoryNode.get("slug").asText();

                if (existingSlugs.contains(originalSlug)) {
                    originalSlug = SlugUtils.toSlug(name);
                }
            } else {
                originalSlug = SlugUtils.toSlug(name);
            }

            // Check for duplicate slug and make it unique if needed
            String slug = ensureUniqueSlug(originalSlug, existingSlugs);

            Category category = Category.builder()
                    .name(name)
                    .slug(slug)
                    .build();

            tempMap.put(id, category);
        }

        // Second pass: set parent relationships and save categories
        for (JsonNode categoryNode : categoriesNode) {
            String id = categoryNode.get("id").asText();
            JsonNode parentIdNode = categoryNode.get("parentId");

            Category category = tempMap.get(id);

            if (parentIdNode != null && !parentIdNode.isNull()) {
                String parentId = parentIdNode.asText();
                Category parentCategory = tempMap.get(parentId);
                category.setParent(parentCategory);
            }

            categoryRepository.save(category);
            categoryMap.put(id, category);
        }

        log.info("Created {} categories from JSON", categoryMap.size());
        return categoryMap;
    }

    /**
     * Helper method to ensure slug uniqueness
     */
    private String ensureUniqueSlug(String originalSlug, Set<String> existingSlugs) {
        String slug = originalSlug;
        int counter = 1;
        // If the slug already exists, append a counter until we have a unique slug
        while (existingSlugs.contains(slug) || categoryRepository.findBySlug(slug).isPresent()) {
            slug = originalSlug + "-" + counter;
            counter++;
        }
        existingSlugs.add(slug);
        return slug;
    }
}
