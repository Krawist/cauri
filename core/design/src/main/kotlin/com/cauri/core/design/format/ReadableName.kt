package com.cauri.core.design.format

/** « BOUTIQUE BETA1 » as « Boutique Beta1 »: messages often shout the names; a name already in mixed case is kept. */
fun readableName(name: String): String = if (name != name.uppercase()) {
    name
} else {
    name.lowercase().split(' ').joinToString(separator = " ") { word -> word.replaceFirstChar(Char::titlecase) }
}
