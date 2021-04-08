package com.binh.android.cookies.searchable.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.list.SearcherSingleIndexDataSource
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.stats.StatsConnector
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.binh.android.cookies.data.PostSearched
import io.ktor.client.features.logging.*

private const val TAG = "PostSearchViewModel"

class PostSearchViewModel(application: Application) : AndroidViewModel(application) {
    val client = ClientSearch(
        ApplicationID("MAN6LLCBXM"),
        APIKey("e2caf90f780be0d5061f4767102d6b36"),
        LogLevel.ALL
    )
    val index = client.initIndex(IndexName("posts"))
    val searcher = SearcherSingleIndex(index)

    val dataSourceFactory = SearcherSingleIndexDataSource.Factory(searcher) {
        it.deserialize(PostSearched.serializer())
    }

    val pagedListConfig = PagedList.Config.Builder().setPageSize(50).build()
    val posts: LiveData<PagedList<PostSearched>> =
        LivePagedListBuilder(dataSourceFactory, pagedListConfig).build()

    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(posts))
    val connection = ConnectionHandler()

    val stats = StatsConnector(searcher)

    init {
        connection += searchBox
        connection += stats
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
        connection.clear()
    }
}