package com.example.myapplication.ui.home

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.myapplication.R
import com.example.myapplication.data.Airport
import com.example.myapplication.data.Favorite
import com.example.myapplication.data.IataWithName
import com.example.myapplication.data.IataWithName2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory)
) {
    val savedQueryDatStore by viewModel.dataStoreUiState.collectAsState()
    val getDeparture by viewModel.iataWithName.collectAsState()
    val getDestination by viewModel.iataWithName2.collectAsState()
    var isHome by remember { mutableStateOf(true) }
    var active by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var departureCode by remember { mutableStateOf("") }
    var destinationCode by remember { mutableStateOf("") }
    var searchAirport by remember { mutableStateOf("") }
    val searchAirportQuery by viewModel.searchAirportsByNameStream(query)
        .collectAsState(emptyList())
    val getAllAirportsExcept by viewModel.getAllAirportsExcept(searchAirport)
        .collectAsState(emptyList())

    val uiStateAirport by viewModel.tappedAirportState.collectAsState()
    val cachedBookmarks = viewModel.cashedBookmarks()
    var from by remember { mutableStateOf("") }

    Column {
        Box(
            Modifier
                .fillMaxWidth()
        ) {
            DockedSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                query = query,
                onQueryChange = { query = it },
                onSearch = { },
                active = active,
                onActiveChange = {
                    active = it;
                    isHome = false
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.placeholder),
                        modifier = Modifier
                            .graphicsLayer(alpha = 0.5f)
                    )
                },
            )
            {
                BackHandler {
                    isHome = true
                    active = false
                }
                SearchResults(
                    list = searchAirportQuery,
                    onClick = {
                        active = false;
                        searchAirport = it.name;
                        viewModel.tappAirport(it.id, it.name, it.iataCode);
                        from = it.iataCode
                    },
                    viewModel = viewModel,
                    saveQuery = query,
                    savedQuery = savedQueryDatStore.savedQuery,
                    savedClick = {
                        query = savedQueryDatStore.savedQuery
                    }
                )
            }
        }
        if (!active) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (!isHome) R.drawable.round_flight_takeoff_24 else R.drawable.round_bookmark_24
                    ),
                    contentDescription = null,
                )
                Text(
                    text = if (!isHome)
                        stringResource(R.string.flights_from, from) else
                            stringResource(R.string.bookmarked_flights),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
//        Spacer(modifier = Modifier.weight(1f))
        if ((!active && uiStateAirport.airportName.isNotEmpty() && !isHome)) {
            BackHandler {
                isHome = true
                active = false
            }
            AirportListResult(
                list = getAllAirportsExcept,
                firstAirportName = uiStateAirport.airportName,
                firstAirportIata = uiStateAirport.airportIata,
                viewModel = viewModel
            )
        } else {
            if (cachedBookmarks.isEmpty() && isHome) {
                FavoriteListResult(
                    departure = getDeparture.iataNameState,
                    destination = getDestination.iataNameState
                )
            } else {
                if (!active && FavoriteUiState().favoriteList.isEmpty()) {
                    EmptyScreenAbout(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 64.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResults(
    onClick: ((Airport) -> Unit)? = null,
    list: List<Airport>,
    modifier: Modifier = Modifier,
    saveQuery: String,
    savedQuery: String,
    savedClick: () -> Unit,
    viewModel: HomeViewModel
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = savedClick)
            .padding(8.dp)
            .fillMaxWidth(),
    ) {
        if (savedQuery.isNotBlank()) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.last_query),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = savedQuery)
        }
    }
    if (savedQuery.isNotBlank()) {
        Divider(
            thickness = 2.dp,
            modifier = Modifier.padding(
                bottom = 4.dp,
            )
        )
    }
    LazyColumn(modifier = modifier) {
        items(items = list, key = { it.id } ) {airport ->
            Column {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = onClick != null) {
                            onClick?.invoke(airport)
                            if (saveQuery.isNotBlank()) {
                                viewModel.saveQuery(saveQuery)
                            }
                        }
                        .padding(8.dp)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = airport.iataCode,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = airport.name)

                }
                if (airport != list[list.size - 1])
                    Divider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(
                            top = 4.dp,
                            bottom = 4.dp,
                            start = 32.dp,
                            end = 32.dp
                        )
                    )

            }
        }
    }
}

