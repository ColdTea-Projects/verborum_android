package de.coldtea.verborum.core.push.data.api

import de.coldtea.verborum.core.push.data.api.model.DeviceRegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * ms_notification (`:8088`) — where this install's push address is registered
 * (`docs/push-notifications-client-guide.md`). Every call needs the bearer token and acts on the
 * token subject's own devices; there is deliberately no read endpoint.
 */
interface NotificationApi {

    /** Idempotent upsert. **403** = this `deviceId` belongs to another account and the token differs. */
    @PUT("notifications/devices")
    suspend fun registerDevice(@Body body: DeviceRegistrationRequest): Response<Unit>

    /** 200, also when the id is absent or not ours. Called on logout. */
    @DELETE("notifications/devices/{deviceId}")
    suspend fun removeDevice(@Path("deviceId") deviceId: String): Response<Unit>
}
