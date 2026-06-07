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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vtopu.app.data.AttendanceCourse
import com.vtopu.app.data.CredentialStore
import com.vtopu.app.data.CourseMarks
import com.vtopu.app.data.DashboardSnapshot
import com.vtopu.app.data.GradeCourse
import com.vtopu.app.data.LoginChallenge
import com.vtopu.app.data.LoginResult
import com.vtopu.app.data.SavedCredentials
import com.vtopu.app.data.SemesterOption
import com.vtopu.app.data.SemesterOptions
import com.vtopu.app.data.SessionKeepAliveResult
import com.vtopu.app.data.SpotlightItem
import com.vtopu.app.data.TimetableClass
import com.vtopu.app.data.VtopRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private val OxfordBlue = Color(0xFF1A365D)
private val OxfordBlueDark = Color(0xFFADC7F7)
private val SuccessGreen = Color(0xFF2F855A)
private val WarmGold = Color(0xFFE6C36A)
private val LightBackground = Color(0xFFF8F9FF)
private val LightCard = Color(0xFFFFFFFF)
private val LightCardSoft = Color(0xFFF1F5FB)
private val LightOutline = Color(0xFFE2E8F0)
private val DarkBackground = Color(0xFF050505)
private val DarkCard = Color(0xFF171717)
private val DarkCardSoft = Color(0xFF222222)
private val DarkOutline = Color(0xFF303030)
private val AcademicFont = FontFamily.Serif
private const val KEEP_ALIVE_INTERVAL_MILLIS = 12L * 60L * 1000L
private val AcademicTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.academic(),
        displayMedium = base.displayMedium.academic(),
        displaySmall = base.displaySmall.academic(),
        headlineLarge = base.headlineLarge.academic(),
        headlineMedium = base.headlineMedium.academic(),
        headlineSmall = base.headlineSmall.academic(),
        titleLarge = base.titleLarge.academic(),
        titleMedium = base.titleMedium.academic(),
        titleSmall = base.titleSmall.academic(),
        bodyLarge = base.bodyLarge.academic(),
        bodyMedium = base.bodyMedium.academic(),
        bodySmall = base.bodySmall.academic(),
        labelLarge = base.labelLarge.academic(),
        labelMedium = base.labelMedium.academic(),
        labelSmall = base.labelSmall.academic()
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkMode by remember { mutableStateOf(true) }
            VtopTheme(dark = darkMode) {
                VtopApp(
                    repository = remember { VtopRepository() },
                    darkMode = darkMode,
                    onToggleTheme = { darkMode = !darkMode }
                )
            }
        }
    }
}

@Composable
private fun VtopTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) {
        darkColorScheme(
            primary = OxfordBlueDark,
            onPrimary = Color(0xFF001B3C),
            secondary = Color(0xFF83D8A6),
            onSecondary = Color(0xFF002111),
            tertiary = WarmGold,
            background = DarkBackground,
            onBackground = Color(0xFFF5F7FB),
            surface = DarkCard,
            onSurface = Color(0xFFF5F7FB),
            surfaceVariant = DarkCardSoft,
            onSurfaceVariant = Color(0xFFC7CCD4),
            outline = DarkOutline,
            error = Color(0xFFFFB4AB)
        )
    } else {
        lightColorScheme(
            primary = OxfordBlue,
            onPrimary = Color.White,
            secondary = SuccessGreen,
            onSecondary = Color.White,
            tertiary = Color(0xFF475569),
            background = LightBackground,
            onBackground = Color(0xFF0D1C2E),
            surface = LightCard,
            onSurface = Color(0xFF0D1C2E),
            surfaceVariant = LightCardSoft,
            onSurfaceVariant = Color(0xFF475569),
            outline = LightOutline,
            error = Color(0xFFBA1A1A)
        )
    }

    MaterialTheme(colorScheme = colors, typography = AcademicTypography, content = content)
}