@Composable
fun AirportListResult(
    list: List<Airport>,
    firstAirportName: String = "",
    firstAirportIata: String = "",
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {

    val coroutineScope = rememberCoroutineScope()
    LazyColumn(modifier = modifier) {
        items(items = list, key = { it.id } ) {airport ->
            var departureCode by remember { mutableStateOf(firstAirportIata) }
            var destinationCode by remember { mutableStateOf(airport.iataCode) }
            val checkFavorite by viewModel.checkFavorites(departureCode, destinationCode)
                .collectAsState(0)
            var isSaved by remember { mutableStateOf(false) }
            AirportCard(
                airportFirst = airport,
                airportSecond = airport,
                firstAirportName = firstAirportName,
                firstAirportIata = firstAirportIata,
                icon = if (checkFavorite == 0)
                    R.drawable.round_bookmark_border_24 else R.drawable.round_bookmark_24,
                modifier = Modifier
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .clickable {
                        viewModel.cashedBookmarks()
                        coroutineScope.launch {
                            if (isSaved) {
                                viewModel.delete(
                                    airport.iataCode,
                                    firstAirportIata
                                )
                                isSaved = false
                            } else {
                                viewModel.uiModelToFavorite(
                                    UiModel(
                                        airport.iataCode,
                                        firstAirportIata,
                                    )
                                )
                                isSaved = true
                            }
                        }
                    },
            )
        }
    }
}

@Composable
fun FavoriteListResult(
    departure: List<IataWithName>,
    destination: List<IataWithName2>,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory)
) {
    val combinedList = combineLists(departure, destination)
    val coroutineScope = rememberCoroutineScope()
    if (combinedList.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            EmptyScreenAbout()
        }
    }
    LazyColumn(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
    ) {
        items(combinedList) {(departure, destination) ->
            FavoriteCard(
                departure,
                destination,
                modifier = Modifier
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .clickable {
                        coroutineScope.launch {
                            viewModel.delete(
                                destination.destinationCode,
                                departure.departureCode,
                            )
                        }
                    },
            )
        }
    }
}

fun combineLists(
    departure: List<IataWithName>,
    destination: List<IataWithName2>,
): List<Pair<IataWithName, IataWithName2>> {
    val combinedList = mutableListOf<Pair<IataWithName, IataWithName2>>()

    val minSize = minOf(departure.size, destination.size)
    for (i in 0 until minSize) {
        combinedList.add(departure[i] to destination[i])
    }

    return combinedList
}


@Composable
fun AirportCard(
    @DrawableRes icon: Int,
    airportFirst: Airport,
    airportSecond: Airport,
    firstAirportName: String = "",
    firstAirportIata: String = "",
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier,
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 176.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AirportFirst(
                    airport = airportFirst,
                    firstAirportName = firstAirportName,
                    firstAirportIata = firstAirportIata
                )
                Divider(
                    color = MaterialTheme.colorScheme.onPrimary,
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
                )
                AirportSecond(airport = airportSecond)
            }
            Spacer(modifier = Modifier.width(32.dp))
            Box(
                modifier = Modifier
                    .padding(end = 16.dp),
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun FavoriteCard(
    departure: IataWithName,
    destinatinon: IataWithName2,
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier,
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 176.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                FirstFavoriteCardInfo(
                    departure
                )
                Divider(
                    color = MaterialTheme.colorScheme.onPrimary,
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
                )
                SecondFavoriteCardInfo(
                    destinatinon
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            Box(
                modifier = Modifier
                    .padding(end = 16.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_bookmark_24),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun FirstFavoriteCardInfo(
    departure: IataWithName,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory)
) {
    Column(
        modifier = Modifier.padding(top = 16.dp, start = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = stringResource(R.string.depart),
                fontWeight = FontWeight.Light
            )
            Icon(
                painter = painterResource(R.drawable.round_flight_takeoff_24),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = departure.departureCode,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Text(
                text = departure.name,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
fun SecondFavoriteCardInfo(
    destinatinon: IataWithName2,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = stringResource(R.string.arrive),
                fontWeight = FontWeight.Light
            )
            Icon(
                painter = painterResource(R.drawable.round_flight_land_24),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ),
                contentAlignment = Alignment.Center) {
                Text(
                    text = destinatinon.destinationCode,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Text(
                text = destinatinon.name,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}


@Composable
fun AirportFirst(
    airport: Airport,
    firstAirportName: String = "",
    firstAirportIata: String = "",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.padding(top = 16.dp, start = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = stringResource(R.string.depart),
                fontWeight = FontWeight.Light
            )
            Icon(
                painter = painterResource(R.drawable.round_flight_takeoff_24),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstAirportIata,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Text(
                text = firstAirportName,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
fun AirportSecond(
    modifier: Modifier = Modifier,
    airport: Airport
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = stringResource(R.string.arrive),
                fontWeight = FontWeight.Light
            )
            Icon(
                painter = painterResource(R.drawable.round_flight_land_24),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ),
                contentAlignment = Alignment.Center) {
                Text(
                    text = airport.iataCode,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Text(
                text = airport.name,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
fun EmptyScreenAbout(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer(alpha = 0.25f)
        ) {
            Icon(
                painter = painterResource(R.drawable.round_bookmark_24),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.background_alert),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
