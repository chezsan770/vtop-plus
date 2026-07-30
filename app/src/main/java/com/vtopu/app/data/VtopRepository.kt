package com.vtopu.app.data

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class VtopRepository(context: Context) {
    private val baseUrl = "https://vtop.vitbhopal.ac.in"
    private val cookieJar = PersistentCookieJar(SessionCookieStore(context.applicationContext))
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private var loginChallenge: LoginChallenge? = null
    private var lastDashboardHtml: String? = null
    private var lastAttendanceHtml: String? = null
    private var lastTimetableHtml: String? = null
    private var lastMarksHtml: String? = null
    private var lastGradesHtml: String? = null
    private var lastGradeHistoryHtml: String? = null
    private var lastProfileHtml: String? = null
    private var lastProctorHtml: String? = null
    private var lastStudentPhotoBase64: String? = null
    private var lastProctorPhotoBase64: String? = null
    private var lastResponseUrl: String? = null
    private var authenticatedPortalUrl: String? = null
    private var selectedAttendanceSemesterId: String? = null
    private var selectedTimetableSemesterId: String? = null
    private var selectedGradeSemesterId: String? = null
    private var currentAuthorizedId: String? = null
    private var currentCsrf: String? = null

    suspend fun prepareLogin(): LoginChallenge? = withContext(Dispatchers.IO) {
        runCatching {
            resetSessionState(clearCookies = true)
            val openHtml = get("/vtop/login")
            val openCsrf = VtopParser.parseCsrf(openHtml) ?: return@runCatching null
            val setupHtml = post("/vtop/prelogin/setup", mapOf("_csrf" to openCsrf, "flag" to "VTOP"))
            val loginHtml = setupHtml.takeIf { it.contains("vtopLoginForm", ignoreCase = true) }
                ?: get("/vtop/init/page")
            val challenge = LoginChallenge(
                csrf = VtopParser.parseCsrf(loginHtml).orEmpty(),
                captchaBase64 = VtopParser.parseCaptchaBase64(loginHtml)
            )
            val captchaReadyChallenge = if (challenge.captchaBase64 == null) {
                val captchaHtml = get("/vtop/get/new/captcha")
                challenge.copy(captchaBase64 = VtopParser.parseCaptchaBase64(captchaHtml))
            } else {
                challenge
            }
            LoginChallenge(
                csrf = captchaReadyChallenge.csrf,
                captchaBase64 = captchaReadyChallenge.captchaBase64
            ).also { loginChallenge = it }
        }.getOrNull()
    }

    suspend fun refreshCaptcha(): LoginChallenge? = withContext(Dispatchers.IO) {
        runCatching {
            val current = loginChallenge ?: prepareLogin()
            val html = get("/vtop/get/new/captcha")
            current?.copy(captchaBase64 = VtopParser.parseCaptchaBase64(html))
                ?.also { loginChallenge = it }
        }.getOrNull()
    }

    suspend fun login(username: String, password: String, captcha: String): LoginResult = withContext(Dispatchers.IO) {
        runCatching {
            val challenge = loginChallenge ?: prepareLogin()
            if (challenge == null || challenge.csrf.isBlank()) {
                return@withContext LoginResult.Failure("Could not start a VTOP login session. Try again.")
            }

            val html = post(
                path = "/vtop/login",
                fields = mapOf(
                    "_csrf" to challenge.csrf,
                    "username" to username.uppercase(),
                    "password" to password,
                    "captchaStr" to captcha.uppercase()
                )
            )

            if (html.contains("vtopLoginForm", ignoreCase = true) ||
                html.contains("Invalid", ignoreCase = true) ||
                html.contains("CAPTCHA", ignoreCase = true)
            ) {
                val reason = VtopParser.parseLoginError(html)
                    ?: "VTOP rejected the login. Re-enter the new CAPTCHA and try again."
                resetSessionState(clearCookies = true)
                val nextChallenge = prepareLogin()
                return@withContext LoginResult.Failure(reason, nextChallenge)
            }

            lastDashboardHtml = html
            currentAuthorizedId = username.uppercase()
            currentCsrf = VtopParser.parseCsrf(html) ?: challenge.csrf
            authenticatedPortalUrl = lastResponseUrl?.takeUnless { it.contains("/vtop/login", ignoreCase = true) }
            rememberPage(html)
            fetchAcademicData()
            val dashboard = buildDashboard()
            selectedAttendanceSemesterId = dashboard.selectedAttendanceSemester?.id
            selectedTimetableSemesterId = dashboard.selectedTimetableSemester?.id
            selectedGradeSemesterId = dashboard.selectedGradeSemester?.id
            LoginResult.Success(dashboard)
        }.getOrElse { error ->
            resetSessionState(clearCookies = true)
            LoginResult.Failure(error.message ?: "VTOP login failed.")
        }
    }

    suspend fun restoreSession(savedUsername: String?): DashboardSnapshot? = withContext(Dispatchers.IO) {
        if (!cookieJar.hasCookies()) return@withContext null
        runCatching {
            val html = get("/vtop/content")
            if (isLoginPage(html)) {
                resetSessionState(clearCookies = true)
                return@withContext null
            }

            lastDashboardHtml = html
            currentCsrf = VtopParser.parseCsrf(html) ?: currentCsrf
            authenticatedPortalUrl = lastResponseUrl?.takeUnless { it.contains("/vtop/login", ignoreCase = true) }
            rememberPage(html)

            val profile = parseSnapshot(html).profile
            currentAuthorizedId = savedUsername?.uppercase()
                ?: profile.registrationNumber?.uppercase()
            fetchAcademicData()
            buildDashboard().also { dashboard ->
                selectedAttendanceSemesterId = dashboard.selectedAttendanceSemester?.id
                selectedTimetableSemesterId = dashboard.selectedTimetableSemester?.id
                selectedGradeSemesterId = dashboard.selectedGradeSemester?.id
            }
        }.getOrNull()
    }

    suspend fun refreshDashboard(): DashboardSnapshot = withContext(Dispatchers.IO) {
        val html = runCatching { get("/vtop/content") }
            .getOrElse { lastDashboardHtml.orEmpty() }
        lastDashboardHtml = html.ifBlank { lastDashboardHtml }
        currentCsrf = VtopParser.parseCsrf(lastDashboardHtml.orEmpty()) ?: currentCsrf
        rememberPage(lastDashboardHtml.orEmpty())
        fetchAcademicData()
        buildDashboard().also { dashboard ->
            selectedAttendanceSemesterId = dashboard.selectedAttendanceSemester?.id
            selectedTimetableSemesterId = dashboard.selectedTimetableSemester?.id
            selectedGradeSemesterId = dashboard.selectedGradeSemester?.id
        }
    }

    suspend fun keepSessionAlive(): SessionKeepAliveResult = withContext(Dispatchers.IO) {
        runCatching {
            val html = get("/vtop/content")
            if (isLoginPage(html)) {
                resetSessionState(clearCookies = true)
                SessionKeepAliveResult.Expired
            } else {
                lastDashboardHtml = html.ifBlank { lastDashboardHtml }
                currentCsrf = VtopParser.parseCsrf(html) ?: currentCsrf
                authenticatedPortalUrl = lastResponseUrl?.takeUnless { it.contains("/vtop/login", ignoreCase = true) }
                    ?: authenticatedPortalUrl
                rememberPage(html)
                if (currentAuthorizedId.isNullOrBlank()) {
                    currentAuthorizedId = parseSnapshot(html).profile.registrationNumber?.uppercase()
                }
                SessionKeepAliveResult.Active
            }
        }.getOrDefault(SessionKeepAliveResult.Failed)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        runCatching { get("/vtop/logout") }
        resetSessionState(clearCookies = true)
    }

    suspend fun selectSemesters(
        attendanceSemesterId: String,
        timetableSemesterId: String
    ): DashboardSnapshot = withContext(Dispatchers.IO) {
        selectedAttendanceSemesterId = attendanceSemesterId
        selectedTimetableSemesterId = timetableSemesterId
        selectedGradeSemesterId = attendanceSemesterId
        fetchAcademicData()
        buildDashboard()
    }

    fun cookiesForWebView(): List<String> {
        val url = "$baseUrl/vtop/content".toHttpUrl()
        return cookieJar.loadForRequest(url).map { cookie ->
            buildString {
                append(cookie.name)
                append('=')
                append(cookie.value)
                append("; Domain=")
                append(cookie.domain)
                append("; Path=")
                append(cookie.path)
                if (cookie.secure) append("; Secure")
            }
        }
    }

    fun portalStartUrl(): String =
        authenticatedPortalUrl ?: "$baseUrl/vtop/content"

    fun portalHomeUrl(): String =
        "$baseUrl/vtop/content"

    private fun isLoginPage(html: String): Boolean =
        html.contains("vtopLoginForm", ignoreCase = true) ||
            html.contains("captchaStr", ignoreCase = true) ||
            lastResponseUrl?.contains("/vtop/login", ignoreCase = true) == true

    private fun resetSessionState(clearCookies: Boolean) {
        if (clearCookies) cookieJar.clear()
        loginChallenge = null
        lastDashboardHtml = null
        lastAttendanceHtml = null
        lastTimetableHtml = null
        lastMarksHtml = null
        lastGradesHtml = null
        lastGradeHistoryHtml = null
        lastProfileHtml = null
        lastProctorHtml = null
        lastStudentPhotoBase64 = null
        lastProctorPhotoBase64 = null
        lastResponseUrl = null
        authenticatedPortalUrl = null
        selectedAttendanceSemesterId = null
        selectedTimetableSemesterId = null
        selectedGradeSemesterId = null
        currentAuthorizedId = null
        currentCsrf = null
    }

    private fun get(path: String): String {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .header("User-Agent", userAgent)
            .get()
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            lastResponseUrl = response.request.url.toString()
            response.body?.string().orEmpty()
        }
    }

    private fun post(path: String, fields: Map<String, String>, ajax: Boolean = false): String {
        val bodyBuilder = FormBody.Builder()
        fields.forEach { (name, value) -> bodyBuilder.add(name, value) }
        val requestBuilder = Request.Builder()
            .url("$baseUrl$path")
            .header("User-Agent", userAgent)
            .post(bodyBuilder.build())
        if (ajax) {
            requestBuilder.header("X-Requested-With", "XMLHttpRequest")
        }
        val request = requestBuilder.build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            lastResponseUrl = response.request.url.toString()
            response.body?.string().orEmpty()
        }
    }

    private fun postAjax(path: String, fields: Map<String, String>): String =
        post(path = path, fields = fields, ajax = true)

    private companion object {
        const val userAgent = "Mozilla/5.0 (Linux; Android 14) VTOP-U/1.0"
        const val MAX_PROFILE_IMAGE_BYTES = 2L * 1024L * 1024L
    }

    private suspend fun fetchAcademicData() = coroutineScope {
        val authorizedId = currentAuthorizedId ?: return@coroutineScope
        val csrf = currentCsrf ?: return@coroutineScope

        val attendanceMenuDeferred = async {
            runCatching {
                postAjax(
                    path = "/vtop/academics/common/StudentAttendance",
                    fields = menuFields(authorizedId, csrf)
                )
            }
        }
        val timetableMenuDeferred = async {
            runCatching {
                postAjax(
                    path = "/vtop/academics/common/StudentTimeTable",
                    fields = menuFields(authorizedId, csrf)
                )
            }
        }
        val marksMenuDeferred = async {
            runCatching {
                postAjax(
                    path = "/vtop/examinations/StudentMarkView",
                    fields = menuFields(authorizedId, csrf)
                )
            }
        }
        val gradesMenuDeferred = async {
            runCatching {
                postAjax(
                    path = "/vtop/examinations/examGradeView/StudentGradeView",
                    fields = menuFields(authorizedId, csrf)
                )
            }
        }
        val gradeHistoryDeferred = async {
            runCatching {
                postAjax(
                    path = "/vtop/examinations/examGradeView/StudentGradeHistory",
                    fields = menuFields(authorizedId, csrf)
                )
            }
        }
        val profileDeferred = async {
            runCatching {
                postAjax(
                    path = "/vtop/studentsRecord/StudentProfileAllView",
                    fields = menuFields(authorizedId, csrf)
                )
            }
        }
        val proctorDeferred = async {
            runCatching {
                postAjax(
                    path = "/vtop/proctor/viewProctorDetails",
                    fields = menuFields(authorizedId, csrf) + mapOf(
                        "winImage" to VtopParser.parseInputValue(
                            lastDashboardHtml.orEmpty(),
                            "winImage"
                        ).orEmpty()
                    )
                )
            }
        }

        val attendanceMenu = attendanceMenuDeferred.await().getOrNull()
        val timetableMenu = timetableMenuDeferred.await().getOrNull()
        val marksMenu = marksMenuDeferred.await().getOrNull()
        val gradesMenu = gradesMenuDeferred.await().getOrNull()
        val profileHtml = profileDeferred.await().getOrNull()
        val proctorHtml = proctorDeferred.await().getOrNull()

        profileHtml?.let { html ->
            lastProfileHtml = html
            lastStudentPhotoBase64 = fetchImageBase64(
                VtopParser.parseStudentPhotoSource(html)
            ) ?: lastStudentPhotoBase64
        }
        proctorHtml?.let { html ->
            lastProctorHtml = html
            lastProctorPhotoBase64 = fetchImageBase64(
                VtopParser.parseProctorPhotoSource(html)
            ) ?: lastProctorPhotoBase64
        }

        val attendanceOptions = attendanceMenu?.let(VtopParser::parseAttendanceSemesters).orEmpty()
        val timetableOptions = timetableMenu?.let(VtopParser::parseTimetableSemesters).orEmpty()
        val markOptions = marksMenu?.let(VtopParser::parseGradeSemesters).orEmpty()
        val gradeOptions = gradesMenu?.let(VtopParser::parseGradeSemesters).orEmpty()

        val nextAttendanceSemesterId = selectedAttendanceSemesterId
            ?.takeIf { selected -> attendanceOptions.any { it.id == selected } }
            ?: attendanceOptions.firstOrNull()?.id
        val nextTimetableSemesterId = selectedTimetableSemesterId
            ?.takeIf { selected -> timetableOptions.any { it.id == selected } }
            ?: timetableOptions.firstOrNull()?.id
        val nextMarksSemesterId = selectedGradeSemesterId
            ?.takeIf { selected -> markOptions.any { it.id == selected } }
            ?: nextAttendanceSemesterId?.takeIf { selected -> markOptions.any { it.id == selected } }
            ?: markOptions.firstOrNull()?.id
        val nextGradesSemesterId = selectedGradeSemesterId
            ?.takeIf { selected -> gradeOptions.any { it.id == selected } }
            ?: nextAttendanceSemesterId?.takeIf { selected -> gradeOptions.any { it.id == selected } }
            ?: nextMarksSemesterId?.takeIf { selected -> gradeOptions.any { it.id == selected } }
            ?: gradeOptions.firstOrNull()?.id

        val attendanceDetailDeferred = async {
            runCatching {
                nextAttendanceSemesterId?.let { semesterId ->
                    postAjax(
                        path = "/vtop/processViewStudentAttendance",
                        fields = semesterFields(authorizedId, csrf, semesterId)
                    )
                }.orEmpty()
            }
        }
        val timetableDetailDeferred = async {
            runCatching {
                nextTimetableSemesterId?.let { semesterId ->
                    postAjax(
                        path = "/vtop/processViewTimeTable",
                        fields = semesterFields(authorizedId, csrf, semesterId)
                    )
                }.orEmpty()
            }
        }
        val marksDetailDeferred = async {
            runCatching {
                nextMarksSemesterId?.let { semesterId ->
                    postAjax(
                        path = "/vtop/examinations/doStudentMarkView",
                        fields = semesterFields(authorizedId, csrf, semesterId)
                    )
                }.orEmpty()
            }
        }
        val gradesDetailDeferred = async {
            runCatching {
                nextGradesSemesterId?.let { semesterId ->
                    postAjax(
                        path = "/vtop/examinations/examGradeView/doStudentGradeView",
                        fields = semesterFields(authorizedId, csrf, semesterId)
                    )
                }.orEmpty()
            }
        }

        attendanceMenu?.let { menu ->
            selectedAttendanceSemesterId = nextAttendanceSemesterId
            val attendanceDetailHtml = attendanceDetailDeferred.await().getOrDefault("")
            val attendanceCourses = parseSnapshot(menu + attendanceDetailHtml).attendance
            val attendanceRecordHtml = nextAttendanceSemesterId?.let { semesterId ->
                attendanceCourses
                    .filter { course -> !course.detailCourseId.isNullOrBlank() && !course.detailCourseType.isNullOrBlank() }
                    .map { course ->
                        async {
                            runCatching {
                                postAjax(
                                    path = "/vtop/processViewAttendanceDetail",
                                    fields = attendanceDetailFields(
                                        authorizedId = authorizedId,
                                        csrf = csrf,
                                        semesterId = semesterId,
                                        registerNumber = authorizedId,
                                        courseId = course.detailCourseId.orEmpty(),
                                        courseType = course.detailCourseType.orEmpty()
                                    )
                                )
                            }.getOrDefault("")
                        }
                    }
                    .map { it.await() }
                    .joinToString("")
            }.orEmpty()
            lastAttendanceHtml = menu + attendanceDetailHtml + attendanceRecordHtml
        }
        timetableMenu?.let { menu ->
            selectedTimetableSemesterId = nextTimetableSemesterId
            lastTimetableHtml = menu + timetableDetailDeferred.await().getOrDefault("")
        }
        marksMenu?.let { menu ->
            if (gradesMenu == null) {
                selectedGradeSemesterId = nextMarksSemesterId
            }
            lastMarksHtml = menu + marksDetailDeferred.await().getOrDefault("")
        }
        gradesMenu?.let { menu ->
            selectedGradeSemesterId = nextGradesSemesterId ?: nextMarksSemesterId
            lastGradesHtml = menu + gradesDetailDeferred.await().getOrDefault("")
        }
        gradeHistoryDeferred.await().getOrNull()?.let { history ->
            lastGradeHistoryHtml = history
        }
    }

    private fun menuFields(authorizedId: String, csrf: String): Map<String, String> =
        mapOf(
            "verifyMenu" to "true",
            "authorizedID" to authorizedId,
            "_csrf" to csrf,
            "nocache" to System.currentTimeMillis().toString()
        )

    private fun semesterFields(authorizedId: String, csrf: String, semesterId: String): Map<String, String> =
        mapOf(
            "_csrf" to csrf,
            "semesterSubId" to semesterId,
            "authorizedID" to authorizedId,
            "x" to utcRequestTimestamp()
        )

    private fun fetchImageBase64(source: String?): String? {
        if (source.isNullOrBlank()) return null
        if (source.startsWith("data:image", ignoreCase = true)) {
            return source.substringAfter("base64,", missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
        }

        val imageUrl = when {
            source.startsWith("https://", ignoreCase = true) ||
                source.startsWith("http://", ignoreCase = true) -> source
            source.startsWith("//") -> "https:$source"
            source.startsWith("/") -> "$baseUrl$source"
            else -> "$baseUrl/vtop/${source.trimStart('/')}"
        }
        val request = Request.Builder()
            .url(imageUrl)
            .header("User-Agent", userAgent)
            .get()
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use null
            val body = response.body ?: return@use null
            if (body.contentLength() > MAX_PROFILE_IMAGE_BYTES) return@use null
            Base64.encodeToString(body.bytes(), Base64.NO_WRAP)
        }
    }

    private fun attendanceDetailFields(
        authorizedId: String,
        csrf: String,
        semesterId: String,
        registerNumber: String,
        courseId: String,
        courseType: String
    ): Map<String, String> =
        mapOf(
            "_csrf" to csrf,
            "semesterSubId" to semesterId,
            "registerNumber" to registerNumber,
            "courseId" to courseId,
            "courseType" to courseType,
            "authorizedID" to authorizedId,
            "x" to utcRequestTimestamp()
        )

    private fun utcRequestTimestamp(): String =
        ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)

    private fun rememberPage(html: String, url: String = "") {
        if (html.isBlank()) return
        val marker = "$url ${html.take(8000)}"
        when {
            marker.contains("attendance", ignoreCase = true) -> lastAttendanceHtml = html
            marker.contains("timetable", ignoreCase = true) ||
                marker.contains("time table", ignoreCase = true) ||
                marker.contains("slot", ignoreCase = true) -> lastTimetableHtml = html
            marker.contains("grade history", ignoreCase = true) -> lastGradeHistoryHtml = html
            marker.contains("marks view", ignoreCase = true) -> lastMarksHtml = html
            marker.contains("grade", ignoreCase = true) -> lastGradesHtml = html
            else -> lastDashboardHtml = html
        }
    }

    private fun buildDashboard(): DashboardSnapshot {
        val base = parseSnapshot(
            listOf(lastDashboardHtml, lastAttendanceHtml, lastTimetableHtml, lastMarksHtml, lastGradesHtml, lastGradeHistoryHtml)
                .firstOrNull { !it.isNullOrBlank() }
                .orEmpty()
        )
        val attendanceSnapshot = lastAttendanceHtml?.let {
            parseSnapshot(it)
        }
        val timetableSnapshot = lastTimetableHtml?.let {
            parseSnapshot(it)
        }
        val marksSnapshot = lastMarksHtml?.let {
            parseSnapshot(it)
        }
        val gradesSnapshot = lastGradesHtml?.let {
            parseSnapshot(it)
        }
        val gradeHistorySnapshot = lastGradeHistoryHtml?.let {
            parseSnapshot(it)
        }
        val profileSnapshot = lastProfileHtml?.let {
            parseSnapshot(it)
        }
        val profileDetails = VtopParser.parseProfileDetails(
            profileHtml = lastProfileHtml.orEmpty(),
            proctorHtml = lastProctorHtml.orEmpty()
        )

        return base.copy(
            profile = mergedProfile(
                base.profile,
                profileSnapshot?.profile,
                gradeHistorySnapshot?.profile,
                gradesSnapshot?.profile,
                marksSnapshot?.profile,
                attendanceSnapshot?.profile,
                timetableSnapshot?.profile
            ),
            profileDetails = profileDetails.copy(
                studentPhotoBase64 = lastStudentPhotoBase64
                    ?: profileDetails.studentPhotoBase64,
                proctorPhotoBase64 = lastProctorPhotoBase64
                    ?: profileDetails.proctorPhotoBase64
            ),
            attendance = attendanceSnapshot?.attendance?.takeIf { it.isNotEmpty() } ?: base.attendance,
            timetable = timetableSnapshot?.timetable?.takeIf { it.isNotEmpty() } ?: base.timetable,
            nextClass = timetableSnapshot?.nextClass ?: base.nextClass,
            marks = marksSnapshot?.marks?.takeIf { it.isNotEmpty() } ?: base.marks,
            grades = gradesSnapshot?.grades?.takeIf { it.isNotEmpty() } ?: base.grades,
            gradeHistory = gradeHistorySnapshot?.gradeHistory?.takeIf { it.isNotEmpty() } ?: base.gradeHistory,
            gpa = gradesSnapshot?.gpa ?: gradeHistorySnapshot?.gpa ?: base.gpa,
            cgpa = gradeHistorySnapshot?.cgpa ?: gradesSnapshot?.cgpa ?: base.cgpa,
            totalCredits = gradeHistorySnapshot?.totalCredits ?: gradesSnapshot?.totalCredits ?: base.totalCredits,
            attendanceSemesters = attendanceSnapshot?.attendanceSemesters?.takeIf { it.isNotEmpty() }
                ?: base.attendanceSemesters,
            timetableSemesters = timetableSnapshot?.timetableSemesters?.takeIf { it.isNotEmpty() }
                ?: base.timetableSemesters,
            gradeSemesters = gradesSnapshot?.gradeSemesters?.takeIf { it.isNotEmpty() }
                ?: marksSnapshot?.gradeSemesters?.takeIf { it.isNotEmpty() }
                ?: base.gradeSemesters,
            selectedAttendanceSemester = attendanceSnapshot?.selectedAttendanceSemester
                ?: base.selectedAttendanceSemester,
            selectedTimetableSemester = timetableSnapshot?.selectedTimetableSemester
                ?: base.selectedTimetableSemester,
            selectedGradeSemester = gradesSnapshot?.selectedGradeSemester
                ?: marksSnapshot?.selectedGradeSemester
                ?: base.selectedGradeSemester
        ).withSelectedSemesters(
            selectedAttendanceSemesterId = selectedAttendanceSemesterId,
            selectedTimetableSemesterId = selectedTimetableSemesterId,
            selectedGradeSemesterId = selectedGradeSemesterId
        ).withReadableNextClassName()
    }

    private fun parseSnapshot(html: String): DashboardSnapshot =
        VtopParser.parseDashboard(
            html = html,
            selectedAttendanceSemesterId = selectedAttendanceSemesterId,
            selectedTimetableSemesterId = selectedTimetableSemesterId,
            selectedGradeSemesterId = selectedGradeSemesterId
        )

}

