package com.cauri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.design.component.HelpBubbles
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * The help bubbles of the app ([HelpBubbles]): of those on screen, the first asked for that the user never closed
 * shows, once the closed ones are read; closing one saves it.
 */
@HiltViewModel
internal class HelpBubblesViewModel @Inject constructor(private val preferences: UserPreferencesRepository) :
    ViewModel(),
    HelpBubbles {
    // Compose states: a bubble shows again as soon as they change.
    private var seen by mutableStateOf<Set<String>?>(null)
    private val requested = mutableStateListOf<String>()

    init {
        viewModelScope.launch { preferences.seenHelp.collect { seen = it } }
    }

    override fun isShown(id: String): Boolean {
        val closed = seen ?: return false
        return requested.firstOrNull { it !in closed } == id
    }

    override fun request(id: String) {
        if (id !in requested) requested += id
    }

    override fun release(id: String) {
        requested -= id
    }

    override fun close(id: String) {
        seen = seen.orEmpty() + id
        viewModelScope.launch { preferences.markHelpSeen(id) }
    }
}
