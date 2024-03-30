package com.altaie.gls.data

import android.annotation.SuppressLint
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.altaie.prettycode.core.base.Resource
import com.altaie.gls.domain.base.LocationRepository
import com.altaie.gls.domain.base.LocationService
import com.altaie.gls.domain.entities.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

@SuppressLint("MissingPermission")
internal class LocationRepositoryImpl(private val service: LocationService) : LocationRepository {
    override fun lastLocationAsFlow(): Flow<Resource<Location>> = wrapWithFlow(service::getLastLocation)

    override suspend fun lastLocation(): Resource<Location> = service.getLastLocation()

    override fun requestLocationUpdatesAsFlow(): Flow<Resource<Location>> =
        service.requestLocationUpdatesAsFlow()

    override suspend fun requestLocationUpdates(timeout: Duration): Resource<List<Location>> =
        service.requestLocationUpdates(timeout = timeout)

    override fun removeLocationUpdates() = service.removeLocationUpdates()

    override fun configureLocationRequest(
        priority: Priority,
        intervalMillis: Long,
        minUpdateIntervalMillis: Long,
        maxUpdates: Int,
        maxUpdateDelayMillis: Long,
        minUpdateDistanceMeters: Float,
    ) {
        service.configureLocationRequest(
            priority = priority.value,
            intervalMillis = intervalMillis,
            maxUpdates = maxUpdates,
            minUpdateIntervalMillis = minUpdateIntervalMillis,
            maxUpdateDelayMillis = maxUpdateDelayMillis,
            minUpdateDistanceMeters = minUpdateDistanceMeters
        )
    }

    override fun requestLocationSettings(resultContracts: ActivityResultLauncher<IntentSenderRequest>) =
        service.requestLocationSettings(resultContracts = resultContracts)

    private fun <T> wrapWithFlow(block: suspend () -> Resource<T>): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        emit(block())
    }
}
