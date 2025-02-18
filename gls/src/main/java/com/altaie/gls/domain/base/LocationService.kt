package com.altaie.gls.domain.base

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresPermission
import com.altaie.prettycode.core.base.Resource
import com.altaie.gls.domain.entities.ServiceFailure
import com.altaie.gls.utils.LocationRequestDefaults
import com.altaie.gls.utils.extenstions.isGpsProviderEnabled
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import kotlin.time.Duration


internal interface LocationService {
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    suspend fun getLastLocation(): Resource<Location>

    fun requestLocationUpdatesAsFlow(): Flow<Resource<Location>>
    suspend fun requestLocationUpdates(timeout: Duration = LocationRequestDefaults.UPDATES_TIMEOUT): Resource<List<Location>>
    fun removeLocationUpdates()

    /**
     * @param maxUpdates numUpdates
     * @param intervalMillis interval
     * @param minUpdateIntervalMillis fastestInterval
     * @param maxUpdateDelayMillis maxWaitTime
     * @param minUpdateDistanceMeters smallestDisplacement
     * */
    fun configureLocationRequest(
        priority: Int = Priority.PRIORITY_HIGH_ACCURACY,
        intervalMillis: Long = LocationRequestDefaults.UPDATE_INTERVAL_MILLIS,
        minUpdateIntervalMillis: Long = LocationRequestDefaults.MIN_UPDATE_INTERVAL_MILLIS,
        maxUpdates: Int = LocationRequestDefaults.MAX_UPDATES,
        maxUpdateDelayMillis: Long = LocationRequestDefaults.MAX_UPDATE_DELAY_MILLIS,
        minUpdateDistanceMeters: Float = LocationRequestDefaults.MIN_UPDATE_DISTANCE_METERS,
    )

    fun requestLocationSettings(resultContracts: ActivityResultLauncher<IntentSenderRequest>)

    suspend fun <T> safeCall(block: suspend () -> Resource<T>) = catchError { block() }

    fun getLocationResult(context: Context, location: Location?): Resource<Location> {
        location?.let {
            return Resource.Success(data = location)
        }

        return if (context.isGpsProviderEnabled().not())
            Resource.Fail(error = ServiceFailure.GpsProviderIsDisabled())
        else
            Resource.Fail(error = ServiceFailure.LocationNeverRecorded())
    }

    private suspend fun <T> catchError(block: suspend () -> Resource<T>) = try {
        block()
    } catch (e: Exception) {
        Timber.e(e)
        Resource.Fail(error = ServiceFailure.UnknownError(message = e.message))
    }
}
