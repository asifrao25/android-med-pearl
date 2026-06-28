package com.knowledgepearls.app.data.settings

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

object AboutCreatorContent {
    const val NAME = "Dr Asif Rao"
    const val ROLE = "ST6 · Diabetes & Endocrinology"
    const val SUBTITLE = "Creator of Med Pearls"
    const val BIO =
        "I'm Dr Asif Rao, a physician who trained at Allama Iqbal Medical College in Lahore, Pakistan. " +
            "I'm currently an ST6 in Diabetes & Endocrinology in the East Midlands Deanery.\n\n" +
            "I built Med Pearls because I wanted a better way to capture what I learn in clinic — the practical " +
            "insights, teaching moments, and references that are easy to lose between busy days. My work sits at the " +
            "intersection of clinical medicine, health education, and technology, and I'm especially interested in " +
            "tools that support safer care, clearer teaching, and learning that actually sticks.\n\n" +
            "This app is my attempt to turn everyday clinical knowledge into something you can keep, organise, and " +
            "revisit — without losing the nuance that makes it useful at the bedside."
}

object CreatorProfileConfig {
    const val FALLBACK_USER_ID = "0a991d74-e0e3-4976-85e9-6d3efb76879b"

    @Serializable
    private data class AppSettingRow(val value: String)

    suspend fun fetchUserId(supabase: SupabaseClient): String {
        return runCatching {
            supabase.from("app_settings").select {
                filter { eq("key", "creator_user_id") }
                limit(1)
            }.decodeList<AppSettingRow>().firstOrNull()?.value?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: FALLBACK_USER_ID
    }
}
