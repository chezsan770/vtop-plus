package com.vtopu.app

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vtopu.app.data.AttendanceCourse
import com.vtopu.app.data.DashboardSnapshot
import com.vtopu.app.data.LoginChallenge
import com.vtopu.app.data.LoginResult
import com.vtopu.app.data.SemesterOption
import com.vtopu.app.data.SemesterOptions
import com.vtopu.app.data.SpotlightItem
import com.vtopu.app.data.VtopRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VtopTheme {
                VtopApp(repository = remember { VtopRepository() })
            }
        }
    }
}

@Composable
private fun VtopTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = Color(0xFF79D0E7),
        onPrimary = Color.White,
        secondary = Color(0xFFE6C36A),
        tertiary = Color(0xFF7DD7A7),
        background = Color(0xFF101411),
        surface = Color(0xFF171D19),
        surfaceVariant = Color(0xFF243029),
        onSurface = Color(0xFFF2F5F1),
        onSurfaceVariant = Color(0xFFC3CCC4)
    )

    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
private fun VtopApp(repository: VtopRepository) {
    var challenge by remember { mutableStateOf<LoginChallenge?>(null) }
    var dashboard by remember { mutableStateOf<DashboardSnapshot?>(null) }
    var spotlight by remember { mutableStateOf<List<SpotlightItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        spotlight = repository.loadPublicSpotlight()
        challenge = repository.prepareLogin()
        loading = false
    }

    Scaffold(
        bottomBar = {
            if (dashboard != null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.School, contentDescription = null) },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        label = { Text("Full VTOP") }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            if (dashboard == null) {
                LoginScreen(
                    challenge = challenge,
                    spotlight = spotlight,
                    loading = loading,
                    message = message,
                    onRefreshCaptcha = {
                        scope.launch {
                            loading = true
                            message = null
                            challenge = repository.refreshCaptcha()
                            loading = false
                        }
                    },
                    onLogin = { username, password, captcha ->
                        scope.launch {
                            loading = true
                            message = null
                            when (val result = repository.login(username, password, captcha)) {
                                is LoginResult.Success -> dashboard = result.dashboard
                                is LoginResult.Failure -> {
                                    message = result.reason
                                    challenge = result.challenge
                                        ?: repository.refreshCaptcha()
                                        ?: repository.prepareLogin()
                                }
                            }
                            loading = false
                        }
                    }
                )
            } else if (selectedTab == 0) {
                DashboardScreen(
                    dashboard = dashboard!!,
                    spotlight = spotlight,
                    loading = loading,
                    onSemesterSelected = { attendanceSemesterId, timetableSemesterId ->
                        scope.launch {
                            loading = true
                            dashboard = repository.selectSemesters(
                                attendanceSemesterId = attendanceSemesterId,
                                timetableSemesterId = timetableSemesterId
                            )
                            loading = false
                        }
                    },
                    onRefresh = {
                        scope.launch {
                            loading = true
                            message = null
                            dashboard = repository.refreshDashboard()
                            spotlight = repository.loadPublicSpotlight()
                            loading = false
                        }
                    }
                )
            } else {
                FullPortalScreen(
                    cookieHeaders = repository.cookiesForWebView(),
                    portalStartUrl = repository.portalStartUrl(),
                    onHomeRequested = { repository.portalHomeUrl() },
                    onPortalHtml = { html, url ->
                        scope.launch {
                            dashboard = repository.capturePortalPage(html, url)
                        }
                    }
                )
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun LoginScreen(
    challenge: LoginChallenge?,
    spotlight: List<SpotlightItem>,
    loading: Boolean,
    message: String?,
    onRefreshCaptcha: () -> Unit,
    onLogin: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var captcha by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(challenge?.captchaBase64, challenge?.csrf) {
        captcha = ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "VTOP U",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "A cleaner companion for VIT Bhopal academics.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Student login", fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.uppercase().take(15) },
                        label = { Text("Registration number") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    CaptchaBlock(challenge = challenge, onRefresh = onRefreshCaptcha)
                    OutlinedTextField(
                        value = captcha,
                        onValueChange = { captcha = it.uppercase().take(6) },
                        label = { Text("CAPTCHA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(message != null) {
                        Text(message.orEmpty(), color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        enabled = !loading && username.isNotBlank() && password.isNotBlank() && captcha.length >= 4,
                        onClick = { onLogin(username, password, captcha) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign in")
                    }
                }
            }
        }

        item {
            SpotlightSection(items = spotlight)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CaptchaBlock(challenge: LoginChallenge?, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
            .weight(1f)
            .height(92.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            val image = remember(challenge?.captchaBase64) {
                challenge?.captchaBase64?.toBitmap()
            }
            if (image != null) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "CAPTCHA image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text("Loading CAPTCHA")
                }
            }
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh CAPTCHA")
        }
    }
}

@Composable
private fun DashboardScreen(
    dashboard: DashboardSnapshot,
    spotlight: List<SpotlightItem>,
    loading: Boolean,
    onSemesterSelected: (String, String) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dashboard.profile.greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = listOfNotNull(dashboard.profile.name, dashboard.profile.registrationNumber)
                            .joinToString(" • ")
                            .ifBlank { "Signed in to VTOP" },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onRefresh, enabled = !loading) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh dashboard")
                }
            }
        }

        item {
            SemesterSelectorCard(
                dashboard = dashboard,
                onSemesterSelected = onSemesterSelected
            )
        }

        item {
            NextClassCard(dashboard = dashboard)
        }

        item {
            Text("Attendance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (dashboard.attendance.isEmpty()) {
            item {
                EmptyState("Attendance was not visible on the current VTOP page. Open Full VTOP once after login, then refresh.")
            }
        } else {
            items(dashboard.attendance) { course ->
                AttendanceRow(course = course)
            }
        }

        item {
            SpotlightSection(items = spotlight)
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SemesterSelectorCard(
    dashboard: DashboardSnapshot,
    onSemesterSelected: (String, String) -> Unit
) {
    val semesterOptions = (dashboard.attendanceSemesters + dashboard.timetableSemesters)
        .distinctBy { it.id }
        .ifEmpty {
            SemesterOptions.fallback("semester", "Latest semester")
        }
    val selectedSemester = semesterOptions.firstOrNull { option ->
        option.id == dashboard.selectedAttendanceSemester?.id
    } ?: semesterOptions.firstOrNull { option ->
        option.id == dashboard.selectedTimetableSemester?.id
    } ?: semesterOptions.first()

    Card(shape = RoundedCornerShape(8.dp)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Semester", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SemesterDropdown(
                label = "Attendance and timetable",
                selected = selectedSemester,
                options = semesterOptions,
                onSelected = { option -> onSemesterSelected(option.id, option.id) }
            )
        }
    }
}

@Composable
private fun SemesterDropdown(
    label: String,
    selected: SemesterOption,
    options: List<SemesterOption>,
    onSelected: (SemesterOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = selected.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select $label semester")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun NextClassCard(dashboard: DashboardSnapshot) {
    val nextClass = dashboard.nextClass
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails && nextClass != null) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text("Class details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "Class", value = nextClass.name)
                    DetailRow(label = "Code", value = nextClass.code.orEmpty().ifBlank { "Check VTOP" })
                    DetailRow(label = "Slot", value = nextClass.slot.orEmpty().ifBlank { "Check VTOP" })
                    DetailRow(label = "Day", value = nextClass.day.orEmpty().ifBlank { "Check VTOP" })
                    DetailRow(label = "Time", value = nextClass.time)
                    DetailRow(label = "Venue", value = nextClass.venue)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetails = false }) {
                    Text("Close")
                }
            }
        )
    }

    Card(
        modifier = Modifier.clickable(enabled = nextClass != null) { showDetails = true },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF123B42))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFF2B84B))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Next class", color = Color.White, fontWeight = FontWeight.Bold)
            }
            if (nextClass == null) {
                Text("No upcoming class found on the loaded page.", color = Color(0xFFDDE7E4))
            } else {
                Text(
                    nextClass.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${nextClass.time} - ${nextClass.venue}", color = Color(0xFFDDE7E4))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        Text(text = value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OldNextClassCard(dashboard: DashboardSnapshot) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF123B42))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFF2B84B))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Next class", color = Color.White, fontWeight = FontWeight.Bold)
            }
            val nextClass = dashboard.nextClass
            if (nextClass == null) {
                Text("No upcoming class found on the loaded page.", color = Color(0xFFDDE7E4))
            } else {
                Text(nextClass.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("${nextClass.time}  •  ${nextClass.venue}", color = Color(0xFFDDE7E4))
            }
        }
    }
}

