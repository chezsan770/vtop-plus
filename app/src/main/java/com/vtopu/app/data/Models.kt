package com.vtopu.app.data

data class LoginChallenge(
    val csrf: String,
    val captchaBase64: String?
)

data class SpotlightItem(
    val title: String
)

data class UserProfile(
    val name: String?,
    val registrationNumber: String?
) {
    val greeting: String
        get() = if (name.isNullOrBlank()) "Welcome back" else "Hi, ${name.firstName()}"
}

data class AttendanceCourse(
    val code: String,
    val name: String,
    val percentage: Int
)

data class SemesterOption(
    val id: String,
    val label: String
)

object SemesterOptions {
    fun fallback(prefix: String, latestLabel: String): List<SemesterOption> =
        listOf(SemesterOption("latest-$prefix", latestLabel)) +
            (8 downTo 1).map { semester ->
                SemesterOption("$prefix-semester-$semester", "Semester $semester")
            }
}

data class NextClass(
    val name: String,
    val venue: String,
    val time: String,
    val code: String? = null,
    val slot: String? = null,
    val day: String? = null
)

data class TimetableClass(
    val name: String,
    val venue: String,
    val time: String,
    val day: String?,
    val code: String? = null,
    val slot: String? = null
)

data class DashboardSnapshot(
    val profile: UserProfile,
    val attendance: List<AttendanceCourse>,
    val nextClass: NextClass?,
    val timetable: List<TimetableClass> = emptyList(),
    val attendanceSemesters: List<SemesterOption> = emptyList(),
    val timetableSemesters: List<SemesterOption> = emptyList(),
    val selectedAttendanceSemester: SemesterOption? = null,
    val selectedTimetableSemester: SemesterOption? = null
)

sealed interface LoginResult {
    data class Success(val dashboard: DashboardSnapshot) : LoginResult
    data class Failure(val reason: String, val challenge: LoginChallenge? = null) : LoginResult
}

private fun String.firstName(): String = trim().split(Regex("\\s+")).firstOrNull().orEmpty()
