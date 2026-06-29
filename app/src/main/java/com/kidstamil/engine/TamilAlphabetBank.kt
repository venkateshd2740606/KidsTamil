package com.kidstamil.engine

import com.kidstamil.domain.model.TamilAlphabetEntry

object TamilAlphabetBank {

    val all: List<TamilAlphabetEntry> = listOf(
        entry("அ", "a", "அம்மா", "Mother", "👩", listOf("ஆ", "இ", "உ")),
        entry("ஆ", "aa", "ஆடு", "Goat", "🐐", listOf("அ", "ஈ", "ஊ")),
        entry("இ", "i", "இலை", "Leaf", "🍃", listOf("ஈ", "எ", "அ")),
        entry("ஈ", "ii", "ஈ", "Fly", "🪰", listOf("இ", "ஐ", "ஒ")),
        entry("உ", "u", "உருளை", "Potato", "🥔", listOf("ஊ", "அ", "எ")),
        entry("ஊ", "uu", "ஊஞ்சல்", "Swing", "🛝", listOf("உ", "ஓ", "ஆ")),
        entry("எ", "e", "எலி", "Rat", "🐭", listOf("ஏ", "இ", "ஒ")),
        entry("ஏ", "ee", "ஏணி", "Ladder", "🪜", listOf("எ", "ஐ", "அ")),
        entry("ஐ", "ai", "ஐவி", "Five", "5️⃣", listOf("ஒ", "ஓ", "ஈ")),
        entry("ஒ", "o", "ஒட்டகம்", "Camel", "🐫", listOf("ஓ", "உ", "எ")),
        entry("ஓ", "oo", "ஓடம்", "Boat", "⛵", listOf("ஒ", "ஊ", "ஐ")),
        entry("க", "ka", "கால்", "Leg", "🦵", listOf("அ", "ச", "த")),
    )

    fun byIndex(index: Int): TamilAlphabetEntry = all[index.coerceIn(0, all.lastIndex)]

    fun byLetter(letter: String): TamilAlphabetEntry? =
        all.firstOrNull { it.letter == letter }

    private fun entry(
        letter: String,
        romanization: String,
        exampleWord: String,
        meaning: String,
        emoji: String,
        distractors: List<String>
    ): TamilAlphabetEntry = TamilAlphabetEntry(
        letter = letter,
        romanization = romanization,
        exampleWord = exampleWord,
        meaning = meaning,
        emoji = emoji,
        quizDistractors = distractors
    )
}
