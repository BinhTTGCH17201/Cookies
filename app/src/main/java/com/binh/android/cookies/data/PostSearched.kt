package com.binh.android.cookies.data


import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.google.firebase.database.Exclude
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PostSearched(
    val postId: String = "",
    val title: String = "",
    val author: String = "",
    val ingredient: String = "",
    val people: Int = 0,
    val time: Int = 0,
    val type: String = "",
    val preparation: String = "",
    var photoUrl: String = "",
    val like: Int = 0,
    @Exclude override val _highlightResult: JsonObject?
) : Highlightable {

    val highlightedTitle: HighlightedString?
        get() = getHighlight(Attribute("title"))
}