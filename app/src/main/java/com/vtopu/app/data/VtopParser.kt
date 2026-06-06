package com.vtopu.app.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object VtopParser {
    fun parseCsrf(html: String): String? =
        Jsoup.parse(html).selectFirst("input[name=_csrf]")?.attr("value")?.takeIf { it.isNotBlank() }

    fun parseCaptchaBase64(html: String): String? {
        val document = Jsoup.parse(html)
        val source = document.selectFirst("img[src^=data:image]")?.attr("src").orEmpty()
        return source.substringAfter("base64,", missingDelimiterValue = "").takeIf { it.isNotBlank() }
    }

    fun parseLoginError(html: String): String? {
        val document = Jsoup.parse(html)
        return document.select(".text-danger, .alert-danger, [class*=danger], [class*=error]")
            .asSequence()
            .map { it.text().cleanText() }
            .firstOrNull { text ->
                text.length in 4..120 &&
                    !text.equals("Sorry !!!", ignoreCase = true) &&
                    !text.contains("Session Timed Out", ignoreCase = true)
            }
    }

    fun parseSpotlight(html: String): List<SpotlightItem> {
        val document = Jsoup.parse(html)
        return document.select(".spotlight-background li, #spotlightCanvas li")
            .mapNotNull { item -> item.text().cleanText().takeIf { it.length > 8 } }
            .distinct()
            .map { SpotlightItem(it) }
    }

    fun parseDashboard(
        html: String,
        selectedAttendanceSemesterId: String? = null,
        selectedTimetableSemesterId: String? = null
    ): DashboardSnapshot {
        val document = Jsoup.parse(html)
        val attendanceSemesters = parseSemesterOptions(document, Purpose.Attendance)
        val timetableSemesters = parseSemesterOptions(document, Purpose.Timetable)
        val selectedAttendanceSemester = chooseSemester(attendanceSemesters, selectedAttendanceSemesterId)
        val selectedTimetableSemester = chooseSemester(timetableSemesters, selectedTimetableSemesterId)

        return DashboardSnapshot(
            profile = parseProfile(document),
            attendance = parseAttendance(document),
            nextClass = parseNextClass(document),
            timetable = parseTimetable(document),
            attendanceSemesters = attendanceSemesters,
            timetableSemesters = timetableSemesters,
            selectedAttendanceSemester = selectedAttendanceSemester,
            selectedTimetableSemester = selectedTimetableSemester
        )
    }

    fun parseAttendanceSemesters(html: String): List<SemesterOption> =
        parseSemesterOptions(Jsoup.parse(html), Purpose.Attendance)

    fun parseTimetableSemesters(html: String): List<SemesterOption> =
        parseSemesterOptions(Jsoup.parse(html), Purpose.Timetable)

    private fun parseSemesterOptions(document: Document, purpose: Purpose): List<SemesterOption> {
        val exactMatches = document.select("select")
            .filter { select -> select.contextText().matchesPurpose(purpose) }
            .flatMap(::selectOptions)

        val fallbackMatches = document.select("select")
            .filter { select ->
                select.contextText().contains("semester", ignoreCase = true) ||
                    selectOptions(select).size > 1
            }
            .flatMap(::selectOptions)

        return (exactMatches.ifEmpty { fallbackMatches })
            .distinctBy { it.id }
            .sortedWith(compareByDescending<SemesterOption> { it.rank() }.thenBy { it.label })
    }

    private fun selectOptions(select: Element): List<SemesterOption> =
        select.select("option")
            .mapNotNull { option ->
                val label = option.text().cleanText()
                val value = option.attr("value").cleanText()
                val id = value.ifBlank { label }
                if (
                    id.isBlank() ||
                    label.isBlank() ||
                    label.contains("select", ignoreCase = true) ||
                    label.contains("choose", ignoreCase = true)
                ) {
                    null
                } else {
                    SemesterOption(id = id, label = label)
                }
            }

    private fun chooseSemester(options: List<SemesterOption>, selectedId: String?): SemesterOption? =
        options.firstOrNull { it.id == selectedId }
            ?: options.maxByOrNull { it.rank() }
            ?: options.firstOrNull()

    private fun parseProfile(document: Document): UserProfile {
        val bodyText = document.body()?.text().orEmpty()
        val registrationNumber = Regex("\\b\\d{2}[A-Z]{3}\\d{4}\\b")
            .find(bodyText)
            ?.value

        val name = listOf(
            "studentName",
            "student-name",
            "profileName",
            "authorizedIDX"
        ).firstNotNullOfOrNull { id ->
            document.getElementById(id)?.text()?.cleanText()?.takeIf { it.length in 3..80 }
        } ?: document.findLabelValue("Name")

        return UserProfile(
            name = name?.removePrefix(":")?.cleanText(),
            registrationNumber = registrationNumber ?: document.findLabelValue("Register Number")
        )
    }

    private fun parseAttendance(document: Document): List<AttendanceCourse> {
        return document.select("table")
            .filter { table -> table.text().contains("attendance", ignoreCase = true) }
            .flatMap { table -> table.select("tbody tr, tr").mapNotNull(::parseAttendanceRow) }
            .distinctBy { "${it.code}-${it.name}" }
    }

    private fun parseTimetable(document: Document): List<TimetableClass> {
        parseStructuredTimetable(document)?.let { return it }

        return document.select("table")
            .filter { table ->
                val text = table.text()
                text.contains("venue", ignoreCase = true) ||
                    text.contains("slot", ignoreCase = true) ||
                    text.contains("time", ignoreCase = true)
            }
            .flatMap { table -> table.select("tbody tr, tr").mapNotNull(::parseTimetableRow) }
            .distinctBy { "${it.day}-${it.time}-${it.name}-${it.venue}" }
    }

    private fun parseStructuredTimetable(document: Document): List<TimetableClass>? {
        val table = document.getElementById("timeTableStyle") ?: return null
        val rows = table.select("tr").map { row ->
            row.children().map { it.text().cleanText() }
        }
        if (rows.size < 3) return null

        val starts = rows[0].drop(2)
        val ends = rows[1].drop(1)
        val timeRanges = starts.zip(ends).map { (start, end) ->
            if (start.equals("Lunch", ignoreCase = true) || end.equals("Lunch", ignoreCase = true)) {
                "Lunch"
            } else {
                "$start - $end"
            }
        }

        return rows.drop(2).flatMap { cells ->
            val day = cells.firstOrNull().orEmpty()
            cells.drop(2).mapIndexedNotNull { index, value ->
                if (!value.contains("-") || value.equals("Lunch", ignoreCase = true)) return@mapIndexedNotNull null
                if (courseCodeRegex.find(value) == null) return@mapIndexedNotNull null

                val parts = value.split("-")
                val slot = parts.firstOrNull().orEmpty().cleanText()
                val course = courseCodeRegex.find(value)?.value.orEmpty()
                val venue = parts.drop(3).dropLast(1).joinToString("-").ifBlank { "Check VTOP" }

                TimetableClass(
                    name = "$slot - $course",
                    venue = venue,
                    time = timeRanges.getOrNull(index) ?: "Check VTOP",
                    day = day,
                    code = course,
                    slot = slot
                )
            }
        }.takeIf { it.isNotEmpty() }
    }

    private fun parseTimetableRow(row: Element): TimetableClass? {
        val cells = row.select("td")
            .map { it.text().cleanText() }
            .filter { it.isNotBlank() }
        if (cells.size < 3) return null

        val joined = cells.joinToString(" ")
        val time = timeRegex.find(joined)?.value ?: cells.firstOrNull {
            it.contains("am", ignoreCase = true) ||
                it.contains("pm", ignoreCase = true) ||
                it.contains(":")
        } ?: return null
        val venue = cells.firstOrNull {
            it.contains("room", ignoreCase = true) ||
                it.contains("lab", ignoreCase = true) ||
                Regex("[A-Z]{1,4}-?\\d{2,4}[A-Z]?").containsMatchIn(it)
        } ?: "Check VTOP"
        val day = cells.firstOrNull { it.matches(dayRegex) }
        val name = cells.firstOrNull { cell ->
            cell != time &&
                cell != venue &&
                cell != day &&
                cell.length > 4 &&
                !cell.contains("slot", ignoreCase = true)
        } ?: "Class"

        val code = courseCodeRegex.find(joined)?.value
        return TimetableClass(name = name, venue = venue, time = time, day = day, code = code)
    }

    private fun parseAttendanceRow(row: Element): AttendanceCourse? {
        val cells = row.select("td")
            .map { it.text().cleanText() }
            .filter { it.isNotBlank() }
        if (cells.size < 3) return null

        val percentage = cells.firstNotNullOfOrNull { cell ->
            Regex("(\\d{1,3})(?:\\.\\d+)?\\s*%").find(cell)?.groupValues?.get(1)?.toIntOrNull()
        } ?: cells.lastOrNull()?.toIntOrNull()
        if (percentage == null || percentage !in 0..100) return null

        val courseCell = cells.firstOrNull { courseCodeRegex.containsMatchIn(it) } ?: return null
        val code = courseCodeRegex.find(courseCell)?.value ?: courseCell
        val name = courseCell.substringAfter(" - ", courseCell).substringBefore(" - Lecture").cleanText()

        return AttendanceCourse(code = code, name = name, percentage = percentage)
    }

    private fun parseNextClass(document: Document): NextClass? {
        parseTimetable(document).firstOrNull()?.let { row ->
            return NextClass(
                name = row.name,
                venue = row.venue,
                time = row.time,
                code = row.code,
                slot = row.slot,
                day = row.day
            )
        }

        val candidateRows = document.select("tr, .card, .list-group-item, div")
            .asSequence()
            .map { it.text().cleanText() }
            .filter { text ->
                text.contains("venue", ignoreCase = true) ||
                    text.contains("slot", ignoreCase = true) ||
                    timeRegex.containsMatchIn(text)
            }
            .filter { it.length in 12..220 }
            .toList()

        val row = candidateRows.firstOrNull { text ->
            timeRegex.containsMatchIn(text) &&
                (text.contains("room", ignoreCase = true) || text.contains("venue", ignoreCase = true))
        } ?: candidateRows.firstOrNull { timeRegex.containsMatchIn(it) }

        val time = row?.let { timeRegex.find(it)?.value } ?: return null
        val venue = Regex("(?:Venue|Room)\\s*:?\\s*([A-Za-z0-9 ./-]+)", RegexOption.IGNORE_CASE)
            .find(row)
            ?.groupValues
            ?.getOrNull(1)
            ?.cleanText()
            ?: "Check VTOP"
        val name = row
            .replace(time, "")
            .replace(Regex("(?:Venue|Room)\\s*:?\\s*[A-Za-z0-9 ./-]+", RegexOption.IGNORE_CASE), "")
            .cleanText()
            .take(70)
            .ifBlank { "Upcoming class" }

        val code = courseCodeRegex.find(row)?.value
        return NextClass(name = name, venue = venue, time = time, code = code)
    }

    private fun Document.findLabelValue(label: String): String? {
        val labelElement = select("*")
            .firstOrNull { it.ownText().trim().equals(label, ignoreCase = true) }
            ?: select("*").firstOrNull { it.ownText().contains(label, ignoreCase = true) }

        return labelElement
            ?.nextElementSibling()
            ?.text()
            ?.cleanText()
            ?.takeIf { it.isNotBlank() && it.length < 90 }
    }

    private val timeRegex = Regex("\\b\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm)?\\b(?:\\s*-\\s*\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm)?)?")
    private val dayRegex = Regex("Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Mon|Tue|Wed|Thu|Fri|Sat|Sun", RegexOption.IGNORE_CASE)
    private val courseCodeRegex = Regex("\\b[A-Z]{2,5}\\d{3,5}[A-Z]?\\b")

    enum class Purpose {
        Attendance,
        Timetable
    }
}

