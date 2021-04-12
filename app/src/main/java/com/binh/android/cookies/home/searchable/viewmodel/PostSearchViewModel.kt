package com.binh.android.cookies.home.searchable.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class PostSearchViewModel(application: Application) : AndroidViewModel(application) {
    private val client = ClientSearch(
        ApplicationID("MAN6LLCBXM"),
        APIKey("e2caf90f780be0d5061f4767102d6b36"),
        LogLevel.ALL
    )
    private val index = client.initIndex(IndexName("posts"))
    private val searcher = SearcherSingleIndex(index)

    private val dataSourceFactory = SearcherSingleIndexDataSource.Factory(searcher) {
        it.deserialize(PostSearched.serializer())
    }

    private val pagedListConfig = PagedList.Config.Builder().setPageSize(50).build()
    val posts: LiveData<PagedList<PostSearched>> =
        LivePagedListBuilder(dataSourceFactory, pagedListConfig).build()

    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(posts))
    private val connection = ConnectionHandler()

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

    @Suppress("UNCHECKED_CAST")
    class PostSearchViewModelFactory constructor(
        private val application: Application
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostSearchViewModel::class.java)) {
                return PostSearchViewModel(application) as T
            }
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}