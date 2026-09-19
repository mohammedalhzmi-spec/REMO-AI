package com.ai.geminiapp

import java.text.Normalizer
import java.util.regex.Pattern

object ContentSafetyFilter {

    private val EXPLICIT_AND_ABUSIVE_WORDS = listOf(
        // Sexually Explicit & Pornographic Keywords (Arabic)
        "اباحي", "إباحي", "اباحية", "إباحية", "بورن", "سكس", "جنسي", "جنسية",
        "عاهر", "عاهرة", "شرموط", "شرموطة", "قحبة", "منيوك", "عرص", "ديوث",
        "زنا", "لواط", "سحاق", "سحاقية", "موجب", "سالب", "شذوذ",
        "عري", "عاري", "عارية", "تعري", "فسق", "فجور", "مجون",
        "مؤخرة", "كس", "زب", "طيز", "مني", "قضيب", "خصية", "ثدي", "بزاز",
        "سكسي", "مثيره", "شهوة", "شهواني", "مفاخذة", "اغتصاب", "نكاح",

        // Sexually Explicit & Pornographic Keywords (English)
        "porn", "pornography", "porno", "xxx", "nsfw", "sexy", "sexual",
        "nude", "nudity", "naked", "erotic", "orgasm", "penis", "vagina", "pussy",
        "dick", "cock", "boobs", "breast", "slut", "whore", "bitch", "blowjob",
        "handjob", "masturbat", "anal", "rape", "stripper", "hentai",

        // Abusive, Profane & Insulting Keywords (Arabic)
        "حمار", "يا حمار", "كلب", "يا كلب", "خنزير", "يا خنزير",
        "حقير", "يا حقير", "سافل", "يا سافل", "واطي", "يا واطي",
        "تافه", "يا تافه", "غبي", "يا غبي", "اهبل", "يا اهبل",
        "قذر", "يا قذر", "نجس", "وسخ", "يا وسخ",
        "تفو", "يلعن", "لعنة", "ابن الكلب", "ابن الحرام", "ابن الشرموطة",
        "ملعون", "يا ملعون", "تبا لك", "سحق لك", "يا رمة", "يا نذل",

        // Abusive, Profane & Insulting Keywords (English)
        "fuck", "fucking", "fucker", "motherfucker", "shit", "bullshit",
        "asshole", "bastard", "dumbass", "retard", "cunt", "faggot",
        "nigger", "dickhead", "moron"
    )

    // Regex pattern for matching offensive terms with optional word boundaries or repeated characters
    private val compiledPatterns: List<Pattern> by lazy {
        EXPLICIT_AND_ABUSIVE_WORDS.map { word ->
            Pattern.compile("(?i)\\b${Pattern.quote(word)}\\b|${Pattern.quote(word)}")
        }
    }

    /**
     * Normalizes text by removing diacritics, repeating characters, and unifying Arabic letters.
     */
    fun normalizeText(input: String): String {
        var clean = input.lowercase()
            .replace("[\\u064B-\\u065F\\u0670]".toRegex(), "") // Remove Arabic harakat/tashkeel
            .replace("ـ+".toRegex(), "") // Remove tatweel/kashida
            .replace("[أإآ]".toRegex(), "ا")
            .replace("ة".toRegex(), "ه")
            .replace("ى".toRegex(), "ي")

        // Remove excess repeated characters (e.g. حمااااار -> حمار)
        clean = clean.replace("(.)\\1{2,}".toRegex(), "$1$1")
        return clean
    }

    /**
     * Checks if the text contains any offensive, abusive, or sexually explicit / pornographic words.
     */
    fun containsExplicitOrAbusiveContent(text: String): Boolean {
        if (text.isBlank()) return false
        val normalized = normalizeText(text)

        for (word in EXPLICIT_AND_ABUSIVE_WORDS) {
            val normalizedWord = normalizeText(word)
            if (normalized.contains(normalizedWord)) {
                return true
            }
        }
        return false
    }

    /**
     * Automatically redacts/replaces all offensive and pornographic words with asterisks (***).
     */
    fun sanitizeAndRedact(text: String): String {
        var result = text
        for (word in EXPLICIT_AND_ABUSIVE_WORDS) {
            val regex = "(?i)${Pattern.quote(word)}".toRegex()
            result = result.replace(regex, "•••")
        }
        return result
    }

    /**
     * Generates a firm, polite, and dignified rejection response from Remo.
     */
    fun getRejectionResponse(): String {
        return """
🛡️ **تنبيه الحماية الأخلاقية والأمان الذاتي**
━━━━━━━━━━━━━━━━━━━━
عذراً يا صديقي العزيز، يلتزم **ريمو (REMO AI)** بمبادئ وأخلاقيات الحوار الراقي والمحترم دائماً.

🚫 **تم رصد وحظر الألفاظ غير اللائقة أو المسيئة تلقائياً، والامتناع التام عن الرد عليها.**

💡 أنا هنا لمساعدتك في كل ما يفيدك؛ من برمجة، بحث علمي موثق، أفكار إبداعية، ونصائح ملهمة.
يسعدني جداً أن تطرح سؤالك أو فكرتك بأسلوب راقٍ ومفيد وسأكون في خدمتك بكل سرور! ✨
━━━━━━━━━━━━━━━━━━━━
        """.trimIndent()
    }
}