@Composable
private fun VtopApp(
    repository: VtopRepository,
    darkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    var challenge by remember { mutableStateOf<LoginChallenge?>(null) }
    var dashboard by remember { mutableStateOf<DashboardSnapshot?>(null) }
    var spotlight by remember { mutableStateOf<List<SpotlightItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var sessionGeneration by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val credentialStore = remember(context) { CredentialStore(context.applicationContext) }
    var savedCredentials by remember { mutableStateOf(credentialStore.load()) }
    var pendingSaveCredentials by remember { mutableStateOf<SavedCredentials?>(null) }
    val scope = rememberCoroutineScope()
    val logout: () -> Unit = {
        scope.launch {
            loading = true
            sessionGeneration += 1
            dashboard = null
            selectedTab = 0
            message = null
            challenge = null
            pendingSaveCredentials = null
            repository.logout()
            clearWebViewCookies()
            challenge = repository.prepareLogin()
            loading = false
        }
    }
    val selectSemester: (String, String) -> Unit = { attendanceSemesterId, timetableSemesterId ->
        scope.launch {
            loading = true
            dashboard = repository.selectSemesters(
                attendanceSemesterId = attendanceSemesterId,
                timetableSemesterId = timetableSemesterId
            )
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        spotlight = repository.loadPublicSpotlight()
        challenge = repository.prepareLogin()
        loading = false
    }

    LaunchedEffect(dashboard != null, sessionGeneration) {
        if (dashboard == null) return@LaunchedEffect
        val activeGeneration = sessionGeneration
        while (dashboard != null && activeGeneration == sessionGeneration) {
            delay(KEEP_ALIVE_INTERVAL_MILLIS)
            if (dashboard == null || activeGeneration != sessionGeneration) break

            when (repository.keepSessionAlive()) {
                SessionKeepAliveResult.Active -> Unit
                SessionKeepAliveResult.Failed -> Unit
                SessionKeepAliveResult.Expired -> {
                    if (activeGeneration == sessionGeneration) {
                        dashboard = null
                        selectedTab = 0
                        message = "VTOP session expired. Sign in again."
                        challenge = repository.prepareLogin()
                    }
                    return@LaunchedEffect
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (dashboard != null) {
                AcademicBottomBar(
                    selectedTab = selectedTab,
                    onSelected = { selectedTab = it }
                )
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
                    savedCredentials = savedCredentials,
                    darkMode = darkMode,
                    onToggleTheme = onToggleTheme,
                    onForgetCredentials = {
                        credentialStore.clear()
                        savedCredentials = null
                    },
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
                                is LoginResult.Success -> {
                                    val signedInCredentials = SavedCredentials(
                                        username = username.uppercase(),
                                        password = password
                                    )
                                    dashboard = result.dashboard
                                    if (savedCredentials != signedInCredentials) {
                                        pendingSaveCredentials = signedInCredentials
                                    }
                                }
                                is LoginResult.Failure -> {
                                    message = result.reason
                                    challenge = result.challenge ?: repository.prepareLogin()
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
                    darkMode = darkMode,
                    onToggleTheme = onToggleTheme,
                    onSemesterSelected = selectSemester,
                    onLogout = logout,
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
            } else if (selectedTab == 1) {
                ClassesScreen(
                    dashboard = dashboard!!,
                    darkMode = darkMode,
                    onToggleTheme = onToggleTheme,
                    onLogout = logout
                )
            } else if (selectedTab == 2) {
                GradesScreen(
                    dashboard = dashboard!!,
                    darkMode = darkMode,
                    onToggleTheme = onToggleTheme,
                    onLogout = logout,
                    onSemesterSelected = selectSemester
                )
            } else if (selectedTab == 3) {
                FullPortalScreen(
                    cookieHeaders = repository.cookiesForWebView(),
                    portalStartUrl = repository.portalStartUrl(),
                    onHomeRequested = { repository.portalHomeUrl() },
                    onLogout = logout,
                    onPortalHtml = { html, url ->
                        val expectedSession = sessionGeneration
                        scope.launch {
                            if (dashboard != null && expectedSession == sessionGeneration) {
                                dashboard = repository.capturePortalPage(html, url)
                            }
                        }
                    }
                )
            }

            if (loading) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(18.dp))
                }
            }

            pendingSaveCredentials?.let { credentials ->
                SaveCredentialsDialog(
                    username = credentials.username,
                    onSave = {
                        credentialStore.save(credentials)
                        savedCredentials = credentialStore.load()
                        pendingSaveCredentials = null
                    },
                    onSkip = {
                        pendingSaveCredentials = null
                    }
                )
            }
        }
    }
}

@Composable
private fun AcademicBottomBar(selectedTab: Int, onSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onSelected(1) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
            label = { Text("Classes") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onSelected(2) },
            icon = { Icon(Icons.Default.Grade, contentDescription = null) },
            label = { Text("Grades") }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onSelected(3) },
            icon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            label = { Text("VTOP") }
        )
    }
}