private fun String.cleanText(): String = replace(Regex("\\s+"), " ").trim()

private fun Element.contextText(): String {
    val parentText = parent()?.text().orEmpty()
    return listOf(id(), attr("name"), attr("aria-label"), previousElementSibling()?.text().orEmpty(), parentText)
        .joinToString(" ")
        .cleanText()
}

private fun String.matchesPurpose(purpose: VtopParser.Purpose): Boolean =
    when (purpose) {
        VtopParser.Purpose.Attendance -> contains("attendance", ignoreCase = true)
        VtopParser.Purpose.Timetable ->
            contains("timetable", ignoreCase = true) ||
                contains("time table", ignoreCase = true) ||
                contains("class schedule", ignoreCase = true)
    }

private fun SemesterOption.rank(): Int {
    val years = Regex("\\b(20\\d{2})\\b").findAll(label).mapNotNull { it.value.toIntOrNull() }.toList()
    val compactYears = Regex("\\b(\\d{2})\\s*-\\s*(\\d{2})\\b")
        .find(label)
        ?.groupValues
        ?.drop(1)
        ?.mapNotNull { "20$it".toIntOrNull() }
        .orEmpty()
    val yearRank = (years + compactYears).maxOrNull() ?: 0
    val seasonRank = when {
        label.contains("winter", ignoreCase = true) -> 3
        label.contains("summer", ignoreCase = true) -> 2
        label.contains("fall", ignoreCase = true) -> 1
        else -> 0
    }
    val semesterRank = Regex("\\bsem(?:ester)?\\s*(\\d{1,2})\\b", RegexOption.IGNORE_CASE)
        .find(label)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?: 0
    return yearRank * 100 + seasonRank * 10 + semesterRank
}
