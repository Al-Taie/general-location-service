package com.altaie.gls.data.services

import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.altaie.prettycode.core.base.Resource
import com.altaie.gls.domain.base.LocationService
import com.altaie.gls.domain.entities.ServiceFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration


internal class NoneService : LocationService {
    override suspend fun getLastLocation(): Resource<Location> =
        Resource.Fail(error = ServiceFailure.LocationServiceNotFound())

    override suspend fun requestLocationUpdates(timeout: Duration): Resource<List<Location>> =
        Resource.Fail(error = ServiceFailure.LocationServiceNotFound())

    override fun requestLocationUpdatesAsFlow(): Flow<Resource<Location>> =
        flow { emit(Resource.Fail(error = ServiceFailure.LocationServiceNotFound())) }

    override fun removeLocationUpdates() = Unit

    override fun configureLocationRequest(
        priority: Int,
        intervalMillis: Long,
        minUpdateIntervalMillis: Long,
        maxUpdates: Int,
        maxUpdateDelayMillis: Long,
        minUpdateDistanceMeters: Float,
    ) = Unit

    override fun requestLocationSettings(resultContracts: ActivityResultLauncher<IntentSenderRequest>) = Unit
}
