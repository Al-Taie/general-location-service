package com.altaie.gls.data


import com.altaie.gls.utils.LocationRequestDefaults
import com.google.android.gms.location.Priority
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest as LocationRequestGoogle
import com.huawei.hms.location.LocationRequest as LocationRequestHuawei


internal sealed class LocationRequestProvider<out T> {
    class Google(
        val intervalMillis: Long = LocationRequestDefaults.UPDATE_INTERVAL_MILLIS,
        val priority: Int = Priority.PRIORITY_HIGH_ACCURACY,
        val minUpdateIntervalMillis: Long = LocationRequestDefaults.MIN_UPDATE_INTERVAL_MILLIS,
        val maxUpdates: Int = LocationRequestDefaults.MAX_UPDATES,
        val maxUpdateDelayMillis: Long = LocationRequestDefaults.MAX_UPDATE_DELAY_MILLIS,
        val minUpdateDistanceMeters: Float = LocationRequestDefaults.MIN_UPDATE_DISTANCE_METERS,
    ) : LocationRequestProvider<LocationRequestGoogle>() {
        val locationRequest: LocationRequestGoogle = LocationRequestGoogle
            .Builder(intervalMillis)
            .setPriority(priority)
            .setMinUpdateIntervalMillis(minUpdateIntervalMillis)
            .setMaxUpdates(maxUpdates)
            .setMaxUpdateDelayMillis(maxUpdateDelayMillis)
            .setMinUpdateDistanceMeters(minUpdateDistanceMeters)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setWaitForAccurateLocation(true)
            .build()
    }

    class Huawei(
        val intervalMillis: Long = LocationRequestDefaults.UPDATE_INTERVAL_MILLIS,
        val priority: Int = Priority.PRIORITY_HIGH_ACCURACY,
        val minUpdateIntervalMillis: Long = LocationRequestDefaults.MIN_UPDATE_INTERVAL_MILLIS,
        val maxUpdates: Int = LocationRequestDefaults.MAX_UPDATES,
        val maxUpdateDelayMillis: Long = LocationRequestDefaults.MAX_UPDATE_DELAY_MILLIS,
        val minUpdateDistanceMeters: Float = LocationRequestDefaults.MIN_UPDATE_DISTANCE_METERS,
    ) : LocationRequestProvider<LocationRequestHuawei>() {
        val locationRequest: LocationRequestHuawei = LocationRequestHuawei.create().apply {
            this.priority = priority
            interval = intervalMillis
            fastestInterval = minUpdateIntervalMillis
            numUpdates = maxUpdates
            maxWaitTime = maxUpdateDelayMillis
            smallestDisplacement = minUpdateDistanceMeters
        }
    }
}