@Composable
private fun AttendanceRow(course: AttendanceCourse) {
    Card(shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(course.code, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${course.percentage}%", fontWeight = FontWeight.Black)
            }
            LinearProgressIndicator(
                progress = { (course.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (course.percentage >= 75) Color(0xFF2D6A4F) else Color(0xFFB64232)
            )
        }
    }
}

@Composable
private fun SpotlightSection(items: List<SpotlightItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Campaign, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Spotlight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (items.isEmpty()) {
            EmptyState("No public spotlight item is available right now.")
        } else {
            items.take(3).forEach { item ->
                Card(shape = RoundedCornerShape(8.dp)) {
                    Text(item.title, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(text, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FullPortalScreen(
    cookieHeaders: List<String>,
    portalStartUrl: String,
    onHomeRequested: () -> String,
    onPortalHtml: (String, String) -> Unit
) {
    val context = LocalContext.current
    val portalUrl = portalStartUrl
    val captureBridge = remember(onPortalHtml) {
        PortalCaptureBridge(onPortalHtml)
    }
    val webView = remember { mutableStateOf<WebView?>(null) }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val createdWebView = WebView(context)
                createdWebView.apply {
                    addJavascriptInterface(captureBridge, "VtopCapture")
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            view.captureAfterLoad()
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.setSupportMultipleWindows(false)
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    CookieManager.getInstance().apply {
                        setAcceptCookie(true)
                        setAcceptThirdPartyCookies(createdWebView, true)
                        cookieHeaders.forEach { cookie ->
                            setCookie("https://vtop.vitbhopal.ac.in", cookie)
                            setCookie(portalUrl, cookie)
                        }
                        flush()
                    }
                    loadUrl(portalUrl)
                }.also { webView.value = it }
            },
            update = { view ->
                CookieManager.getInstance().apply {
                    cookieHeaders.forEach { cookie ->
                        setCookie("https://vtop.vitbhopal.ac.in", cookie)
                        setCookie(portalUrl, cookie)
                    }
                    flush()
                }
                if (view.url.isNullOrBlank()) view.loadUrl(portalUrl)
            }
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(onClick = { webView.value?.loadUrl(onHomeRequested()) }) {
                Icon(Icons.Default.Home, contentDescription = "VTOP home")
            }
            FloatingActionButton(onClick = { webView.value?.reload() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload VTOP")
            }
        }
    }
}

private class PortalCaptureBridge(
    private val onPortalHtml: (String, String) -> Unit
) {
    @JavascriptInterface
    fun capture(html: String, url: String) {
        onPortalHtml(html, url)
    }
}

private fun WebView.captureAfterLoad() {
    Handler(Looper.getMainLooper()).postDelayed({
        evaluateJavascript(
            """
                (function() {
                  if (window.VtopCapture) {
                    window.VtopCapture.capture(document.documentElement.outerHTML, window.location.href);
                  }
                })();
            """.trimIndent(),
            null
        )
    }, 1200)
}

private fun String.toBitmap() = runCatching {
    val bytes = Base64.decode(this, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()
