package com.example.myapplication.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeScreenContentStateTest {
    @Test
    fun homeScreenContentState_returnsEmpty_whenNoFavoritesExist() {
        assertEquals(HomeScreenContentState.Empty, homeScreenContentState(hasNoFavorites = true))
    }

    @Test
    fun homeScreenContentState_returnsFavorites_whenFavoritesExist() {
        assertEquals(HomeScreenContentState.Favorites, homeScreenContentState(hasNoFavorites = false))
    }
}
