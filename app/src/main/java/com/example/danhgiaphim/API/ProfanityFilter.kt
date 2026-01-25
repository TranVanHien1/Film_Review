package com.example.danhgiaphim.API

object ProfanityFilter {
    // Danh sách từ cấm (có thể mở rộng hoặc tải từ database/API)
    private val bannedWords = listOf(
        "fuck", "shit", "asshole", "bitch", "cunt", "dick",
        "đụ", "địt", "lồn", "cặc", "đĩ", "cứt", "chó", "mẹ mày",
        "đéo", "vãi", "vl", "cl", "dm", "cc", "vl", "lol"
    ).map { it.lowercase() }

    /**
     * Kiểm tra văn bản có chứa từ cấm không
     * @param text Văn bản cần kiểm tra
     * @return true nếu chứa từ cấm, false nếu không
     */
    fun containsProfanity(text: String): Boolean {
        if (text.isBlank()) return false
        return bannedWords.any { bannedWord ->
            text.lowercase().contains(bannedWord)
        }
    }

    /**
     * Lọc bỏ các từ cấm trong văn bản
     * @param text Văn bản cần lọc
     * @return Văn bản đã được lọc
     */
    fun filterProfanity(text: String): String {
        if (text.isBlank()) return text

        var filteredText = text
        bannedWords.forEach { word ->
            filteredText = filteredText.replace(word, "***", ignoreCase = true)
        }
        return filteredText
    }
}