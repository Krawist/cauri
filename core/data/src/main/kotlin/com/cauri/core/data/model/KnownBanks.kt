package com.cauri.core.data.model

private const val CAMEROON = "CM"

/**
 * The banks the user can declare. The Cameroon list follows the Ministry of Finance (« Secteur financier au
 * 30 septembre 2023 »): to check against the current COBAC list before release, as the other operator data.
 */
val knownBanks: List<Bank> = listOf(
    Bank(code = "ACCESS_BANK", name = "Access Bank", description = "", country = CAMEROON),
    Bank(code = "AFG_BANK", name = "AFG Bank", description = "", country = CAMEROON),
    Bank(code = "AFRICA_GOLDEN_BANK", name = "Africa Golden Bank", description = "", country = CAMEROON),
    Bank(code = "AFRILAND_FIRST_BANK", name = "Afriland First Bank", description = "", country = CAMEROON),
    Bank(code = "BANGE_BANK", name = "BANGE Bank", description = "", country = CAMEROON),
    Bank(code = "BANQUE_ATLANTIQUE", name = "Banque Atlantique", description = "", country = CAMEROON),
    Bank(code = "BC_PME", name = "BC-PME", description = "", country = CAMEROON),
    Bank(code = "BGFIBANK", name = "BGFIBank", description = "", country = CAMEROON),
    Bank(code = "BICEC", name = "BICEC", description = "", country = CAMEROON),
    Bank(code = "CCA_BANK", name = "CCA-Bank", description = "", country = CAMEROON),
    Bank(code = "CITIBANK", name = "Citibank", description = "", country = CAMEROON),
    Bank(code = "COMMERCIAL_BANK_CAMEROON", name = "Commercial Bank Cameroon", description = "", country = CAMEROON),
    Bank(code = "ECOBANK", name = "Ecobank", description = "", country = CAMEROON),
    Bank(code = "LA_REGIONALE_BANK", name = "La Régionale Bank", description = "", country = CAMEROON),
    Bank(code = "NFC_BANK", name = "NFC Bank", description = "", country = CAMEROON),
    Bank(code = "SCB_CAMEROUN", name = "SCB Cameroun", description = "", country = CAMEROON),
    Bank(code = "SOCIETE_GENERALE", name = "Société Générale", description = "", country = CAMEROON),
    Bank(code = "STANDARD_CHARTERED", name = "Standard Chartered", description = "", country = CAMEROON),
    Bank(code = "UNION_BANK_OF_CAMEROON", name = "Union Bank of Cameroon", description = "", country = CAMEROON),
    Bank(code = "UBA", name = "UBA", description = "", country = CAMEROON),
)
