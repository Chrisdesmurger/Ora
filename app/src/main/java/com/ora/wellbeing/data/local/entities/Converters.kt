package com.ora.wellbeing.data.local.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Convertisseurs de type pour Room Database
 */
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromLocalDateTime(date: LocalDateTime?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun toLocalDateTime(dateString: String?): LocalDateTime? {
        return dateString?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let {
            LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME)
        }
    }

    @TypeConverter
    fun fromTimeSlot(timeSlot: TimeSlot?): String? {
        return timeSlot?.name
    }

    @TypeConverter
    fun toTimeSlot(timeSlotString: String?): TimeSlot? {
        return timeSlotString?.let { TimeSlot.valueOf(it) }
    }

    @TypeConverter
    fun fromExperienceLevel(experienceLevel: ExperienceLevel?): String? {
        return experienceLevel?.name
    }

    @TypeConverter
    fun toExperienceLevel(experienceLevelString: String?): ExperienceLevel? {
        return experienceLevelString?.let { ExperienceLevel.valueOf(it) }
    }

    @TypeConverter
    fun fromContentType(contentType: ContentType?): String? {
        return contentType?.name
    }

    @TypeConverter
    fun toContentType(contentTypeString: String?): ContentType? {
        return contentTypeString?.let { ContentType.valueOf(it) }
    }

    @TypeConverter
    fun fromCategory(category: Category?): String? {
        return category?.name
    }

    @TypeConverter
    fun toCategory(categoryString: String?): Category? {
        return categoryString?.let { Category.valueOf(it) }
    }

    @TypeConverter
    fun fromMood(mood: Mood?): String? {
        return mood?.name
    }

    @TypeConverter
    fun toMood(moodString: String?): Mood? {
        return moodString?.let { Mood.valueOf(it) }
    }

    @TypeConverter
    fun fromSessionType(sessionType: SessionType?): String? {
        return sessionType?.name
    }

    @TypeConverter
    fun toSessionType(sessionTypeString: String?): SessionType? {
        return sessionTypeString?.let { SessionType.valueOf(it) }
    }

    @TypeConverter
    fun fromNotificationType(notificationType: NotificationType?): String? {
        return notificationType?.name
    }

    @TypeConverter
    fun toNotificationType(notificationTypeString: String?): NotificationType? {
        return notificationTypeString?.let { NotificationType.valueOf(it) }
    }

    @TypeConverter
    fun fromNotificationFrequency(notificationFrequency: NotificationFrequency?): String? {
        return notificationFrequency?.name
    }

    @TypeConverter
    fun toNotificationFrequency(notificationFrequencyString: String?): NotificationFrequency? {
        return notificationFrequencyString?.let { NotificationFrequency.valueOf(it) }
    }
}