@Composable
private fun LoginScreen(
    challenge: LoginChallenge?,
    spotlight: List<SpotlightItem>,
    loading: Boolean,
    message: String?,
    savedCredentials: SavedCredentials?,
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onForgetCredentials: () -> Unit,
    onRefreshCaptcha: () -> Unit,
    onLogin: (String, String, String) -> Unit
) {
    var username by remember(savedCredentials?.username) { mutableStateOf(savedCredentials?.username.orEmpty()) }
    var password by remember(savedCredentials?.password) { mutableStateOf(savedCredentials?.password.orEmpty()) }
    var captcha by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val usingSavedCredentials = savedCredentials != null &&
        username.equals(savedCredentials.username, ignoreCase = true) &&
        password == savedCredentials.password

    LaunchedEffect(challenge?.captchaBase64, challenge?.csrf) {
        captcha = ""
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ThemeToggleButton(darkMode = darkMode, onToggleTheme = onToggleTheme)
            }
            Spacer(modifier = Modifier.height(34.dp))
            AppMark()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "VTOP-U",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Academic Excellence System",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        item {
            AcademicCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Student login",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    savedCredentials?.let { credentials ->
                        SavedCredentialCard(
                            username = credentials.username,
                            onForgetCredentials = onForgetCredentials
                        )
                    }
                    AcademicTextField(
                        value = username,
                        onValueChange = { username = it.uppercase().take(15) },
                        label = "Registration number",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    AcademicTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )
                    CaptchaBlock(challenge = challenge, onRefresh = onRefreshCaptcha)
                    AcademicTextField(
                        value = captcha,
                        onValueChange = { captcha = it.uppercase().take(6) },
                        label = "CAPTCHA"
                    )
                    AnimatedVisibility(message != null) {
                        Text(
                            text = message.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(
                        enabled = !loading && username.isNotBlank() && password.isNotBlank() && captcha.length >= 4,
                        onClick = { onLogin(username, password, captcha) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (usingSavedCredentials) "Sign in with CAPTCHA" else "Sign in",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            SpotlightSection(items = spotlight)
        }
    }
}

@Composable
private fun SavedCredentialCard(
    username: String,
    onForgetCredentials: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Saved credentials",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = onForgetCredentials) {
                Text("Forget")
            }
        }
    }
}

@Composable
private fun SaveCredentialsDialog(
    username: String,
    onSave: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onSkip,
        title = { Text("Save credentials?") },
        text = {
            Text("Save credentials for $username so next time you only enter the CAPTCHA.")
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Yes, save")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("No")
            }
        }
    )
}

