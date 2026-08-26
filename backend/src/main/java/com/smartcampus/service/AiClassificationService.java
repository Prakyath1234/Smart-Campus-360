package com.smartcampus.service;

import com.smartcampus.entity.ComplaintCategory;
import com.smartcampus.entity.ComplaintPriority;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AiClassificationService {

    @Data
    public static class ClassificationResult {
        private final ComplaintCategory category;
        private final ComplaintPriority priority;
    }

    public ClassificationResult classify(String description) {
        if (description == null || description.trim().isEmpty()) {
            return new ClassificationResult(ComplaintCategory.OTHER, ComplaintPriority.LOW);
        }

        String text = description.toLowerCase(Locale.ROOT);

        // 1. Determine Category
        ComplaintCategory category = ComplaintCategory.OTHER;
        if (containsAny(text, "wire", "shock", "fire", "safety", "harass", "bully", "stranger", "hazard", "threat", "theft", "steal")) {
            category = ComplaintCategory.SAFETY;
        } else if (containsAny(text, "leak", "plumbing", "toilet", "fan", "light", "ac", "air condition", "pipe", "geyser", "bulb", "switch")) {
            category = ComplaintCategory.MAINTENANCE;
        } else if (containsAny(text, "broken", "chair", "desk", "board", "wall", "paint", "window", "door", "bench")) {
            category = ComplaintCategory.INFRASTRUCTURE;
        } else if (containsAny(text, "mess", "hostel", "warden", "roommate", "room", "food")) {
            category = ComplaintCategory.HOSTEL;
        } else if (containsAny(text, "bus", "shuttle", "transport", "parking", "driver", "route")) {
            category = ComplaintCategory.TRANSPORT;
        } else if (containsAny(text, "grade", "marks", "exam", "syllabus", "lecture", "professor", "course", "attendance", "teacher")) {
            category = ComplaintCategory.ACADEMIC;
        }

        // 2. Determine Priority
        ComplaintPriority priority = ComplaintPriority.LOW;
        if (containsAny(text, "fire", "shock", "wire", "harass", "threat", "weapon", "danger", "emergency", "injury", "critical")) {
            priority = ComplaintPriority.CRITICAL;
        } else if (containsAny(text, "broken", "leak", "food", "exam", "grade", "bully", "water", "flood", "high")) {
            priority = ComplaintPriority.HIGH;
        } else if (containsAny(text, "mess", "toilet", "parking", "fan", "light", "bus", "medium")) {
            priority = ComplaintPriority.MEDIUM;
        }

        return new ClassificationResult(category, priority);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
