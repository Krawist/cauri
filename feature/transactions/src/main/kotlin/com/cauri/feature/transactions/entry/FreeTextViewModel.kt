package com.cauri.feature.transactions.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.ai.LanguageModel
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.ManualEntry
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.parsing.freetext.FreeTextReader
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * « taxi 1500, beignets 500 » in the quick entry: the phone's model reads the expenses of the text, only those whose
 * amount is written in it are kept, and nothing is saved before the user agrees.
 */
@HiltViewModel
internal class FreeTextViewModel @Inject constructor(
    private val model: LanguageModel,
    private val reader: FreeTextReader,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val clock: Clock,
) : ViewModel() {
    private val state = MutableStateFlow(FreeTextUiState())

    val uiState: StateFlow<FreeTextUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val available = model.isAvailable()
            state.update { it.copy(available = available) }
        }
    }

    fun onText(text: String) = state.update { it.copy(text = text, nothingFound = false) }

    /** Has the model read the text; what it read waits for the user. */
    fun onRead() {
        val text = state.value.text.trim()
        if (text.isEmpty() || state.value.reading) return
        state.update { it.copy(reading = true, nothingFound = false) }
        viewModelScope.launch {
            val expenses = categories.categories.first().filter { it.nature == CategoryNature.EXPENSE }
            val names = expenses.map { it.name }
            val read = model.answer(reader.prompt(text, names))?.let { reader.read(it, text, names) }.orEmpty()
            val lines = read.map { expense ->
                val category = expenses.firstOrNull { it.name == expense.category }
                FreeTextLine(
                    label = expense.label,
                    amount = expense.amount,
                    categoryId = category?.id,
                    categoryName = category?.name,
                )
            }
            state.update {
                it.copy(
                    reading = false,
                    lines = lines.takeIf(List<FreeTextLine>::isNotEmpty),
                    nothingFound = lines.isEmpty(),
                )
            }
        }
    }

    /** Leaves the line [index] out of what is saved. */
    fun onRemove(index: Int) = state.update { current ->
        val lines = current.lines?.filterIndexed { i, _ -> i != index }
        current.copy(lines = lines?.takeIf { it.isNotEmpty() })
    }

    /** Saves the lines read as confirmed expenses on [accountId], now, each named as the user wrote it. */
    fun onSave(accountId: Long) {
        val lines = state.value.lines ?: return
        viewModelScope.launch {
            lines.forEach { line ->
                transactions.recordEntry(
                    ManualEntry(
                        accountId = accountId,
                        type = EntryType.EXPENSE,
                        amount = line.amount,
                        categoryId = line.categoryId,
                        note = line.label,
                        occurredAt = clock.instant(),
                        proposedCategoryId = line.categoryId,
                        fromFreeText = true,
                    ),
                )
            }
            state.update { it.copy(lines = null, text = "", saved = true) }
        }
    }

    fun onSavedShown() = state.update { it.copy(saved = false) }
}