private fun mergedProfile(vararg profiles: UserProfile?): UserProfile {
    val availableProfiles = profiles.filterNotNull()
    return UserProfile(
        name = availableProfiles.firstOrNull { !it.name.isNullOrBlank() }?.name,
        registrationNumber = availableProfiles.firstOrNull { !it.registrationNumber.isNullOrBlank() }?.registrationNumber
    )
}

private fun DashboardSnapshot.withReadableNextClassName(): DashboardSnapshot {
    val currentNextClass = nextClass ?: return this
    val code = currentNextClass.code ?: classCodeRegex.find(currentNextClass.name)?.value
    val matchingCourse = attendance.firstOrNull { course ->
        code != null && course.code.equals(code, ignoreCase = true)
    } ?: attendance.firstOrNull { course ->
        currentNextClass.name.contains(course.code, ignoreCase = true)
    }

    return if (matchingCourse == null) {
        this
    } else {
        copy(
            nextClass = currentNextClass.copy(
                name = matchingCourse.name,
                code = matchingCourse.code
            )
        )
    }
}

private val classCodeRegex = Regex("\\b[A-Z]{2,5}\\d{3,5}[A-Z]?\\b")

private fun DashboardSnapshot.withSelectedSemesters(
    selectedAttendanceSemesterId: String? = selectedAttendanceSemester?.id,
    selectedTimetableSemesterId: String? = selectedTimetableSemester?.id,
    selectedGradeSemesterId: String? = selectedGradeSemester?.id
): DashboardSnapshot {
    val attendanceOptions = attendanceSemesters.ifEmpty {
        SemesterOptions.fallback("attendance", "Latest attendance")
    }
    val timetableOptions = timetableSemesters.ifEmpty {
        SemesterOptions.fallback("timetable", "Latest timetable")
    }
    val gradeOptions = gradeSemesters.ifEmpty {
        SemesterOptions.fallback("grades", "Latest grades")
    }
    return copy(
        attendanceSemesters = attendanceOptions,
        timetableSemesters = timetableOptions,
        gradeSemesters = gradeOptions,
        selectedAttendanceSemester = attendanceOptions.firstOrNull { it.id == selectedAttendanceSemesterId }
            ?: selectedAttendanceSemester
            ?: attendanceOptions.first(),
        selectedTimetableSemester = timetableOptions.firstOrNull { it.id == selectedTimetableSemesterId }
            ?: selectedTimetableSemester
            ?: timetableOptions.first(),
        selectedGradeSemester = gradeOptions.firstOrNull { it.id == selectedGradeSemesterId }
            ?: selectedGradeSemester
            ?: gradeOptions.first()
    )
}

