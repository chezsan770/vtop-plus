package com.vtopu.app.data

data class LoginChallenge(
    val csrf: String,
    val captchaBase64: String?
)

data class UserProfile(
    val name: String?,
    val registrationNumber: String?
) {
    val greeting: String
        get() = if (name.isNullOrBlank()) "Welcome back" else "Welcome back ${name.firstName()}"
}

data class AttendanceCourse(
    val code: String,
    val name: String,
    val percentage: Int,
    val records: List<AttendanceRecord> = emptyList(),
    val detailCourseId: String? = null,
    val detailCourseType: String? = null
)

data class AttendanceRecord(
    val date: String,
    val dayTime: String,
    val status: String,
    val slot: String = ""
)

data class GradeCourse(
    val code: String,
    val title: String,
    val courseType: String,
    val credits: String,
    val grade: String,
    val grandTotal: String? = null,
    val examMonth: String? = null,
    val resultDeclared: String? = null,
    val distribution: String? = null
)

data class MarkEntry(
    val title: String,
    val maxMark: String,
    val weightage: String,
    val status: String,
    val scoredMark: String,
    val weightedMark: String
)

data class CourseMarks(
    val code: String,
    val title: String,
    val faculty: String,
    val slot: String,
    val courseType: String,
    val assessments: List<MarkEntry>
)

data class FeatureRequestPayload(
    val title: String,
    val description: String,
    val category: String,
    val studentName: String?,
    val registrationNumber: String?,
    val appVersion: String,
    val createdAt: String
)

data class ActiveUserHeartbeat(
    val registrationNumber: String,
    val studentName: String?,
    val appVersion: String
)

data class FacultyProfile(
    val id: String,
    val name: String,
    val cabinNumber: String?,
    val mobileNumber: String?
)

data class AppUpdate(
    val versionName: String,
    val versionCode: Int,
    val title: String,
    val message: String,
    val apkUrl: String?,
    val changelogUrl: String?,
    val isForceUpdate: Boolean
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
    val grades: List<GradeCourse> = emptyList(),
    val gradeHistory: List<GradeCourse> = emptyList(),
    val marks: List<CourseMarks> = emptyList(),
    val gpa: String? = null,
    val cgpa: String? = null,
    val totalCredits: String? = null,
    val attendanceSemesters: List<SemesterOption> = emptyList(),
    val timetableSemesters: List<SemesterOption> = emptyList(),
    val gradeSemesters: List<SemesterOption> = emptyList(),
    val selectedAttendanceSemester: SemesterOption? = null,
    val selectedTimetableSemester: SemesterOption? = null,
    val selectedGradeSemester: SemesterOption? = null
)

sealed interface LoginResult {
    data class Success(val dashboard: DashboardSnapshot) : LoginResult
    data class Failure(val reason: String, val challenge: LoginChallenge? = null) : LoginResult
}

enum class SessionKeepAliveResult {
    Active,
    Expired,
    Failed
}

private fun String.firstName(): String =
    trim()
        .split(Regex("\\s+"))
        .firstOrNull()
        .orEmpty()
        .lowercase()
        .replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase() else character.toString()
        }
