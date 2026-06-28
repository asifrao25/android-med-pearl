package com.knowledgepearls.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.settings.CreatorProfileConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AboutCreatorViewModel @Inject constructor(
    private val supabase: SupabaseClient,
) : ViewModel() {
    private val _creatorUserId = MutableStateFlow(CreatorProfileConfig.FALLBACK_USER_ID)
    val creatorUserId: StateFlow<String> = _creatorUserId.asStateFlow()

    init {
        viewModelScope.launch {
            _creatorUserId.value = CreatorProfileConfig.fetchUserId(supabase)
        }
    }
}