private class PersistentCookieJar(
    private val store: SessionCookieStore
) : CookieJar {
    private val cookiesByHost = linkedMapOf<String, MutableList<Cookie>>()

    init {
        store.load().forEach { cookie ->
            cookiesByHost.getOrPut(cookie.domain) { mutableListOf() }.add(cookie)
        }
    }

    fun hasCookies(): Boolean =
        synchronized(cookiesByHost) {
            val now = System.currentTimeMillis()
            cookiesByHost.values.flatten().any { it.expiresAt > now }
        }

    fun clear() {
        synchronized(cookiesByHost) {
            cookiesByHost.clear()
            store.clear()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(cookiesByHost) {
            val existing = cookiesByHost.getOrPut(url.host) { mutableListOf() }
            cookies.forEach { cookie ->
                existing.removeAll { it.name == cookie.name && it.path == cookie.path }
                existing.add(cookie)
            }
            persistLocked()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        return synchronized(cookiesByHost) {
            cookiesByHost[url.host]
                ?.filter { cookie -> cookie.expiresAt > now && cookie.matches(url) }
                .orEmpty()
        }
    }

    private fun persistLocked() {
        val now = System.currentTimeMillis()
        store.save(cookiesByHost.values.flatten().filter { it.expiresAt > now })
    }
}
