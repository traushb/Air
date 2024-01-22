package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportsDao {
    // Airport table
    @Query("SELECT * FROM airport ORDER BY passengers DESC")
    fun getAllAirports(): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE name LIKE '%' || :search || '%' " +
            "OR iata_code  LIKE '%' || :search || '%'")
    fun searchAirportsByNameStream(search: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE name != :name AND name IS NOT NULL AND LENGTH(name) > 0 ORDER BY passengers DESC")
    fun getAllAirportsExcept(name: String): Flow<List<Airport>>

    @Query(
        "SELECT departure_code AS departureCode, name AS name " +
                "FROM favorite, airport " +
                "WHERE favorite.departure_code = airport.iata_code"
    )
    fun getDepartureCode(): Flow<List<IataWithName>>

    @Query(
        "SELECT destination_code AS destinationCode, name AS name " +
                "FROM favorite, airport " +
                "WHERE favorite.destination_code = airport.iata_code"
    )
    fun getDestinationCode(): Flow<List<IataWithName2>>

    // Favorite table
    @Query("SELECT * FROM favorite WHERE id IS NOT NULL ORDER BY id DESC ")
    fun getAllBookmarks(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun bookmark(favorite: Favorite)

    @Query("DELETE FROM favorite WHERE destination_code = :destinationCode AND departure_code = :departureCode")
    suspend fun delete(destinationCode: String, departureCode: String)

    @Query("SELECT COUNT(*) FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
    fun findFavorite(departureCode: String, destinationCode: String): Flow<Int>
}

data class IataWithName(
    val departureCode: String,
    val name: String
)

data class IataWithName2(
    val destinationCode: String,
    val name: String
)