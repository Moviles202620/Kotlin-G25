package com.example.goatly.data.network

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.TimeZone

// Sprint 3: Feature Calendar Sync — START
// Manages inserting job offer events into the device's native calendar.
// If no calendar is available or permission is missing, the event is queued
// in SharedPreferences and synced automatically when connectivity is restored.

data class PendingCalendarEvent(
    val offerId: String,
    val title: String,
    val dateTimeMillis: Long,
    val durationHours: Int,
    val location: String
)

object CalendarSyncManager {

    private const val PREFS_NAME = "goatly_calendar_prefs"
    private const val KEY_PENDING_EVENTS = "pending_calendar_events"
    private const val KEY_SYNCED_OFFER_IDS = "synced_offer_ids"

    // Tries to insert the event directly into the device calendar.
    // If it fails, queues it in SharedPreferences for later sync.
    fun addOfferToCalendar(
        context: Context,
        offerId: String,
        title: String,
        dateTimeMillis: Long,
        durationHours: Int,
        location: String
    ): Boolean {
        return try {
            val calendarId = getPrimaryCalendarId(context)
            if (calendarId == null) {
                Log.w("CalendarSync", "No calendar found — queuing event for later")
                enqueuePendingEvent(context, offerId, title, dateTimeMillis, durationHours, location)
                return false
            }

            val endTimeMillis = dateTimeMillis + durationHours * 60 * 60 * 1000L

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, "Goatly: $title")
                put(CalendarContract.Events.DTSTART, dateTimeMillis)
                put(CalendarContract.Events.DTEND, endTimeMillis)
                put(CalendarContract.Events.EVENT_LOCATION, location)
                put(CalendarContract.Events.DESCRIPTION, "Oferta de trabajo ocasional de Goatly")
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                markAsSynced(context, offerId)
                Log.d("CalendarSync", "Event inserted successfully for offer $offerId")
                true
            } else {
                enqueuePendingEvent(context, offerId, title, dateTimeMillis, durationHours, location)
                false
            }
        } catch (e: Exception) {
            Log.e("CalendarSync", "Error inserting event: ${e.message}")
            enqueuePendingEvent(context, offerId, title, dateTimeMillis, durationHours, location)
            false
        }
    }

    // Saves the event to SharedPreferences when calendar insert fails
    private fun enqueuePendingEvent(
        context: Context,
        offerId: String,
        title: String,
        dateTimeMillis: Long,
        durationHours: Int,
        location: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = getPendingEvents(context).toMutableList()
        if (existing.none { it.offerId == offerId }) {
            existing.add(PendingCalendarEvent(offerId, title, dateTimeMillis, durationHours, location))
            prefs.edit().putString(KEY_PENDING_EVENTS, serializeEvents(existing)).apply()
            Log.d("CalendarSync", "Event queued for later sync: $offerId")
        }
    }

    // Called when connectivity is restored — tries to insert all queued events
    fun syncPendingEvents(context: Context) {
        val pending = getPendingEvents(context).toMutableList()
        if (pending.isEmpty()) return

        Log.d("CalendarSync", "Syncing ${pending.size} pending calendar events")
        val stillPending = mutableListOf<PendingCalendarEvent>()

        pending.forEach { event ->
            val success = addOfferToCalendar(
                context = context,
                offerId = event.offerId,
                title = event.title,
                dateTimeMillis = event.dateTimeMillis,
                durationHours = event.durationHours,
                location = event.location
            )
            if (!success) stillPending.add(event)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PENDING_EVENTS, serializeEvents(stillPending)).apply()
        Log.d("CalendarSync", "Sync complete. Still pending: ${stillPending.size}")
    }

    // Returns true if the offer has already been added to the calendar (used for BQ indicator)
    fun isOfferSynced(context: Context, offerId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val synced = prefs.getStringSet(KEY_SYNCED_OFFER_IDS, emptySet()) ?: emptySet()
        return synced.contains(offerId)
    }

    fun isOfferPending(context: Context, offerId: String): Boolean {
        return getPendingEvents(context).any { it.offerId == offerId }
    }

    private fun markAsSynced(context: Context, offerId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val synced = prefs.getStringSet(KEY_SYNCED_OFFER_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        synced.add(offerId)
        prefs.edit().putStringSet(KEY_SYNCED_OFFER_IDS, synced).apply()
    }

    private fun getPendingEvents(context: Context): List<PendingCalendarEvent> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PENDING_EVENTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                PendingCalendarEvent(
                    offerId = obj.getString("offerId"),
                    title = obj.getString("title"),
                    dateTimeMillis = obj.getLong("dateTimeMillis"),
                    durationHours = obj.getInt("durationHours"),
                    location = obj.getString("location")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeEvents(events: List<PendingCalendarEvent>): String {
        val array = JSONArray()
        events.forEach { event ->
            val obj = JSONObject().apply {
                put("offerId", event.offerId)
                put("title", event.title)
                put("dateTimeMillis", event.dateTimeMillis)
                put("durationHours", event.durationHours)
                put("location", event.location)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun getPrimaryCalendarId(context: Context): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
        return try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, null, null, null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val isPrimary = cursor.getInt(1)
                    if (isPrimary == 1) return id
                }
                cursor.moveToFirst()
                if (cursor.count > 0) cursor.getLong(0) else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
// Sprint 3: Feature Calendar Sync — END