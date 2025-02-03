package com.altaie.gls.data.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.altaie.gls.data.LocationRequestProvider
import com.altaie.gls.domain.base.LocationService
import com.altaie.gls.domain.entities.ServiceFailure
import com.altaie.gls.utils.extenstions.isEqual
import com.altaie.gls.utils.extenstions.isGpsProviderEnabled
import com.altaie.prettycode.core.base.Resource
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.time.Duration


@SuppressLint("MissingPermission")
internal class GoogleService(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private var locationRequest: LocationRequest
) : LocationService {
    private lateinit var locationCallback: LocationCallback

    override suspend fun getLastLocation(): Resource<Location> = safeCall {
        val location = fusedLocationClient.lastLocation.await()
        getLocationResult(context = context, location = location)
    }

    override fun requestLocationUpdatesAsFlow(): Flow<Resource<Location>> =
        callbackFlow {
            trySend(Resource.Loading)

            locationCallback = createLocationCallback { location ->
                trySendBlocking(Resource.Success(data = location))
                    .onFailure { trySendBlocking(Resource.Fail(error = it.toServiceFailure())) }
            }

            startUpdates { error -> trySend(Resource.Fail(error = error)) }

            awaitClose { removeLocationUpdates() }
        }.distinctUntilChanged { old, new ->
            old.toData.isEqual(new.toData)
        }.buffer(Channel.UNLIMITED)

    override suspend fun requestLocationUpdates(timeout: Duration): Resource<List<Location>> =
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                val locations: MutableList<Location> = mutableListOf()

                locationCallback = createLocationCallback { location ->
                    locations.add(location)
                    if (locations.size >= locationRequest.maxUpdates) {
                        removeLocationUpdates()
                        continuation.resume(Resource.Success(data = locations))
                    }
                }

                startUpdates { error -> continuation.resume(Resource.Fail(error = error)) }

                continuation.invokeOnCancellation { removeLocationUpdates() }
            }
        }

    override fun removeLocationUpdates() {
        if (::locationCallback.isInitialized.not()) return
        fusedLocationClient.flushLocations()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun configureLocationRequest(
        priority: Int,
        intervalMillis: Long,
        minUpdateIntervalMillis: Long,
        maxUpdates: Int,
        maxUpdateDelayMillis: Long,
        minUpdateDistanceMeters: Float,
    ) {
        locationRequest = LocationRequestProvider.Google(
            priority = priority,
            maxUpdates = maxUpdates,
            intervalMillis = intervalMillis,
            maxUpdateDelayMillis = maxUpdateDelayMillis,
            minUpdateIntervalMillis = minUpdateIntervalMillis,
            minUpdateDistanceMeters = minUpdateDistanceMeters
        ).locationRequest
    }

    override fun requestLocationSettings(resultContracts: ActivityResultLauncher<IntentSenderRequest>) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        LocationServices.getSettingsClient(context)
            .checkLocationSettings(builder.build())
            .addOnFailureListener { exception ->
                handleLocationSettingsFailure(exception, resultContracts)
            }
    }

    private fun handleLocationSettingsFailure(
        exception: Exception,
        resultContracts: ActivityResultLauncher<IntentSenderRequest>
    ) {
        if (exception is ResolvableApiException)
            runCatching {
                val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                resultContracts.launch(intentSenderRequest)
            }.onFailure {
                Timber.d("Failed to launch intent sender: ${it.message}")
            }
        else
            Timber.d("Location settings request failed with unknown exception: ${exception.message}")
    }

    private fun createLocationCallback(onLocationResult: (Location) -> Unit): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.locations.minBy { it.accuracy }
                onLocationResult(location)
            }
        }
    }

    private fun startUpdates(onFailure: (ServiceFailure) -> Unit) = runCatching {
        if (context.isGpsProviderEnabled())
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        else
            onFailure(ServiceFailure.GpsProviderIsDisabled())
    }.onFailure { onFailure(it.toServiceFailure()) }

    private fun Throwable?.toServiceFailure(): ServiceFailure = when (this) {
        is ResolvableApiException -> ServiceFailure.LocationServiceNotFound()
        null -> ServiceFailure.UnknownError()
        else -> ServiceFailure.UnknownError(message = message)
    }
}
