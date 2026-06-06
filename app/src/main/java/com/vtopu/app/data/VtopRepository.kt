package com.vtopu.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class VtopRepository {
    private val baseUrl = "https://vtop.vitbhopal.ac.in"
    private val cookieJar = MemoryCookieJar()
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    private val publicClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private var loginChallenge: LoginChallenge? = null
    private var lastDashboardHtml: String? = null
    private var lastAttendanceHtml: String? = null
    private var lastTimetableHtml: String? = null
    private var lastResponseUrl: String? = null
    private var authenticatedPortalUrl: String? = null
    private var selectedAttendanceSemesterId: String? = null
    private var selectedTimetableSemesterId: String? = null
    private var currentAuthorizedId: String? = null
    private var currentCsrf: String? = null

    suspend fun loadPublicSpotlight(): List<SpotlightItem> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/vtop/login")
                .header("User-Agent", userAgent)
                .get()
                .build()
            val html = publicClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                response.body?.string().orEmpty()
            }
            VtopParser.parseSpotlight(html)
        }.getOrDefault(emptyList())
    }

    suspend fun prepareLogin(): LoginChallenge? = withContext(Dispatchers.IO) {
        runCatching {
            cookieJar.clear()
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
                val nextChallenge = VtopParser.parseCsrf(html)?.let { csrf ->
                    LoginChallenge(
                        csrf = csrf,
                        captchaBase64 = VtopParser.parseCaptchaBase64(html)
                    )
                }?.let { challenge ->
                    if (challenge.captchaBase64 == null) {
                        val captchaHtml = get("/vtop/get/new/captcha")
                        challenge.copy(captchaBase64 = VtopParser.parseCaptchaBase64(captchaHtml))
                    } else {
                        challenge
                    }
                }?.also { loginChallenge = it }
                val reason = VtopParser.parseLoginError(html)
                    ?: "VTOP rejected the login. Re-enter the new CAPTCHA and try again."
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
            LoginResult.Success(dashboard)
        }.getOrElse { error ->
            LoginResult.Failure(error.message ?: "VTOP login failed.")
        }
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
        }
    }

    suspend fun selectSemesters(
        attendanceSemesterId: String,
        timetableSemesterId: String
    ): DashboardSnapshot = withContext(Dispatchers.IO) {
        selectedAttendanceSemesterId = attendanceSemesterId
        selectedTimetableSemesterId = timetableSemesterId
        fetchAcademicData()
        buildDashboard()
    }

    suspend fun capturePortalPage(html: String, url: String): DashboardSnapshot = withContext(Dispatchers.Default) {
        rememberPage(html = html, url = url)
        buildDashboard()
    }

    fun cookiesForWebView(): List<String> {
        val url = "$baseUrl/vtop/content".toHttpUrl()
        return cookieJar.loadForRequest(url).map { cookie ->
            buildString {
                append(cookie.name)
                append('=')
                append(cookie.value)
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

    private fun post(path: String, fields: Map<String, String>): String {
        val bodyBuilder = FormBody.Builder()
        fields.forEach { (name, value) -> bodyBuilder.add(name, value) }
        val request = Request.Builder()
            .url("$baseUrl$path")
            .header("User-Agent", userAgent)
            .post(bodyBuilder.build())
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            lastResponseUrl = response.request.url.toString()
            response.body?.string().orEmpty()
        }
    }

    private companion object {
        const val userAgent = "Mozilla/5.0 (Linux; Android 14) VTOP-U/1.0"
    }

    private fun fetchAcademicData() {
        val authorizedId = currentAuthorizedId ?: return
        val csrf = currentCsrf ?: return

        runCatching {
            val attendanceMenu = post(
                path = "/vtop/academics/common/StudentAttendance",
                fields = menuFields(authorizedId, csrf)
            )
            val attendanceOptions = VtopParser.parseAttendanceSemesters(attendanceMenu)
            selectedAttendanceSemesterId = selectedAttendanceSemesterId
                ?.takeIf { selected -> attendanceOptions.any { it.id == selected } }
                ?: attendanceOptions.firstOrNull()?.id
            val attendanceDetail = selectedAttendanceSemesterId?.let { semesterId ->
                post(
                    path = "/vtop/processViewStudentAttendance",
                    fields = semesterFields(authorizedId, csrf, semesterId)
                )
            }.orEmpty()
            lastAttendanceHtml = attendanceMenu + attendanceDetail
        }

        runCatching {
            val timetableMenu = post(
                path = "/vtop/academics/common/StudentTimeTable",
                fields = menuFields(authorizedId, csrf)
            )
            val timetableOptions = VtopParser.parseTimetableSemesters(timetableMenu)
            selectedTimetableSemesterId = selectedTimetableSemesterId
                ?.takeIf { selected -> timetableOptions.any { it.id == selected } }
                ?: timetableOptions.firstOrNull()?.id
            val timetableDetail = selectedTimetableSemesterId?.let { semesterId ->
                post(
                    path = "/vtop/processViewTimeTable",
                    fields = semesterFields(authorizedId, csrf, semesterId)
                )
            }.orEmpty()
            lastTimetableHtml = timetableMenu + timetableDetail
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
            "x" to java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
        )

    private fun rememberPage(html: String, url: String = "") {
        if (html.isBlank()) return
        val marker = "$url ${html.take(8000)}"
        when {
            marker.contains("attendance", ignoreCase = true) -> lastAttendanceHtml = html
            marker.contains("timetable", ignoreCase = true) ||
                marker.contains("time table", ignoreCase = true) ||
                marker.contains("slot", ignoreCase = true) -> lastTimetableHtml = html
            else -> lastDashboardHtml = html
        }
    }

    private fun buildDashboard(): DashboardSnapshot {
        val base = VtopParser.parseDashboard(
            html = listOf(lastDashboardHtml, lastAttendanceHtml, lastTimetableHtml)
                .firstOrNull { !it.isNullOrBlank() }
                .orEmpty(),
            selectedAttendanceSemesterId = selectedAttendanceSemesterId,
            selectedTimetableSemesterId = selectedTimetableSemesterId
        )
        val attendanceSnapshot = lastAttendanceHtml?.let {
            VtopParser.parseDashboard(
                html = it,
                selectedAttendanceSemesterId = selectedAttendanceSemesterId,
                selectedTimetableSemesterId = selectedTimetableSemesterId
            )
        }
        val timetableSnapshot = lastTimetableHtml?.let {
            VtopParser.parseDashboard(
                html = it,
                selectedAttendanceSemesterId = selectedAttendanceSemesterId,
                selectedTimetableSemesterId = selectedTimetableSemesterId
            )
        }

        return base.copy(
            attendance = attendanceSnapshot?.attendance?.takeIf { it.isNotEmpty() } ?: base.attendance,
            timetable = timetableSnapshot?.timetable?.takeIf { it.isNotEmpty() } ?: base.timetable,
            nextClass = timetableSnapshot?.nextClass ?: base.nextClass,
            attendanceSemesters = attendanceSnapshot?.attendanceSemesters?.takeIf { it.isNotEmpty() }
                ?: base.attendanceSemesters,
            timetableSemesters = timetableSnapshot?.timetableSemesters?.takeIf { it.isNotEmpty() }
                ?: base.timetableSemesters,
            selectedAttendanceSemester = attendanceSnapshot?.selectedAttendanceSemester
                ?: base.selectedAttendanceSemester,
            selectedTimetableSemester = timetableSnapshot?.selectedTimetableSemester
                ?: base.selectedTimetableSemester
        ).withSelectedSemesters(
            selectedAttendanceSemesterId = selectedAttendanceSemesterId,
            selectedTimetableSemesterId = selectedTimetableSemesterId
        ).withReadableNextClassName()
    }
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
    selectedTimetableSemesterId: String? = selectedTimetableSemester?.id
): DashboardSnapshot {
    val attendanceOptions = attendanceSemesters.ifEmpty {
        SemesterOptions.fallback("attendance", "Latest attendance")
    }
    val timetableOptions = timetableSemesters.ifEmpty {
        SemesterOptions.fallback("timetable", "Latest timetable")
    }
    return copy(
        attendanceSemesters = attendanceOptions,
        timetableSemesters = timetableOptions,
        selectedAttendanceSemester = attendanceOptions.firstOrNull { it.id == selectedAttendanceSemesterId }
            ?: selectedAttendanceSemester
            ?: attendanceOptions.first(),
        selectedTimetableSemester = timetableOptions.firstOrNull { it.id == selectedTimetableSemesterId }
            ?: selectedTimetableSemester
            ?: timetableOptions.first()
    )
}

private class MemoryCookieJar : CookieJar {
    private val cookiesByHost = linkedMapOf<String, MutableList<Cookie>>()

    fun clear() {
        cookiesByHost.clear()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val existing = cookiesByHost.getOrPut(url.host) { mutableListOf() }
        cookies.forEach { cookie ->
            existing.removeAll { it.name == cookie.name && it.path == cookie.path }
            existing.add(cookie)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        return cookiesByHost[url.host]
            ?.filter { cookie -> cookie.expiresAt > now && cookie.matches(url) }
            .orEmpty()
    }
}
