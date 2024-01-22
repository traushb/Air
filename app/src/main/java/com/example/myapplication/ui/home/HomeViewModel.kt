package com.example.myapplication.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.AirportApplication
import com.example.myapplication.data.Airport
import com.example.myapplication.data.AirportsDao
import com.example.myapplication.data.DataStoreRepository
import com.example.myapplication.data.Favorite
import com.example.myapplication.data.IataWithName
import com.example.myapplication.data.IataWithName2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dao: AirportsDao,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    private val _tappedAirportState = MutableStateFlow(TappedAirportState())
    val tappedAirportState: StateFlow<TappedAirportState> = _tappedAirportState.asStateFlow()

    private val _airportUiState = MutableStateFlow(AirportUiState())
    val airportUiState: StateFlow<AirportUiState> = _airportUiState.asStateFlow()

    fun checkFavorites(departureCode: String, destinationCode: String): Flow<Int> =
        dao.findFavorite(departureCode, destinationCode)

    fun getAllAirportsExcept(name: String): Flow<List<Airport>> =
        dao.getAllAirportsExcept(name)

    fun searchAirportsByNameStream(search: String): Flow<List<Airport>> =
        dao.searchAirportsByNameStream(search)

    fun tappAirport(
        id: Int,
        name: String,
        iata: String
    ) {
        _tappedAirportState.update {
            it.copy(
                id = id,
                airportName = name,
                airportIata = iata
            )
        }
    }

    suspend fun uiModelToFavorite(uiMode: UiModel) {
        val favorite = Favorite(
            destinationCode = uiMode.destinationCode,
            departureCoded = uiMode.departureCode
        )

        dao.bookmark(favorite)
    }


    val bookmarkedUiState: StateFlow<FavoriteUiState> =
        dao.getAllBookmarks().map { FavoriteUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = FavoriteUiState()
            )

    init {
        if (bookmarkedUiState.value.favoriteList.isNotEmpty()) {
            cashedBookmarks()
        }
    }

    val iataWithName: StateFlow<IataNameState> =
        dao.getDepartureCode().map { IataNameState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = IataNameState()
            )

    val iataWithName2: StateFlow<IataNameState2> =
        dao.getDestinationCode().map { IataNameState2(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = IataNameState2()
            )

    suspend fun delete(
        departureCode: String,
        destinationCode: String,
    ) {
        dao.delete(
            departureCode,
            destinationCode
        )
    }

    fun cashedBookmarks(): MutableMap<String, String> {
        val bookmarks = mutableMapOf<String, String>()
        val favorites = bookmarkedUiState.value.favoriteList
        viewModelScope.launch {
            favorites.forEach {
                bookmarks[it.departureCoded] = it.destinationCode
            }
        }
        return bookmarks
    }

    // DataStore
    fun saveQuery(saveQuery: String) {
        viewModelScope.launch {
            dataStoreRepository.saveQuery(saveQuery)
        }
    }

    val dataStoreUiState: StateFlow<DataStoreState> =
        dataStoreRepository.savedQuery.map { query ->
            DataStoreState(query)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DataStoreState()
        )


    companion object {
        val factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as AirportApplication)
                HomeViewModel(application.database.airportsDao(), application.dataStoreRepository)
            }
        }
    }
}


data class IataNameState(
    val iataNameState: List<IataWithName> = listOf()
)

data class IataNameState2(
    val iataNameState: List<IataWithName2> = listOf()
)

data class TappedAirportState(
    val id: Int = 0,
    val airportName: String = "",
    val airportIata: String = ""
)

data class UiModel(
    val destinationCode: String,
    val departureCode: String
)

data class DataStoreState(
    val savedQuery: String = ""
)

data class AirportUiState(
    val airports: List<Airport> = listOf()
)

data class FavoriteUiState(
    val favoriteList: List<Favorite> = listOf()
)

data class BookamrksUiState(
    val bookamrks: List<Favorite> = listOf()
)