@Composable
private fun AppMark() {
    Surface(
        modifier = Modifier.size(68.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun AcademicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    )
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
                .height(82.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                    Text("Loading CAPTCHA", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onSemesterSelected: (String, String) -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit
) {
    val registrationNumber = dashboard.profile.registrationNumber ?: "Signed in to VTOP"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            DashboardHeader(
                title = "VTOP-U",
                greeting = dashboard.profile.greeting,
                registrationNumber = registrationNumber,
                loading = loading,
                darkMode = darkMode,
                onRefresh = onRefresh,
                onToggleTheme = onToggleTheme,
                onLogout = onLogout
            )
        }

        item {
            SemesterSelectorCard(
                dashboard = dashboard,
                onSemesterSelected = onSemesterSelected
            )
        }

        item {
            AttendanceSection(courses = dashboard.attendance)
        }

        item {
            SectionHeader(title = "Next Class", trailing = "TODAY")
            Spacer(modifier = Modifier.height(8.dp))
            NextClassCard(dashboard = dashboard)
        }

        item {
            SpotlightSection(items = spotlight)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ClassesScreen(
    dashboard: DashboardSnapshot,
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    val sortedTimetable = remember(dashboard.timetable) {
        dashboard.timetable.sortedBy { "${it.day.orEmpty()} ${it.time} ${it.code.orEmpty()}" }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SimpleScreenHeader(
                title = "Classes",
                subtitle = dashboard.selectedTimetableSemester?.label ?: "Selected semester",
                darkMode = darkMode,
                onToggleTheme = onToggleTheme,
                onLogout = onLogout
            )
        }
        if (dashboard.timetable.isEmpty()) {
            item {
                EmptyState("No timetable classes found in the loaded VTOP data.")
            }
        } else {
            items(
                items = sortedTimetable,
                key = { "${it.day.orEmpty()}-${it.time}-${it.code.orEmpty()}-${it.venue}" }
            ) { classSlot ->
                ClassScheduleCard(classSlot = classSlot)
            }
        }
    }
}

@Composable
private fun GradesScreen(
    dashboard: DashboardSnapshot,
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onSemesterSelected: (String, String) -> Unit
) {
    var showingHistory by remember { mutableStateOf(false) }
    val marksByCode = remember(dashboard.marks) {
        dashboard.marks.associateBy { it.code.uppercase() }
    }
    val historyByTerm = remember(dashboard.gradeHistory) {
        dashboard.gradeHistory.groupBy { it.examMonth?.ifBlank { "Earlier" } ?: "Earlier" }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SimpleScreenHeader(
                title = if (showingHistory) "Grade History" else "Grades",
                subtitle = if (showingHistory) {
                    "Review your academic performance."
                } else {
                    dashboard.selectedGradeSemester?.label ?: dashboard.selectedAttendanceSemester?.label ?: "Selected semester"
                },
                darkMode = darkMode,
                onToggleTheme = onToggleTheme,
                onLogout = onLogout
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showingHistory = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!showingHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!showingHistory) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Current")
                }
                Button(
                    onClick = { showingHistory = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showingHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (showingHistory) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("History")
                }
            }
        }
        if (showingHistory) {
            item {
                CumulativeGpaCard(
                    cgpa = dashboard.cgpa ?: estimateTermGpa(dashboard.gradeHistory),
                    historyCount = dashboard.gradeHistory.size
                )
            }
            if (dashboard.gradeHistory.isEmpty()) {
                item {
                    EmptyState("No grade history found in the loaded VTOP data.")
                }
            } else {
                historyByTerm.forEach { (term, courses) ->
                    item {
                        TermHeader(term = term, gpa = estimateTermGpa(courses))
                    }
                    item {
                        TermGradeGroup(courses = courses)
                    }
                }
            }
        } else {
            item {
                SemesterSelectorCard(
                    dashboard = dashboard,
                    onSemesterSelected = onSemesterSelected
                )
            }
            item {
                GradeSummaryCard(courseCount = dashboard.grades.size, gpa = dashboard.gpa)
            }
            if (dashboard.grades.isEmpty()) {
                item {
                    EmptyState("No grades found for the selected semester.")
                }
            } else {
                items(
                    items = dashboard.grades,
                    key = { "${it.code}-${it.grade}-${it.grandTotal.orEmpty()}" }
                ) { course ->
                    GradeCourseCard(
                        course = course,
                        marks = marksByCode[course.code.uppercase()]
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleScreenHeader(
    title: String,
    subtitle: String,
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            ThemeToggleButton(darkMode = darkMode, onToggleTheme = onToggleTheme)
            LogoutButton(onLogout = onLogout)
        }
    }
}

@Composable
private fun ClassScheduleCard(classSlot: TimetableClass) {
    AcademicCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(text = classSlot.code ?: classSlot.slot ?: "Class")
                    Text(
                        classSlot.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(classSlot.time.startTime(), fontWeight = FontWeight.Black)
            }
            DetailRow(label = "Venue", value = classSlot.venue)
            DetailRow(label = "Day", value = classSlot.day.orEmpty().ifBlank { "Check VTOP" })
        }
    }
}

@Composable
private fun GradeSummaryCard(courseCount: Int, gpa: String?) {
    AcademicCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Current Grade Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text("$courseCount enrolled courses", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    gpa?.let { "GPA $it" } ?: "VTOP",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun GradeCourseCard(course: GradeCourse, marks: CourseMarks?) {
    var expanded by remember(course.code) { mutableStateOf(false) }
    AcademicCard(modifier = Modifier.animateContentSize()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(course.title, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(text = course.code)
                        StatusChip(text = course.credits.ifBlank { "Credits" })
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(course.grade, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    course.grandTotal?.let { Text("$it total", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Text(course.courseType, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (marks != null && marks.assessments.isNotEmpty()) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide marks" else "View marks")
                }
                AnimatedVisibility(expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        marks.assessments.forEach { mark ->
                            MarkEntryRow(
                                title = mark.title,
                                scored = mark.scoredMark,
                                max = mark.maxMark,
                                weight = mark.weightedMark,
                                status = mark.status
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkEntryRow(title: String, scored: String, max: String, weight: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Text("$scored/$max  ($weight)", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CumulativeGpaCard(cgpa: String?, historyCount: Int) {
    AcademicCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CUMULATIVE CGPA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(cgpa ?: "--", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text("$historyCount completed courses", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Grade, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun TermHeader(term: String, gpa: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(term, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = "Term GPA: ${gpa ?: "--"}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun TermGradeGroup(courses: List<GradeCourse>) {
    AcademicCard(contentPadding = PaddingValues(0.dp)) {
        Column {
            courses.forEachIndexed { index, course ->
                TermGradeRow(course = course)
                if (index != courses.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }
    }
}

@Composable
private fun TermGradeRow(course: GradeCourse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(course.title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusChip(text = course.code)
                Text("${course.credits} Credits", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(4.dp),
            color = if (course.grade.equals("S", true) || course.grade.startsWith("A", true)) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(course.grade, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    title: String,
    greeting: String,
    registrationNumber: String,
    loading: Boolean,
    darkMode: Boolean,
    onRefresh: () -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                ThemeToggleButton(darkMode = darkMode, onToggleTheme = onToggleTheme)
                IconButton(onClick = onRefresh, enabled = !loading) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh dashboard")
                }
                LogoutButton(onLogout = onLogout)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            StatusChip(text = registrationNumber)
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

    AcademicCard(contentPadding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                .height(58.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
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
            modifier = Modifier.fillMaxWidth(0.88f)
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
private fun AttendanceSection(courses: List<AttendanceCourse>) {
    SectionHeader(
        title = "My Attendance",
        trailing = if (courses.isEmpty()) null else "${courses.size} courses"
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (courses.isEmpty()) {
        EmptyState("Attendance was not visible on the current VTOP page. Open Full VTOP once after login, then refresh.")
    } else {
        LazyRow(
            state = rememberLazyListState(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = courses,
                key = { it.code }
            ) { course ->
                AttendanceCourseCard(course = course)
            }
        }
    }
}

@Composable
private fun AttendanceCourseCard(course: AttendanceCourse) {
    var animateIn by remember(course.code, course.percentage) { mutableStateOf(false) }
    LaunchedEffect(course.code, course.percentage) {
        animateIn = false
        delay(80)
        animateIn = true
    }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animateIn) (course.percentage / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "attendance-progress"
    )

    AcademicCard(
        modifier = Modifier
            .width(148.dp)
            .height(154.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 5.dp,
                    color = if (course.percentage >= 75) SuccessGreen else MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text("${course.percentage}%", fontWeight = FontWeight.Black)
            }
            Text(
                text = course.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            StatusChip(text = course.code)
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = nextClass != null) { showDetails = true }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        if (nextClass == null) {
            EmptyState("No upcoming class found on the loaded page.")
        } else {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(text = nextClass.code ?: nextClass.slot ?: "VTOP")
                        Text(
                            text = nextClass.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("STARTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(nextClass.time.startTime(), fontWeight = FontWeight.Black)
                        }
                    }
                }
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                DetailRow(label = "Venue", value = nextClass.venue)
                nextClass.day?.takeIf { it.isNotBlank() }?.let { day ->
                    DetailRow(label = "Day", value = day)
                }
            }
        }
    }
}

@Composable
private fun SpotlightSection(items: List<SpotlightItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Spotlight", trailing = null)
        if (items.isEmpty()) {
            EmptyState("No public spotlight item is available right now.")
        } else {
            items.take(3).forEach { item ->
                AcademicCard(contentPadding = PaddingValues(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(item.title, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        trailing?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeToggleButton(darkMode: Boolean, onToggleTheme: () -> Unit) {
    IconButton(onClick = onToggleTheme) {
        Icon(
            imageVector = if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (darkMode) "Switch to light mode" else "Switch to dark mode"
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    IconButton(onClick = onLogout) {
        Icon(Icons.Default.Logout, contentDescription = "Log out")
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AcademicCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyState(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(text, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FullPortalScreen(
    cookieHeaders: List<String>,
    portalStartUrl: String,
    onHomeRequested: () -> String,
    onLogout: () -> Unit,
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
            FloatingActionButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, contentDescription = "Log out")
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

private suspend fun clearWebViewCookies() {
    suspendCancellableCoroutine { continuation ->
        Handler(Looper.getMainLooper()).post {
            CookieManager.getInstance().removeAllCookies {
                CookieManager.getInstance().flush()
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }
}

private fun String.toBitmap() = runCatching {
    val bytes = Base64.decode(this, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()

private fun String.startTime(): String =
    substringBefore("-").trim().ifBlank { this }

private fun TextStyle.academic(): TextStyle = copy(fontFamily = AcademicFont)

private fun estimateTermGpa(courses: List<GradeCourse>): String? {
    val weighted = courses.mapNotNull { course ->
        val credits = course.credits.toDoubleOrNull() ?: return@mapNotNull null
        val points = when (course.grade.uppercase()) {
            "S" -> 10.0
            "A" -> 9.0
            "B" -> 8.0
            "C" -> 7.0
            "D" -> 6.0
            "E" -> 5.0
            "P" -> null
            else -> null
        } ?: return@mapNotNull null
        credits to points
    }
    val credits = weighted.sumOf { it.first }
    if (credits == 0.0) return null
    val gpa = weighted.sumOf { (credit, points) -> credit * points } / credits
    return String.format(java.util.Locale.US, "%.2f", gpa)
}
