package com.cauri.core.data.service

import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryNature
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [CategoryStyleService]. */
@Singleton
internal class CategoryStyleServiceImpl @Inject constructor() : CategoryStyleService {
    override fun icon(name: String, nature: CategoryNature): CategoryIcon {
        val plain = Normalizer.normalize(name.lowercase(), Normalizer.Form.NFD)
            .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
        return WORDS.entries.firstOrNull { (_, words) -> words.any { it in plain } }?.key
            ?: when (nature) {
                CategoryNature.INCOME -> CategoryIcon.INCOME
                CategoryNature.SAVING -> CategoryIcon.SAVING
                CategoryNature.EXPENSE -> CategoryIcon.TAG
            }
    }

    override fun color(icon: CategoryIcon): CategoryColor = when (icon) {
        CategoryIcon.FOOD, CategoryIcon.PHONE, CategoryIcon.EDUCATION -> CategoryColor.LAGOON
        CategoryIcon.TRANSPORT, CategoryIcon.SAVING -> CategoryColor.GOLD
        CategoryIcon.FAMILY, CategoryIcon.HEALTH -> CategoryColor.CORAL
        CategoryIcon.LEISURE, CategoryIcon.INCOME -> CategoryColor.GREEN
        CategoryIcon.HOUSE, CategoryIcon.TAG -> CategoryColor.NEUTRAL
    }

    private companion object {
        // The words of the Cameroon pack and of the usual names, without accents.
        val WORDS = linkedMapOf(
            CategoryIcon.FOOD to listOf("aliment", "marche", "nourriture", "snack", "restaurant", "beignet"),
            CategoryIcon.TRANSPORT to listOf("transport", "moto", "taxi", "carburant", "essence", "bus"),
            CategoryIcon.HOUSE to listOf("logement", "loyer", "facture", "eneo", "camwater", "maison"),
            CategoryIcon.PHONE to listOf("credit", "forfait", "telephone", "internet", "data"),
            CategoryIcon.FAMILY to listOf("famille", "enfant", "parent"),
            CategoryIcon.EDUCATION to listOf("education", "ecole", "scolar", "livre"),
            CategoryIcon.HEALTH to listOf("sante", "pharmacie", "hopital", "medic"),
            CategoryIcon.LEISURE to listOf("loisir", "sortie", "sport", "cinema"),
            CategoryIcon.SAVING to listOf("tontine", "epargne", "njangi"),
            CategoryIcon.INCOME to listOf("salaire", "revenu", "vente"),
        )
    }
}
