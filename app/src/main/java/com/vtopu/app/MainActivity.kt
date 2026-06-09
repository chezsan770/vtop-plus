package com.vtopu.app

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.Gravity
import android.widget.Toast
import android.widget.FrameLayout
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.vtopu.app.data.AttendanceCourse
import com.vtopu.app.data.AppSettings
import com.vtopu.app.data.AppUpdate
import com.vtopu.app.data.AppUpdateRepository
import com.vtopu.app.data.ActiveUserHeartbeat
import com.vtopu.app.data.AppUsageRepository
import com.vtopu.app.data.CredentialStore
import com.vtopu.app.data.CourseMarks
import com.vtopu.app.data.DashboardSnapshot
import com.vtopu.app.data.FeatureRequestPayload
import com.vtopu.app.data.FeatureRequestRepository
import com.vtopu.app.data.GradeCourse
import com.vtopu.app.data.AttendanceRecord
import com.vtopu.app.data.LoginChallenge
import com.vtopu.app.data.LoginResult
import com.vtopu.app.data.SavedCredentials
import com.vtopu.app.data.SemesterOption
import com.vtopu.app.data.SemesterOptions
import com.vtopu.app.data.SessionKeepAliveResult
import com.vtopu.app.data.TimetableClass
import com.vtopu.app.data.VtopRepository
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppAd.AdMode
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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
private const val APP_USAGE_HEARTBEAT_INTERVAL_MILLIS = 60L * 1000L
private const val FEATURE_REQUEST_COOLDOWN_MILLIS = 30L * 1000L
private const val GRADES_AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
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

private enum class AppearanceTheme(
    val storageValue: String,
    val label: String,
    val previewBackground: Color,
    val previewAccent: Color
) {
    Classic(
        storageValue = "classic",
        label = "Classic",
        previewBackground = DarkBackground,
        previewAccent = OxfordBlueDark
    ),
    Ocean(
        storageValue = "ocean",
        label = "Ocean",
        previewBackground = Color(0xFF071324),
        previewAccent = Color(0xFF64B5F6)
    ),
    Slate(
        storageValue = "slate",
        label = "Slate",
        previewBackground = Color(0xFF111827),
        previewAccent = Color(0xFF94A3B8)
    );

    companion object {
        fun fromStorage(value: String): AppearanceTheme =
            entries.firstOrNull { it.storageValue == value } ?: Classic
    }
}

private enum class AccentColor(
    val storageValue: String,
    val label: String,
    val lightPrimary: Color,
    val darkPrimary: Color,
    val lightOnPrimary: Color = Color.White,
    val darkOnPrimary: Color = Color(0xFF061314)
) {
    Gamma(
        storageValue = "gamma",
        label = "Gamma",
        lightPrimary = OxfordBlue,
        darkPrimary = OxfordBlueDark,
        darkOnPrimary = Color(0xFF001B3C)
    ),
    Mint(
        storageValue = "mint",
        label = "Mint",
        lightPrimary = Color(0xFF047857),
        darkPrimary = Color(0xFF5EEAD4),
        darkOnPrimary = Color(0xFF00201A)
    ),
    Violet(
        storageValue = "violet",
        label = "Violet",
        lightPrimary = Color(0xFF6D28D9),
        darkPrimary = Color(0xFFC4B5FD),
        darkOnPrimary = Color(0xFF261047)
    ),
    Amber(
        storageValue = "amber",
        label = "Amber",
        lightPrimary = Color(0xFFB45309),
        darkPrimary = Color(0xFFFCD34D),
        lightOnPrimary = Color.White,
        darkOnPrimary = Color(0xFF2B1700)
    ),
    Rose(
        storageValue = "rose",
        label = "Rose",
        lightPrimary = Color(0xFFBE123C),
        darkPrimary = Color(0xFFFDA4AF),
        darkOnPrimary = Color(0xFF3B0712)
    );

    fun primary(dark: Boolean): Color = if (dark) darkPrimary else lightPrimary
    fun onPrimary(dark: Boolean): Color = if (dark) darkOnPrimary else lightOnPrimary
    fun secondary(dark: Boolean): Color =
        if (dark) primary(dark).copy(alpha = 0.82f) else primary(dark).copy(alpha = 0.88f)
    fun tertiary(dark: Boolean): Color =
        if (dark) primary(dark).copy(alpha = 0.62f) else primary(dark).copy(alpha = 0.72f)

    companion object {
        fun fromStorage(value: String): AccentColor =
            entries.firstOrNull { it.storageValue == value } ?: Gamma
    }
}

private data class ScheduleDateOption(
    val date: LocalDate,
    val dayLabel: String,
    val dateLabel: String
)

private data class ClassTimelineEntry(
    val classSlot: TimetableClass,
    val start: LocalDateTime,
    val end: LocalDateTime
)

private data class NextClassDisplay(
    val name: String,
    val venue: String,
    val faculty: String,
    val time: String,
    val code: String?,
    val slot: String?,
    val day: String?,
    val statusLabel: String,
    val timeLabel: String,
    val countdownLabel: String,
    val progress: Float
)

private data class NextClassCardState(
    val activeClass: NextClassDisplay?,
    val nextClass: NextClassDisplay?
)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.STARTAPP_APP_ID.isNotBlank()) {
            StartAppSDK.init(this, BuildConfig.STARTAPP_APP_ID, false)
        }
        setContent {
            val context = LocalContext.current
            val appSettings = remember(context) { AppSettings(context.applicationContext) }
            var darkMode by remember { mutableStateOf(appSettings.darkModeEnabled) }
            var appearanceTheme by remember {
                mutableStateOf(AppearanceTheme.fromStorage(appSettings.appearanceTheme))
            }
            var accentColor by remember {
                mutableStateOf(AccentColor.fromStorage(appSettings.accentColor))
            }
            VtopTheme(dark = darkMode, appearanceTheme = appearanceTheme, accentColor = accentColor) {
                VtopApp(
                    repository = remember(context) { VtopRepository(context.applicationContext) },
                    darkMode = darkMode,
                    appearanceTheme = appearanceTheme,
                    accentColor = accentColor,
                    onToggleTheme = {
                        darkMode = !darkMode
                        appSettings.darkModeEnabled = darkMode
                    },
                    onAppearanceThemeSelected = { selectedTheme ->
                        appearanceTheme = selectedTheme
                        appSettings.appearanceTheme = selectedTheme.storageValue
                    },
                    onAccentColorSelected = { selectedAccent ->
                        accentColor = selectedAccent
                        appSettings.accentColor = selectedAccent.storageValue
                    }
                )
            }
        }
    }

    fun showKeepAliveRewardedAd(
        onRewardEarned: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        if (BuildConfig.STARTAPP_APP_ID.isBlank()) {
            onRewardEarned()
            return
        }

        val rewardedAd = StartAppAd(this)
        var rewardEarned = false
        var callbackCompleted = false
        val mainHandler = Handler(Looper.getMainLooper())
        val completeAsUnavailable = {
            if (!callbackCompleted && !rewardEarned) {
                callbackCompleted = true
                onUnavailable()
            }
        }
        val noFillTimeout = Runnable {
            runOnUiThread(completeAsUnavailable)
        }

        rewardedAd.setVideoListener {
            rewardEarned = true
            if (!callbackCompleted) {
                callbackCompleted = true
                mainHandler.removeCallbacks(noFillTimeout)
                runOnUiThread(onRewardEarned)
            }
        }
        mainHandler.postDelayed(noFillTimeout, 8_000L)
        rewardedAd.loadAd(
            AdMode.REWARDED_VIDEO,
            object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    mainHandler.removeCallbacks(noFillTimeout)
                    if (!rewardedAd.showAd()) {
                        runOnUiThread(completeAsUnavailable)
                    }
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    mainHandler.removeCallbacks(noFillTimeout)
                    runOnUiThread(completeAsUnavailable)
                }
            }
        )
    }

    fun showGradesBiometricPrompt(
        onUnlocked: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Grades")
            .setSubtitle("Use fingerprint, face, phone PIN, pattern, or password.")
            .setAllowedAuthenticators(GRADES_AUTHENTICATORS)
            .build()

        prompt.authenticate(promptInfo)
    }
}

private tailrec fun Context.findMainActivity(): MainActivity? =
    when (this) {
        is MainActivity -> this
        is ContextWrapper -> baseContext.findMainActivity()
        else -> null
    }

@Composable
private fun VtopTheme(
    dark: Boolean,
    appearanceTheme: AppearanceTheme,
    accentColor: AccentColor,
    content: @Composable () -> Unit
) {
    val baseColors = if (dark) {
        when (appearanceTheme) {
            AppearanceTheme.Classic -> darkColorScheme(
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
            AppearanceTheme.Ocean -> darkColorScheme(
                primary = Color(0xFF7DD3FC),
                onPrimary = Color(0xFF00263A),
                secondary = Color(0xFF2DD4BF),
                onSecondary = Color(0xFF00201C),
                tertiary = Color(0xFFBFE9FF),
                background = Color(0xFF020914),
                onBackground = Color(0xFFEAF7FF),
                surface = Color(0xFF071426),
                onSurface = Color(0xFFEAF7FF),
                surfaceVariant = Color(0xFF0E2238),
                onSurfaceVariant = Color(0xFFC2D9EA),
                outline = Color(0xFF1E3A5F),
                error = Color(0xFFFFB4AB)
            )
            AppearanceTheme.Slate -> darkColorScheme(
                primary = Color(0xFFCBD5E1),
                onPrimary = Color(0xFF111827),
                secondary = Color(0xFFA7F3D0),
                onSecondary = Color(0xFF042014),
                tertiary = Color(0xFFBFDBFE),
                background = Color(0xFF080A0D),
                onBackground = Color(0xFFF4F6F8),
                surface = Color(0xFF14181F),
                onSurface = Color(0xFFF4F6F8),
                surfaceVariant = Color(0xFF1F2937),
                onSurfaceVariant = Color(0xFFD1D8E2),
                outline = Color(0xFF334155),
                error = Color(0xFFFFB4AB)
            )
        }
    } else {
        when (appearanceTheme) {
            AppearanceTheme.Classic -> lightColorScheme(
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
            AppearanceTheme.Ocean -> lightColorScheme(
                primary = Color(0xFF0369A1),
                onPrimary = Color.White,
                secondary = Color(0xFF0F766E),
                onSecondary = Color.White,
                tertiary = Color(0xFF2563EB),
                background = Color(0xFFF2FAFF),
                onBackground = Color(0xFF092033),
                surface = Color.White,
                onSurface = Color(0xFF092033),
                surfaceVariant = Color(0xFFE3F3FB),
                onSurfaceVariant = Color(0xFF395466),
                outline = Color(0xFFB8D7E8),
                error = Color(0xFFBA1A1A)
            )
            AppearanceTheme.Slate -> lightColorScheme(
                primary = Color(0xFF334155),
                onPrimary = Color.White,
                secondary = Color(0xFF475569),
                onSecondary = Color.White,
                tertiary = Color(0xFF64748B),
                background = Color(0xFFF6F7F9),
                onBackground = Color(0xFF111827),
                surface = Color.White,
                onSurface = Color(0xFF111827),
                surfaceVariant = Color(0xFFE7EAEE),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1),
                error = Color(0xFFBA1A1A)
            )
        }
    }
    val colors = baseColors.copy(
        primary = accentColor.primary(dark),
        onPrimary = accentColor.onPrimary(dark),
        secondary = accentColor.secondary(dark),
        tertiary = accentColor.tertiary(dark)
    )

    MaterialTheme(colorScheme = colors, typography = AcademicTypography, content = content)
}

@Composable
private fun VtopApp(
    repository: VtopRepository,
    darkMode: Boolean,
    appearanceTheme: AppearanceTheme,
    accentColor: AccentColor,
    onToggleTheme: () -> Unit,
    onAppearanceThemeSelected: (AppearanceTheme) -> Unit,
    onAccentColorSelected: (AccentColor) -> Unit
) {
    var challenge by remember { mutableStateOf<LoginChallenge?>(null) }
    var dashboard by remember { mutableStateOf<DashboardSnapshot?>(null) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var sessionGeneration by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 4 })
    val context = LocalContext.current
    val credentialStore = remember(context) { CredentialStore(context.applicationContext) }
    val appSettings = remember(context) { AppSettings(context.applicationContext) }
    val appUsageRepository = remember { AppUsageRepository() }
    val appUpdateRepository = remember { AppUpdateRepository() }
    val activity = remember(context) { context.findMainActivity() }
    val openPortalWindow: () -> Unit = {
        context.startActivity(PortalActivity.createIntent(context))
    }
    var savedCredentials by remember { mutableStateOf(credentialStore.load()) }
    var backgroundKeepAliveEnabled by remember { mutableStateOf(appSettings.backgroundKeepAliveEnabled) }
    var showLanding by remember { mutableStateOf(!appSettings.hasSeenLanding) }
    var pendingSaveCredentials by remember { mutableStateOf<SavedCredentials?>(null) }
    var availableUpdate by remember { mutableStateOf<AppUpdate?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            appSettings.backgroundKeepAliveEnabled = true
            backgroundKeepAliveEnabled = true
            if (dashboard != null) VtopKeepAliveService.start(context.applicationContext)
        } else {
            appSettings.backgroundKeepAliveEnabled = false
            backgroundKeepAliveEnabled = false
            message = "Notification permission is needed for background keep-alive."
        }
    }
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
            appSettings.backgroundKeepAliveEnabled = false
            backgroundKeepAliveEnabled = false
            VtopKeepAliveService.stop(context.applicationContext)
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
    val enableBackgroundKeepAlive: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            appSettings.backgroundKeepAliveEnabled = true
            backgroundKeepAliveEnabled = true
            VtopKeepAliveService.start(context.applicationContext)
        }
    }
    val setBackgroundKeepAlive: (Boolean) -> Unit = { enabled ->
        if (!enabled) {
            appSettings.backgroundKeepAliveEnabled = false
            backgroundKeepAliveEnabled = false
            VtopKeepAliveService.stop(context.applicationContext)
        } else if (BuildConfig.STARTAPP_APP_ID.isBlank()) {
            message = "Ads are not configured in this build, so VTOP Online was enabled directly."
            Toast.makeText(context, "VTOP Online enabled for this build.", Toast.LENGTH_SHORT).show()
            enableBackgroundKeepAlive()
        } else {
            message = "Watch a short rewarded ad to enable VTOP Online."
            Toast.makeText(context, "Watch a short ad to enable VTOP Online.", Toast.LENGTH_SHORT).show()
            activity?.showKeepAliveRewardedAd(
                onRewardEarned = {
                    message = "VTOP Online enabled. Thanks for supporting Gamma."
                    Toast.makeText(context, "VTOP Online enabled.", Toast.LENGTH_SHORT).show()
                    enableBackgroundKeepAlive()
                },
                onUnavailable = {
                    message = "Reward ad was not available, so VTOP Online was enabled directly."
                    Toast.makeText(context, "No reward ad available. VTOP Online enabled.", Toast.LENGTH_SHORT).show()
                    enableBackgroundKeepAlive()
                }
            ) ?: run {
                message = "Reward ad could not be opened, so VTOP Online was enabled directly."
                Toast.makeText(context, "VTOP Online enabled.", Toast.LENGTH_SHORT).show()
                enableBackgroundKeepAlive()
            }
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        dashboard = repository.restoreSession(savedCredentials?.username)
        if (dashboard == null && !showLanding) {
            challenge = repository.prepareLogin()
        }
        loading = false
    }

    LaunchedEffect(Unit) {
        val latestUpdate = appUpdateRepository.fetchLatestUpdate().getOrNull()
        if (latestUpdate != null &&
            latestUpdate.versionCode > BuildConfig.VERSION_CODE &&
            (latestUpdate.isForceUpdate || appSettings.dismissedUpdateVersionCode != latestUpdate.versionCode)
        ) {
            availableUpdate = latestUpdate
        }
    }

    LaunchedEffect(dashboard != null, backgroundKeepAliveEnabled) {
        if (dashboard != null && backgroundKeepAliveEnabled) {
            VtopKeepAliveService.start(context.applicationContext)
        } else {
            VtopKeepAliveService.stop(context.applicationContext)
        }
    }

    LaunchedEffect(pagerState.currentPage, dashboard != null) {
        if (dashboard != null && selectedTab != 4) {
            selectedTab = pagerState.currentPage
        }
    }

    LaunchedEffect(dashboard?.profile?.registrationNumber, sessionGeneration) {
        val activeGeneration = sessionGeneration
        val profile = dashboard?.profile ?: return@LaunchedEffect
        val registrationNumber = profile.registrationNumber?.uppercase()?.takeIf { it.isNotBlank() }
            ?: return@LaunchedEffect

        while (dashboard != null && activeGeneration == sessionGeneration) {
            appUsageRepository.submitHeartbeat(
                ActiveUserHeartbeat(
                    registrationNumber = registrationNumber,
                    studentName = profile.name,
                    appVersion = BuildConfig.VERSION_NAME,
                    lastSeen = Instant.now().toString()
                )
            )
            delay(APP_USAGE_HEARTBEAT_INTERVAL_MILLIS)
        }
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
                    selectedTab = if (selectedTab == 4) 3 else selectedTab,
                    swipePosition = if (selectedTab == 4) {
                        3f
                    } else {
                        (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 3f)
                    },
                    onSelected = { tab ->
                        selectedTab = tab
                        scope.launch {
                            pagerState.animateScrollToPage(tab)
                        }
                    }
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
            if (darkMode) {
                AmoledNeonBackground()
            }
            if (dashboard == null && showLanding) {
                LandingScreen(
                    onContinue = {
                        appSettings.hasSeenLanding = true
                        showLanding = false
                        if (challenge == null) {
                            scope.launch {
                                loading = true
                                challenge = repository.prepareLogin()
                                loading = false
                            }
                        }
                    }
                )
            } else if (dashboard == null) {
                LoginScreen(
                    challenge = challenge,
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
            } else if (selectedTab == 4) {
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
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> DashboardScreen(
                            dashboard = dashboard!!,
                            loading = loading,
                            onSemesterSelected = selectSemester,
                            onRefresh = {
                                scope.launch {
                                    loading = true
                                    message = null
                                    dashboard = repository.refreshDashboard()
                                    loading = false
                                }
                            }
                        )
                        1 -> ClassesScreen(
                            dashboard = dashboard!!
                        )
                        2 -> GradesScreen(
                            dashboard = dashboard!!,
                            isActive = selectedTab == 2 && pagerState.settledPage == 2,
                            onSemesterSelected = selectSemester
                        )
                        3 -> ProfileSettingsScreen(
                            dashboard = dashboard!!,
                            darkMode = darkMode,
                            appearanceTheme = appearanceTheme,
                            accentColor = accentColor,
                            backgroundKeepAliveEnabled = backgroundKeepAliveEnabled,
                            onToggleTheme = onToggleTheme,
                            onAppearanceThemeSelected = onAppearanceThemeSelected,
                            onAccentColorSelected = onAccentColorSelected,
                            onBackgroundKeepAliveChanged = setBackgroundKeepAlive,
                            onOpenPortal = openPortalWindow,
                            onLogout = logout
                        )
                    }
                }
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

            availableUpdate?.let { update ->
                UpdatePromptDialog(
                    update = update,
                    onDownload = {
                        val targetUrl = update.targetUrl
                        if (targetUrl.isNullOrBlank()) {
                            message = "Update link is not available yet."
                            if (!update.isForceUpdate) {
                                availableUpdate = null
                            }
                        } else {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }.onFailure {
                                message = "Could not open the update link."
                            }
                        }
                    },
                    onDismiss = {
                        if (!update.isForceUpdate) {
                            appSettings.dismissedUpdateVersionCode = update.versionCode
                            availableUpdate = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AmoledNeonBackground() {
    val accent = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "amoled-neon")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "neon-drift"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon-pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        fun drawBeam(progress: Float, yFraction: Float, lengthFraction: Float, alphaScale: Float) {
            val beamLength = canvasWidth * lengthFraction
            val travel = canvasWidth + beamLength * 2f
            val x = -beamLength + ((progress % 1f) * travel)
            val start = Offset(x, canvasHeight * yFraction)
            val end = Offset(x + beamLength, canvasHeight * (yFraction + 0.10f))
            val glow = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    accent.copy(alpha = 0.08f * pulse * alphaScale),
                    accent.copy(alpha = 0.30f * pulse * alphaScale),
                    accent.copy(alpha = 0.08f * pulse * alphaScale),
                    Color.Transparent
                ),
                start = start,
                end = end
            )
            drawLine(
                brush = glow,
                start = start,
                end = end,
                strokeWidth = 30.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                brush = glow,
                start = start,
                end = end,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawBeam(drift, 0.10f, 0.78f, 1.18f)
        drawBeam((drift + 0.34f) % 1f, 0.44f, 0.64f, 0.92f)
        drawBeam((drift + 0.68f) % 1f, 0.78f, 0.72f, 1.0f)

        val topLineAlpha = 0.12f + (0.08f * pulse)
        drawLine(
            color = accent.copy(alpha = topLineAlpha),
            start = Offset(0f, canvasHeight * 0.035f),
            end = Offset(canvasWidth, canvasHeight * 0.02f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = accent.copy(alpha = topLineAlpha * 0.72f),
            start = Offset(canvasWidth * 0.08f, canvasHeight),
            end = Offset(canvasWidth * 0.92f, canvasHeight * 0.96f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun AcademicBottomBar(
    selectedTab: Int,
    swipePosition: Float,
    onSelected: (Int) -> Unit
) {
    val dockItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f),
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onSelected(0) },
            icon = {
                DockIcon(
                    tabIndex = 0,
                    swipePosition = swipePosition
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                }
            },
            label = { Text("Home") },
            colors = dockItemColors
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onSelected(1) },
            icon = {
                DockIcon(
                    tabIndex = 1,
                    swipePosition = swipePosition
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                }
            },
            label = { Text("Classes") },
            colors = dockItemColors
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onSelected(2) },
            icon = {
                DockIcon(
                    tabIndex = 2,
                    swipePosition = swipePosition
                ) {
                    Icon(Icons.Default.Grade, contentDescription = null)
                }
            },
            label = { Text("Grades") },
            colors = dockItemColors
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onSelected(3) },
            icon = {
                DockIcon(
                    tabIndex = 3,
                    swipePosition = swipePosition
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            },
            label = { Text("Profile") },
            colors = dockItemColors
        )
    }
}

@Composable
private fun DockIcon(
    tabIndex: Int,
    swipePosition: Float,
    content: @Composable () -> Unit
) {
    val focus = (1f - abs(swipePosition - tabIndex)).coerceIn(0f, 1f)
    val scale = 0.92f + (0.22f * focus)
    val alpha = 0.62f + (0.38f * focus)

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            translationY = -3f * focus
        },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun LandingScreen(onContinue: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onContinue()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BrandLogo(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(82.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Your VTOP, cleaned up.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Track attendance, classes, grades, and requests from one lighter academic dashboard.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun LoginScreen(
    challenge: LoginChallenge?,
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
            BrandLogo(
                modifier = Modifier
                    .fillMaxWidth(0.76f)
                    .height(78.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            AcademicCard(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 400.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
            ) {
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
    }
}

@Composable
private fun SavedCredentialCard(
    username: String,
    onForgetCredentials: () -> Unit
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        baseTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
private fun UpdatePromptDialog(
    update: AppUpdate,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!update.isForceUpdate) {
                onDismiss()
            }
        },
        title = { Text(update.title.ifBlank { "Update available" }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip(text = "Version ${update.versionName}")
                Text(
                    text = update.message.ifBlank { "A new version of Gamma is available." },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (update.isForceUpdate) {
                    Text(
                        text = "This update is required to continue using the app.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text(if (update.targetUrl.isNullOrBlank()) "OK" else "Download update")
            }
        },
        dismissButton = if (update.isForceUpdate) {
            null
        } else {
            {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}

@Composable
private fun BrandLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Image(
        painter = painterResource(id = R.drawable.gamma_logo),
        contentDescription = "Gamma for VTOP",
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(color)
    )
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
        GlassPanel(
            modifier = Modifier
                .weight(1f)
                .height(82.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
            baseTint = MaterialTheme.colorScheme.surfaceVariant
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    dashboard: DashboardSnapshot,
    loading: Boolean,
    onSemesterSelected: (String, String) -> Unit,
    onRefresh: () -> Unit
) {
    val registrationNumber = dashboard.profile.registrationNumber ?: "Signed in to VTOP"

    PullToRefreshBox(
        isRefreshing = loading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                DashboardHeader(
                    greeting = dashboard.profile.greeting,
                    registrationNumber = registrationNumber
                )
            }

            item {
                SemesterSelectorCard(
                    dashboard = dashboard,
                    onSemesterSelected = onSemesterSelected
                )
            }

            item {
                SectionHeader(
                    title = "Next Class",
                    trailing = "TODAY",
                    icon = Icons.Default.EventNote
                )
                Spacer(modifier = Modifier.height(8.dp))
                NextClassCard(dashboard = dashboard)
            }

            item {
                AttendanceSection(courses = dashboard.attendance)
            }

            if (BuildConfig.STARTAPP_APP_ID.isNotBlank()) {
                item {
                    StartIoBannerAd()
                }
            }
        }
    }
}

@Composable
private fun ClassesScreen(
    dashboard: DashboardSnapshot
) {
    val today = remember { LocalDate.now() }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var selectedDate by remember { mutableStateOf(today) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000)
        }
    }
    val dateOptions = remember(today) {
        (-2..4).map { offset ->
            val date = today.plusDays(offset.toLong())
            ScheduleDateOption(
                date = date,
                dayLabel = date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.ENGLISH).uppercase(Locale.ENGLISH),
                dateLabel = date.dayOfMonth.toString()
            )
        }
    }
    val selectedDayKey = remember(selectedDate) {
        selectedDate.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.ENGLISH).uppercase(Locale.ENGLISH)
    }
    val classesForSelectedDay = remember(dashboard.timetable, selectedDayKey) {
        dashboard.timetable
            .filter { classSlot -> classSlot.matchesDay(selectedDayKey) }
            .sortedBy { it.time.startTime() }
    }
    val visibleClasses = if (classesForSelectedDay.isNotEmpty()) {
        classesForSelectedDay
    } else {
        dashboard.timetable.sortedBy { "${it.day.orEmpty()} ${it.time.startTime()} ${it.code.orEmpty()}" }
    }
    val marksByCode = remember(dashboard.marks) {
        dashboard.marks.associateBy { it.code.uppercase() }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ClassesTopHeader()
        }
        item {
            ScheduleDateStrip(
                dates = dateOptions,
                selectedDate = selectedDate,
                onSelected = { selectedDate = it }
            )
        }
        item {
            TodayScheduleHeader(
                count = visibleClasses.size,
                subtitle = if (classesForSelectedDay.isNotEmpty()) {
                    selectedDate.dayOfWeek.getDisplayName(DateTextStyle.FULL, Locale.ENGLISH)
                } else {
                    dashboard.selectedTimetableSemester?.label ?: "Selected semester"
                }
            )
        }
        if (dashboard.timetable.isEmpty()) {
            item {
                EmptyState("No timetable classes found in the loaded VTOP data.")
            }
        } else {
            items(
                items = visibleClasses,
                key = { "${it.day.orEmpty()}-${it.time}-${it.code.orEmpty()}-${it.venue}" }
            ) { classSlot ->
                ScheduleClassCard(
                    classSlot = classSlot,
                    marks = classSlot.code?.uppercase()?.let { marksByCode[it] },
                    selectedDate = selectedDate,
                    now = now
                )
            }
        }
        if (BuildConfig.STARTAPP_APP_ID.isNotBlank()) {
            item {
                StartIoBannerAd()
            }
        }
    }
}

@Composable
private fun GradesScreen(
    dashboard: DashboardSnapshot,
    isActive: Boolean,
    onSemesterSelected: (String, String) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findMainActivity() }
    var gradesUnlocked by rememberSaveable { mutableStateOf(false) }
    var authAttempted by rememberSaveable { mutableStateOf(false) }
    var authMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val biometricStatus = remember(context) {
        BiometricManager.from(context).canAuthenticate(GRADES_AUTHENTICATORS)
    }
    val requestUnlock = {
        authAttempted = true
        when (biometricStatus) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val hostActivity = activity
                if (hostActivity == null) {
                    authMessage = "Biometric unlock is unavailable right now."
                } else {
                    hostActivity.showGradesBiometricPrompt(
                        onUnlocked = {
                            gradesUnlocked = true
                            authMessage = null
                        },
                        onError = { reason ->
                            authMessage = reason.ifBlank { "Grades stayed locked." }
                        },
                        onFailed = {
                            authMessage = "Fingerprint or face did not match. Try again."
                        }
                    )
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                authMessage = "Set up fingerprint, face unlock, or a phone PIN/password to protect grades."
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                authMessage = "This device does not support biometric unlock."
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                authMessage = "Biometric unlock is temporarily unavailable."
            }
            else -> {
                authMessage = "Biometric unlock is unavailable right now."
            }
        }
    }

    LaunchedEffect(biometricStatus, gradesUnlocked, isActive) {
        if (isActive && !gradesUnlocked && !authAttempted && biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) {
            requestUnlock()
        }
    }

    if (!gradesUnlocked) {
        GradesSecurityScreen(
            status = biometricStatus,
            message = authMessage,
            onUnlock = requestUnlock
        )
        return
    }

    var selectedGradeSection by remember { mutableStateOf("Current") }
    val marksByCode = remember(dashboard.marks) {
        dashboard.marks.associateBy { it.code.uppercase() }
    }
    val historyTerms = remember(dashboard.gradeHistory) {
        dashboard.gradeHistory
            .groupBy { it.examMonth?.ifBlank { "Earlier" } ?: "Earlier" }
            .toList()
            .sortedWith(
                compareByDescending<Pair<String, List<GradeCourse>>> { termSortValue(it.first) }
                    .thenBy { it.first }
            )
    }
    val historyGradeCounts = remember(dashboard.gradeHistory) {
        dashboard.gradeHistory.toGradeCounts()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SimpleScreenHeader(
                title = when (selectedGradeSection) {
                    "History" -> "Grade History"
                    else -> "Grades"
                },
                subtitle = when (selectedGradeSection) {
                    "History" -> "Review your academic performance."
                    else -> dashboard.selectedGradeSemester?.label ?: dashboard.selectedAttendanceSemester?.label ?: "Selected semester"
                }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { selectedGradeSection = "Current" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGradeSection == "Current") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedGradeSection == "Current") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Current")
                }
                Button(
                    onClick = { selectedGradeSection = "History" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGradeSection == "History") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedGradeSection == "History") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("History")
                }
            }
        }
        if (selectedGradeSection == "History") {
            item {
                CumulativeGpaCard(
                    cgpa = dashboard.cgpa ?: estimateTermGpa(dashboard.gradeHistory),
                    historyCount = dashboard.gradeHistory.size
                )
            }
            item {
                GradeDistributionCard(gradeCounts = historyGradeCounts)
            }
            item {
                CreditSummaryCard(courses = dashboard.gradeHistory, totalCredits = dashboard.totalCredits)
            }
            if (dashboard.gradeHistory.isEmpty()) {
                item {
                    EmptyState("No grade history found in the loaded VTOP data.")
                }
            } else {
                historyTerms.forEach { (term, courses) ->
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
        if (BuildConfig.STARTAPP_APP_ID.isNotBlank()) {
            item {
                StartIoBannerAd()
            }
        }
    }
}

@Composable
private fun GradesSecurityScreen(
    status: Int,
    message: String?,
    onUnlock: () -> Unit
) {
    val statusText = when (status) {
        BiometricManager.BIOMETRIC_SUCCESS -> "Use fingerprint, face unlock, or your phone PIN/password to view grades."
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Set up fingerprint, face unlock, or a phone PIN/password on this device."
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Use your phone PIN/password to view grades."
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Secure unlock is temporarily unavailable."
        else -> "Secure unlock is unavailable right now."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Text(
                text = "Unlock Grades",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = message ?: statusText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
            Button(
                onClick = onUnlock,
                enabled = status == BiometricManager.BIOMETRIC_SUCCESS,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Unlock with biometrics or PIN")
            }
        }
    }
}

@Composable
private fun SimpleScreenHeader(
    title: String,
    subtitle: String
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
    }
}

@Composable
private fun ClassesTopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandLogo(
            modifier = Modifier
                .width(132.dp)
                .height(36.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = "Classes",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ScheduleDateStrip(
    dates: List<ScheduleDateOption>,
    selectedDate: LocalDate,
    onSelected: (LocalDate) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 1.dp)
    ) {
        items(
            items = dates,
            key = { it.date.toString() }
        ) { option ->
            ScheduleDateChip(
                option = option,
                selected = option.date == selectedDate,
                onClick = { onSelected(option.date) }
            )
        }
    }
}

@Composable
private fun ScheduleDateChip(
    option: ScheduleDateOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(width = 48.dp, height = 58.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = option.dayLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = option.dateLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TodayScheduleHeader(
    count: Int,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = "Today's Schedule",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "$count Classes remaining today • $subtitle",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScheduleClassCard(
    classSlot: TimetableClass,
    marks: CourseMarks?,
    selectedDate: LocalDate,
    now: LocalDateTime
) {
    var showDetails by remember(classSlot.code, classSlot.time, classSlot.venue) { mutableStateOf(false) }
    val completed = remember(classSlot.time, selectedDate, now) {
        classSlot.isCompletedOn(selectedDate, now)
    }

    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text(classSlot.name.ifBlank { "Class details" }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(label = "Teacher", value = marks?.faculty?.ifBlank { null } ?: "Check VTOP")
                    DetailRow(label = "Class number", value = classSlot.venue.ifBlank { "Check VTOP" })
                    DetailRow(label = "Time", value = classSlot.time.ifBlank { "Check VTOP" })
                    DetailRow(label = "Day", value = classSlot.day.orEmpty().ifBlank { "Check VTOP" })
                    DetailRow(label = "Course code", value = classSlot.code.orEmpty().ifBlank { "Check VTOP" })
                    DetailRow(label = "Slot", value = classSlot.slot.orEmpty().ifBlank { "Check VTOP" })
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetails = false }) {
                    Text("Close")
                }
            }
        )
    }

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetails = true },
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.42f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = classSlot.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = marks?.title?.ifBlank { null } ?: classSlot.name.ifBlank { "Scheduled class" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                ClassStatusPill(
                    text = if (completed) "DONE" else classSlot.statusLabel(),
                    completed = completed
                )
            }
            Text(
                text = classSlot.time,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                ScheduleMetaText(text = classSlot.venue.ifBlank { "Venue TBA" })
                ScheduleMetaText(text = classSlot.day.orEmpty().ifBlank { "Check VTOP" })
            }
        }
    }
}

@Composable
private fun ClassStatusPill(text: String, completed: Boolean = false) {
    val containerColor = if (completed) SuccessGreen else MaterialTheme.colorScheme.onBackground
    val contentColor = if (completed) Color.White else MaterialTheme.colorScheme.background
    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(
            1.dp,
            if (completed) SuccessGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (completed) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor
                )
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ScheduleMetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
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
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = marks?.assessments?.isNotEmpty() == true) { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = course.title.ifBlank { course.code },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CourseCodePill(text = course.code)
                        Text(
                            text = course.displayCreditsLabel(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                GradeBadge(grade = course.grade)
            }
            AnimatedVisibility(expanded && marks != null && marks.assessments.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 13.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = course.courseType.ifBlank { "Marks" },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    marks?.let { courseMarks ->
                        courseMarks.assessments.forEach { mark ->
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
private fun CourseCodePill(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.46f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun GradeBadge(grade: String) {
    val highlighted = grade.equals("S", true) || grade.startsWith("A", ignoreCase = true)
    Surface(
        modifier = Modifier.size(width = 52.dp, height = 52.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = grade.ifBlank { "--" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
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
private fun GradeDistributionCard(gradeCounts: List<Pair<String, Int>>) {
    AcademicCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("GRADE DISTRIBUTION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Completed course grades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
                Text(
                    text = gradeCounts.sumOf { it.second }.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                gradeCounts.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (grade, count) ->
                            GradeCountTile(
                                grade = grade,
                                count = count,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeCountTile(
    grade: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val active = count > 0
    val glassTint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        modifier = modifier,
        shape = shape,
        color = glassTint.copy(alpha = if (active) 0.13f else 0.28f),
        border = BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.62f) else Color.White.copy(alpha = 0.16f)
        )
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (active) 0.18f else 0.10f),
                        glassTint.copy(alpha = if (active) 0.10f else 0.05f),
                        Color.White.copy(alpha = 0.03f)
                    )
                ),
                shape = shape
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(grade, fontWeight = FontWeight.Black)
                Text(
                    count.toString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CreditSummaryCard(courses: List<GradeCourse>, totalCredits: String?) {
    val fallbackTotalCredits = courses.sumOf { it.numericCredits() }.formatCredits()
    val earnedCredits = courses.filter { it.isEarnedCredit() }.sumOf { it.numericCredits() }
    val gradedCredits = courses.filter { it.grade.isNotBlank() && !it.grade.equals("P", true) }.sumOf { it.numericCredits() }
    AcademicCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("CREDIT OVERVIEW", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CreditMetric(label = "Total Credits", value = totalCredits?.ifBlank { null } ?: fallbackTotalCredits)
                CreditMetric(label = "Earned", value = earnedCredits.formatCredits())
                CreditMetric(label = "Graded", value = gradedCredits.formatCredits())
            }
        }
    }
}

@Composable
private fun CreditMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
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
    greeting: String,
    registrationNumber: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandLogo(
                modifier = Modifier
                    .width(148.dp)
                    .height(38.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clickable { expanded = !expanded },
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
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Hide semester list" else "Show semester list"
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { option ->
                    val selectedOption = option.id == selected.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                onSelected(option)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedOption) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (selectedOption) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.label,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedOption) FontWeight.Black else FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (selectedOption) {
                                Text(
                                    text = "Selected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSettingsScreen(
    dashboard: DashboardSnapshot,
    darkMode: Boolean,
    appearanceTheme: AppearanceTheme,
    accentColor: AccentColor,
    backgroundKeepAliveEnabled: Boolean,
    onToggleTheme: () -> Unit,
    onAppearanceThemeSelected: (AppearanceTheme) -> Unit,
    onAccentColorSelected: (AccentColor) -> Unit,
    onBackgroundKeepAliveChanged: (Boolean) -> Unit,
    onOpenPortal: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf("Settings") }
    var showFeatureRequestDialog by remember { mutableStateOf(false) }
    var featureRequestMessage by remember { mutableStateOf<String?>(null) }
    var lastFeatureRequestSuccessAt by remember { mutableStateOf(0L) }
    val featureRequestRepository = remember { FeatureRequestRepository() }
    val context = LocalContext.current
    val displayName = dashboard.profile.name
        ?: dashboard.profile.greeting.removePrefix("Welcome back").trim().ifBlank { "VTOP Student" }
    val registrationNumber = dashboard.profile.registrationNumber ?: "Signed in"

    if (showFeatureRequestDialog) {
        FeatureRequestDialog(
            profileName = dashboard.profile.name,
            registrationNumber = dashboard.profile.registrationNumber,
            repository = featureRequestRepository,
            lastSuccessAtMillis = lastFeatureRequestSuccessAt,
            onSuccess = {
                lastFeatureRequestSuccessAt = System.currentTimeMillis()
                featureRequestMessage = "Request sent. Thanks for helping improve Gamma."
                Toast.makeText(
                    context,
                    "Request sent. Thanks for helping improve Gamma.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showFeatureRequestDialog = false },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileHeader(
                displayName = displayName,
                registrationNumber = registrationNumber
            )
        }
        item {
            ProfileTabs(
                selected = selectedSection,
                onSelected = { selectedSection = it }
            )
        }

        if (selectedSection == "Settings") {
            item {
                AppearanceSettingsCard(
                    darkMode = darkMode,
                    selectedTheme = appearanceTheme,
                    selectedAccent = accentColor,
                    onToggleTheme = onToggleTheme,
                    onThemeSelected = onAppearanceThemeSelected,
                    onAccentSelected = onAccentColorSelected
                )
            }
            item {
                ConnectivitySettingsCard(
                    enabled = backgroundKeepAliveEnabled,
                    onEnabledChange = onBackgroundKeepAliveChanged
                )
            }
            item {
                FeatureRequestCard(
                    configured = featureRequestRepository.isConfigured,
                    message = featureRequestMessage,
                    onClick = {
                        featureRequestMessage = null
                        showFeatureRequestDialog = true
                    }
                )
            }
            item {
                PortalSettingsCard(onOpenPortal = onOpenPortal)
            }
            item {
                LogoutSettingsButton(
                    displayName = displayName,
                    onLogout = onLogout
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        } else {
            item {
                AboutProfileCard(
                    attendanceCount = dashboard.attendance.size,
                    gradeCount = dashboard.grades.size,
                    selectedSemester = dashboard.selectedAttendanceSemester?.label
                        ?: dashboard.selectedTimetableSemester?.label
                        ?: "Latest semester"
                )
            }
        }
        if (BuildConfig.STARTAPP_APP_ID.isNotBlank()) {
            item {
                StartIoBannerAd()
            }
        }
        item {
            AppVersionFooter()
        }
    }
}

@Composable
private fun StartIoBannerAd() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Sponsored",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f)
        )
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            factory = { context ->
                FrameLayout(context).apply {
                    addView(
                        Banner(context),
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun AppVersionFooter() {
    Text(
        text = "Gamma v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 18.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
    )
}

@Composable
private fun ProfileHeader(
    displayName: String,
    registrationNumber: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandLogo(
                modifier = Modifier
                    .width(152.dp)
                    .height(40.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Updates",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(text = registrationNumber)
                Text(
                    text = "VTOP Student",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileTabs(
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        listOf(
            "Settings" to Icons.Default.Settings,
            "About" to Icons.Default.Info
        ).forEach { (label, icon) ->
            Column(
                modifier = Modifier.clickable { onSelected(label) },
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (selected == label) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = if (selected == label) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .width(58.dp)
                        .height(1.dp)
                        .background(
                            if (selected == label) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                Color.Transparent
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun AppearanceSettingsCard(
    darkMode: Boolean,
    selectedTheme: AppearanceTheme,
    selectedAccent: AccentColor,
    onToggleTheme: () -> Unit,
    onThemeSelected: (AppearanceTheme) -> Unit,
    onAccentSelected: (AccentColor) -> Unit
) {
    AcademicCard(contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            SettingsSwitchRow(
                icon = { Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "AMOLED Dark Mode",
                subtitle = "Pure black background to save battery and reduce strain.",
                checked = darkMode,
                onCheckedChange = { onToggleTheme() }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppearanceTheme.entries.forEach { theme ->
                    ThemePreviewTile(
                        title = theme.label,
                        selected = selectedTheme == theme,
                        background = theme.previewBackground,
                        accent = theme.previewAccent,
                        onClick = { onThemeSelected(theme) }
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Accent color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 1.dp)
                ) {
                    items(
                        items = AccentColor.entries,
                        key = { it.storageValue }
                    ) { accent ->
                        AccentColorSwatch(
                            accent = accent,
                            selected = selectedAccent == accent,
                            darkMode = darkMode,
                            onClick = { onAccentSelected(accent) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectivitySettingsCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    AcademicCard(contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                text = "Connectivity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            SettingsSwitchRow(
                icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "VTOP Online",
                subtitle = "Enables VTOP to be logged in even when app is closed",
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
    }
}

@Composable
private fun FeatureRequestCard(
    configured: Boolean,
    message: String?,
    onClick: () -> Unit
) {
    AcademicCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconChip(icon = Icons.Default.Feedback)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Request a Feature", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text(
                            if (configured) {
                                "Send ideas, bugs, and improvements directly to Gamma."
                            } else {
                                "Feature requests are unavailable right now."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Request a feature")
            }
            message?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeatureRequestDialog(
    profileName: String?,
    registrationNumber: String?,
    repository: FeatureRequestRepository,
    lastSuccessAtMillis: Long,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val categories = remember { listOf("Feature", "Bug", "UI", "Backend", "Other") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val cooldownRemainingSeconds = remember(lastSuccessAtMillis) {
        (((lastSuccessAtMillis + FEATURE_REQUEST_COOLDOWN_MILLIS) - System.currentTimeMillis()).coerceAtLeast(0L) + 999L) / 1000L
    }
    val configured = repository.isConfigured
    val canSubmit = configured && cooldownRemainingSeconds == 0L && !submitting

    AlertDialog(
        onDismissRequest = {
            if (!submitting) onDismiss()
        },
        title = { Text("Request a Feature") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!configured) {
                    Text(
                        "Feature requests are unavailable right now.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                } else if (cooldownRemainingSeconds > 0L) {
                    Text(
                        "You can send another request in ${cooldownRemainingSeconds}s.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                AcademicTextField(
                    value = title,
                    onValueChange = {
                        title = it.take(80)
                        error = null
                    },
                    label = "Title"
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it.take(800)
                        error = null
                    },
                    label = { Text("Description") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                )
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clickable(enabled = !submitting) { categoryExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                        }
                    }
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canSubmit,
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedDescription = description.trim()
                    when {
                        trimmedTitle.length !in 4..80 -> error = "Title must be 4-80 characters."
                        trimmedDescription.length !in 10..800 -> error = "Description must be 10-800 characters."
                        else -> {
                            submitting = true
                            error = null
                            scope.launch {
                                val result = repository.submitFeatureRequest(
                                    FeatureRequestPayload(
                                        title = trimmedTitle,
                                        description = trimmedDescription,
                                        category = category,
                                        studentName = profileName,
                                        registrationNumber = registrationNumber,
                                        appVersion = BuildConfig.VERSION_NAME,
                                        createdAt = Instant.now().toString()
                                    )
                                )
                                submitting = false
                                if (result.isSuccess) {
                                    onSuccess()
                                    onDismiss()
                                } else {
                                    error = "Couldn't send request. Try again later."
                                }
                            }
                        }
                    }
                }
            ) {
                Text(if (submitting) "Sending..." else "Send")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !submitting,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PortalSettingsCard(onOpenPortal: () -> Unit) {
    AcademicCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenPortal),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsIconChip(icon = Icons.Default.OpenInNew)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Full VTOP", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(
                        "Open the original VTOP portal when you need it.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Open Full VTOP")
        }
    }
}

@Composable
private fun AboutProfileCard(
    attendanceCount: Int,
    gradeCount: Int,
    selectedSemester: String
) {
    AcademicCard(contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            DetailRow(label = "Selected semester", value = selectedSemester)
            DetailRow(label = "Attendance courses", value = attendanceCount.toString())
            DetailRow(label = "Grade courses", value = gradeCount.toString())
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                icon()
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = gammaSwitchColors()
        )
    }
}

@Composable
private fun gammaSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = MaterialTheme.colorScheme.primary,
    checkedIconColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
    uncheckedIconColor = MaterialTheme.colorScheme.surfaceVariant
)

@Composable
private fun SettingsIconChip(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(34.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ThemePreviewTile(
    title: String,
    selected: Boolean,
    background: Color,
    accent: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier = Modifier
            .size(width = 68.dp, height = 78.dp)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (selected) 0.32f else 0.22f),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (selected) 0.16f else 0.08f),
                            accent.copy(alpha = if (selected) 0.08f else 0.04f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    ),
                    shape = shape
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Surface(
                modifier = Modifier.size(width = 40.dp, height = 32.dp),
                shape = RoundedCornerShape(4.dp),
                color = background.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.62f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = CircleShape,
                        color = accent
                    ) {}
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AccentColorSwatch(
    accent: AccentColor,
    selected: Boolean,
    darkMode: Boolean,
    onClick: () -> Unit
) {
    val accentColor = accent.primary(darkMode)
    GlassPanel(
        modifier = Modifier
            .size(width = 74.dp, height = 58.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp),
        baseTint = accentColor.copy(alpha = if (selected) 0.42f else 0.22f),
        borderTint = if (selected) accentColor else MaterialTheme.colorScheme.primary
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Surface(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = accentColor,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
            ) {}
            Text(
                text = accent.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LogoutSettingsButton(
    displayName: String,
    onLogout: () -> Unit
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onLogout),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp),
        baseTint = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
        borderTint = MaterialTheme.colorScheme.error
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out $displayName",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BackgroundKeepAliveCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    AcademicCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Keep VTOP online",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Runs a foreground service with a persistent notification.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                colors = gammaSwitchColors()
            )
        }
    }
}

@Composable
private fun AttendanceSection(courses: List<AttendanceCourse>) {
    SectionHeader(
        title = "My Attendance",
        trailing = if (courses.isEmpty()) null else "${courses.size} courses",
        icon = Icons.Default.DonutLarge
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (courses.isEmpty()) {
        EmptyState("Attendance was not visible on the current VTOP page. Open Full VTOP once after login, then refresh.")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            courses.chunked(2).forEach { rowCourses ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCourses.forEach { course ->
                        AttendanceCourseCard(
                            course = course,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowCourses.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceCourseCard(
    course: AttendanceCourse,
    modifier: Modifier = Modifier
) {
    var showDetails by remember(course.code) { mutableStateOf(false) }
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

    if (showDetails) {
        AttendanceDetailDialog(
            course = course,
            onDismiss = { showDetails = false }
        )
    }

    AcademicCard(
        modifier = modifier
            .height(176.dp)
            .clickable { showDetails = true },
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            StatusChip(text = course.code)
        }
    }
}

@Composable
private fun NextClassCard(dashboard: DashboardSnapshot) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var showDetails by remember { mutableStateOf(false) }
    val cardState = remember(dashboard, now) {
        dashboard.resolveNextClassCardState(now)
    }
    val activeClass = cardState.activeClass
    val nextClass = cardState.nextClass
    val facultyDisplayName = nextClass?.faculty?.toDisplayNameCase().orEmpty()

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000)
        }
    }

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
                    DetailRow(label = "Faculty", value = facultyDisplayName.ifBlank { "Check VTOP" })
                    DetailRow(label = "Status", value = "${nextClass.statusLabel} • ${nextClass.countdownLabel}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetails = false }) {
                    Text("Close")
                }
            }
        )
    }

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = nextClass != null) { showDetails = true }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (nextClass == null) {
            EmptyState("No upcoming class found on the loaded page.")
        } else {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (activeClass != null && activeClass != nextClass) {
                    ActiveClassStrip(activeClass = activeClass)
                }
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
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(nextClass.statusLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(nextClass.timeLabel, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
                Text(
                    text = "${nextClass.statusLabel} • ${nextClass.countdownLabel}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                if (activeClass == null || activeClass == nextClass) {
                    LinearProgressIndicator(
                        progress = { nextClass.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                NextClassInfoGrid(
                    venue = nextClass.venue,
                    faculty = facultyDisplayName.ifBlank { "Check VTOP" },
                    day = nextClass.day
                )
            }
        }
    }
}

@Composable
private fun AttendanceDetailDialog(
    course: AttendanceCourse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = course.name.ifBlank { course.code },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${course.code} • ${course.percentage}%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        text = {
            if (course.records.isEmpty()) {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(16.dp),
                    baseTint = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "Daily attendance details are not loaded yet. Refresh once after opening attendance in VTOP.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(course.records, key = { "${it.date}-${it.slot}-${it.dayTime}-${it.status}" }) { record ->
                        AttendanceRecordRow(record = record)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AttendanceRecordRow(record: AttendanceRecord) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        baseTint = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(record.date, fontWeight = FontWeight.Black)
                Text(
                    listOf(record.dayTime, record.slot).filter { it.isNotBlank() }.joinToString(" • "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            AttendanceStatusChip(status = record.status)
        }
    }
}

@Composable
private fun AttendanceStatusChip(status: String) {
    val normalized = status.trim().lowercase(Locale.ENGLISH)
    val color = when {
        normalized.contains("present") -> SuccessGreen
        normalized.contains("absent") -> MaterialTheme.colorScheme.error
        normalized.contains("duty") -> WarmGold
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.72f))
    ) {
        Text(
            text = status.ifBlank { "--" },
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun ActiveClassStrip(activeClass: NextClassDisplay) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(text = activeClass.code ?: activeClass.slot ?: "LIVE")
                Text(
                    text = activeClass.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = activeClass.countdownLabel,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
        LinearProgressIndicator(
            progress = { activeClass.progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun NextClassInfoGrid(
    venue: String,
    faculty: String,
    day: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            NextClassMetaRow(
                icon = Icons.Default.LocationOn,
                label = "Venue",
                value = venue,
                modifier = Modifier.weight(1f)
            )
            NextClassMetaRow(
                icon = Icons.Default.School,
                label = "Faculty",
                value = faculty,
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                textAlign = TextAlign.End,
                trailingIcon = true
            )
        }
        day?.takeIf { it.isNotBlank() }?.let { classDay ->
            NextClassMetaRow(
                icon = Icons.Default.CalendarToday,
                label = "Day",
                value = classDay,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NextClassMetaRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    textAlign: TextAlign = TextAlign.Start,
    trailingIcon: Boolean = false
) {
    val iconContent: @Composable () -> Unit = {
        Surface(
            modifier = Modifier.size(30.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    val textContent: @Composable (Modifier) -> Unit = { textModifier ->
        Column(
            modifier = textModifier,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = horizontalAlignment
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                textAlign = textAlign
            )
            Text(
                text = value.ifBlank { "Check VTOP" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = textAlign
            )
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (trailingIcon) {
            textContent(Modifier.weight(1f))
            iconContent()
        } else {
            iconContent()
            textContent(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailing: String? = null,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    it,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
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
    GlassPanel(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    baseTint: Color = MaterialTheme.colorScheme.surface,
    borderTint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "glass-panel")
    val sheenPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glass-sheen"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glass-pulse"
    )
    val accent = MaterialTheme.colorScheme.primary
    val driftX = sin(sheenPhase) * 260f
    val driftY = cos(sheenPhase) * 90f
    val start = Offset(x = -160f + driftX, y = -180f + driftY)
    val end = Offset(x = 460f + driftX, y = 520f + driftY)

    Surface(
        modifier = modifier,
        shape = shape,
        color = baseTint.copy(alpha = 0.48f),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.34f * pulse),
                    borderTint.copy(alpha = 0.54f * pulse),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.11f * pulse),
                            accent.copy(alpha = 0.10f * pulse),
                            Color.White.copy(alpha = 0.035f),
                            baseTint.copy(alpha = 0.16f)
                        ),
                        start = start,
                        end = end
                    ),
                    shape = shape
                )
                .padding(contentPadding)
        ) {
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
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(14.dp),
        baseTint = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    var loadingPortal by remember { mutableStateOf(true) }
    var portalError by remember { mutableStateOf<String?>(null) }
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
                            loadingPortal = false
                            portalError = null
                            view.captureAfterLoad()
                            view.activateVtopShellIfBlank()
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request.isForMainFrame) {
                                loadingPortal = false
                                portalError = error.description?.toString()
                                    ?: "VTOP could not be loaded."
                            }
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.setSupportMultipleWindows(false)
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    settings.userAgentString = vtopDesktopUserAgent
                    loadingPortal = true
                    portalError = null
                    syncVtopCookiesAndLoad(portalUrl, cookieHeaders)
                }.also { webView.value = it }
            },
            update = { view ->
                view.syncVtopCookies(cookieHeaders)
                if (view.url.isNullOrBlank()) {
                    loadingPortal = true
                    portalError = null
                    view.syncVtopCookiesAndLoad(portalUrl, cookieHeaders)
                }
            }
        )
        if (loadingPortal) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Text("Loading VTOP")
                }
            }
        }
        portalError?.let { error ->
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(18.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("VTOP did not load", fontWeight = FontWeight.Black)
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = {
                        loadingPortal = true
                        portalError = null
                        webView.value?.syncVtopCookiesAndLoad(portalUrl, cookieHeaders)
                    }) {
                        Text("Retry")
                    }
                }
            }
        }
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
            FloatingActionButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(portalUrl)))
            }) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Open VTOP in browser")
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

private fun WebView.activateVtopShellIfBlank() {
    Handler(Looper.getMainLooper()).postDelayed({
        evaluateJavascript(
            """
                (function() {
                  if (window.__gammaVtopShellActivated) return "already";

                  function isVisible(element) {
                    if (!element) return false;
                    var style = window.getComputedStyle(element);
                    var rect = element.getBoundingClientRect();
                    return style.display !== "none" &&
                      style.visibility !== "hidden" &&
                      rect.width > 0 &&
                      rect.height > 0;
                  }

                  function visibleTextLength() {
                    var candidates = [
                      "#page-content-wrapper",
                      "#pageContent",
                      "#content",
                      "#main-content",
                      ".page-content",
                      ".content-wrapper",
                      "main"
                    ];
                    var text = "";
                    candidates.forEach(function(selector) {
                      var element = document.querySelector(selector);
                      if (isVisible(element)) text += " " + element.innerText;
                    });
                    if (!text.trim()) text = document.body.innerText || "";
                    text = text
                      .replace(/VIT/g, "")
                      .replace(/Bhopal Campus/g, "")
                      .replace(/Home/g, "")
                      .replace(/Quick Links/g, "")
                      .replace(/\s+/g, " ")
                      .trim();
                    return text.length;
                  }

                  if (visibleTextLength() > 120) return "content-visible";
                  window.__gammaVtopShellActivated = true;

                  function clickElement(element) {
                    if (!element || !isVisible(element)) return false;
                    element.dispatchEvent(new MouseEvent("click", {
                      bubbles: true,
                      cancelable: true,
                      view: window
                    }));
                    if (typeof element.click === "function") element.click();
                    return true;
                  }

                  function closestClickable(element) {
                    while (element && element !== document.body) {
                      var tag = (element.tagName || "").toLowerCase();
                      if (tag === "a" || tag === "button" || element.getAttribute("onclick")) return element;
                      element = element.parentElement;
                    }
                    return null;
                  }

                  var homeIcon = document.querySelector(
                    ".fa-home, .bi-house, .bi-house-door, [class*='home'], [title*='Home' i], [aria-label*='Home' i]"
                  );
                  if (clickElement(closestClickable(homeIcon))) return "home-clicked";

                  var menuIcons = Array.prototype.slice.call(document.querySelectorAll(
                    ".fa-bars, .bi-list, [class*='menu'], [data-bs-toggle='offcanvas'], [aria-controls*='menu' i]"
                  ));
                  for (var i = 0; i < menuIcons.length; i++) {
                    if (clickElement(closestClickable(menuIcons[i]))) return "menu-clicked";
                  }

                  var clickable = Array.prototype.slice.call(document.querySelectorAll("a, button"))
                    .filter(function(element) {
                      var label = (element.innerText || element.getAttribute("title") || element.getAttribute("aria-label") || "").toLowerCase();
                      return label.indexOf("home") >= 0 || label.indexOf("menu") >= 0 || label.indexOf("quick") >= 0;
                    })[0];
                  if (clickElement(clickable)) return "label-clicked";

                  return "blank-no-target";
                })();
            """.trimIndent(),
            null
        )
    }, 1800)
}

private fun WebView.syncVtopCookies(cookieHeaders: List<String>) {
    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(this@syncVtopCookies, true)
        cookieHeaders.forEach { cookie ->
            setCookie(vtopCookieUrl, cookie)
        }
        flush()
    }
}

private fun WebView.syncVtopCookiesAndLoad(
    url: String,
    cookieHeaders: List<String>
) {
    val cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    cookieManager.setAcceptThirdPartyCookies(this, true)

    if (cookieHeaders.isEmpty()) {
        loadUrl(url)
        return
    }

    var pendingCookies = cookieHeaders.size
    cookieHeaders.forEach { cookie ->
        cookieManager.setCookie(vtopCookieUrl, cookie) {
            pendingCookies -= 1
            if (pendingCookies == 0) {
                cookieManager.flush()
                Handler(Looper.getMainLooper()).post {
                    loadUrl(url)
                }
            }
        }
    }
}

private const val vtopCookieUrl = "https://vtop.vitbhopal.ac.in"
private const val vtopDesktopUserAgent =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"

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

private fun DashboardSnapshot.resolveNextClassCardState(now: LocalDateTime): NextClassCardState {
    val entries = timetable
        .flatMap { classSlot -> classSlot.timelineEntriesFrom(now) }
        .sortedBy { it.start }

    val activeEntry = entries.firstOrNull { entry ->
        !now.isBefore(entry.start) && now.isBefore(entry.end)
    }
    val activeClass = activeEntry?.toDisplay(
        dashboard = this,
        now = now,
        statusLabel = "ONGOING",
        targetTime = activeEntry.end,
        progress = activeEntry.progress(now)
    )

    val upcomingEntry = entries.firstOrNull { entry ->
        entry.start.isAfter(now) && entry != activeEntry
    }
    val upcomingClass = if (upcomingEntry != null) {
        val statusLabel = if (upcomingEntry.start.toLocalDate() == now.toLocalDate()) {
            "NEXT"
        } else {
            "TOMORROW"
        }
        val countdownLabel = if (statusLabel == "TOMORROW" && activeEntry == null) {
            "Classes for today are over"
        } else {
            Duration.between(now, upcomingEntry.start).formatCountdown("away")
        }
        upcomingEntry.toDisplay(
            dashboard = this,
            now = now,
            statusLabel = statusLabel,
            targetTime = upcomingEntry.start,
            progress = 0f,
            countdownLabel = countdownLabel
        )
    } else {
        nextClass?.let { fallback ->
        NextClassDisplay(
            name = fallback.name,
            venue = fallback.venue,
            faculty = fallback.code?.let { code ->
                marks.firstOrNull { it.code.equals(code, ignoreCase = true) }?.faculty
            }.orEmpty().ifBlank { "Check VTOP" },
            time = fallback.time,
            code = fallback.code,
            slot = fallback.slot,
            day = fallback.day,
            statusLabel = "NEXT",
            timeLabel = fallback.time.startTime(),
            countdownLabel = "Check VTOP",
            progress = 0f
        )
        }
    }

    return NextClassCardState(
        activeClass = activeClass,
        nextClass = upcomingClass ?: activeClass
    )
}

private fun TimetableClass.timelineEntriesFrom(now: LocalDateTime): List<ClassTimelineEntry> {
    val dayOfWeek = day.toDayOfWeek() ?: return emptyList()
    val (startTime, endTime) = time.toTimeRange() ?: return emptyList()
    val today = now.toLocalDate()

    return (0..7).mapNotNull { offset ->
        val date = today.plusDays(offset.toLong())
        if (date.dayOfWeek != dayOfWeek) return@mapNotNull null

        ClassTimelineEntry(
            classSlot = this,
            start = LocalDateTime.of(date, startTime),
            end = LocalDateTime.of(date, endTime)
        )
    }
}

private fun ClassTimelineEntry.toDisplay(
    dashboard: DashboardSnapshot,
    now: LocalDateTime,
    statusLabel: String,
    targetTime: LocalDateTime,
    progress: Float,
    countdownLabel: String? = null
): NextClassDisplay {
    val className = classSlot.readableName(dashboard)
    val timeLabel = if (statusLabel == "ONGOING") {
        end.toLocalTime().formatClassTime()
    } else {
        start.toLocalTime().formatClassTime()
    }
    return NextClassDisplay(
        name = className,
        venue = classSlot.venue,
        faculty = classSlot.facultyName(dashboard),
        time = classSlot.time,
        code = classSlot.code,
        slot = classSlot.slot,
        day = classSlot.day,
        statusLabel = statusLabel,
        timeLabel = timeLabel,
        countdownLabel = countdownLabel ?: Duration.between(now, targetTime).formatCountdown(
            if (statusLabel == "ONGOING") "left" else "away"
        ),
        progress = progress
    )
}

private fun TimetableClass.readableName(dashboard: DashboardSnapshot): String {
    val matchingCourse = dashboard.attendance.firstOrNull { course ->
        code != null && course.code.equals(code, ignoreCase = true)
    }
    return matchingCourse?.name ?: name
}

private fun TimetableClass.facultyName(dashboard: DashboardSnapshot): String =
    code?.let { courseCode ->
        dashboard.marks.firstOrNull { marks ->
            marks.code.equals(courseCode, ignoreCase = true)
        }?.faculty
    }.orEmpty().ifBlank { "Check VTOP" }

private fun ClassTimelineEntry.progress(now: LocalDateTime): Float {
    val totalMillis = Duration.between(start, end).toMillis().coerceAtLeast(1L)
    val elapsedMillis = Duration.between(start, now).toMillis().coerceIn(0L, totalMillis)
    return elapsedMillis.toFloat() / totalMillis.toFloat()
}

private fun String?.toDayOfWeek(): DayOfWeek? {
    val normalized = orEmpty().uppercase(Locale.ENGLISH)
    return when {
        normalized.contains("MON") -> DayOfWeek.MONDAY
        normalized.contains("TUE") -> DayOfWeek.TUESDAY
        normalized.contains("WED") -> DayOfWeek.WEDNESDAY
        normalized.contains("THU") -> DayOfWeek.THURSDAY
        normalized.contains("FRI") -> DayOfWeek.FRIDAY
        normalized.contains("SAT") -> DayOfWeek.SATURDAY
        normalized.contains("SUN") -> DayOfWeek.SUNDAY
        else -> null
    }
}

private fun String.toTimeRange(): Pair<LocalTime, LocalTime>? {
    val matches = Regex("(\\d{1,2}):(\\d{2})\\s*([AaPp][Mm])?")
        .findAll(this)
        .toList()
    if (matches.size < 2) return null

    val start = matches[0].toLocalTimeOrNull() ?: return null
    val end = matches[1].toLocalTimeOrNull() ?: return null
    return if (end.isAfter(start)) start to end else null
}

private fun MatchResult.toLocalTimeOrNull(): LocalTime? = runCatching {
    var hour = groupValues[1].toInt()
    val minute = groupValues[2].toInt()
    val meridiem = groupValues.getOrNull(3).orEmpty().uppercase(Locale.ENGLISH)

    if (meridiem == "PM" && hour != 12) hour += 12
    if (meridiem == "AM" && hour == 12) hour = 0

    LocalTime.of(hour, minute)
}.getOrNull()

private fun LocalTime.formatClassTime(): String =
    String.format(
        Locale.ENGLISH,
        "%d:%02d %s",
        if (hour % 12 == 0) 12 else hour % 12,
        minute,
        if (hour < 12) "AM" else "PM"
    )

private fun Duration.formatCountdown(suffix: String): String {
    val minutes = toMinutes().coerceAtLeast(0L)
    val days = minutes / (24L * 60L)
    val hours = (minutes % (24L * 60L)) / 60L
    val mins = minutes % 60L
    val value = when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${mins}m"
        mins > 0 -> "${mins}m"
        else -> "Now"
    }
    return if (value == "Now") value else "$value $suffix"
}

private fun String.startTime(): String =
    substringBefore("-").trim().ifBlank { this }

private fun String.toDisplayNameCase(): String {
    val cleaned = trim().replace(Regex("\\s+"), " ")
    if (cleaned.isBlank() || cleaned.equals("Check VTOP", ignoreCase = true)) return cleaned

    return Regex("[A-Za-z]+").replace(cleaned) { match ->
        val word = match.value
        if (word.length == 1) {
            word.uppercase(Locale.ENGLISH)
        } else {
            word.lowercase(Locale.ENGLISH).replaceFirstChar { first ->
                first.titlecase(Locale.ENGLISH)
            }
        }
    }
}

private fun TimetableClass.matchesDay(dayKey: String): Boolean {
    val normalizedDay = day.orEmpty().uppercase(Locale.ENGLISH)
    return normalizedDay.contains(dayKey) || normalizedDay.contains(dayKey.take(3))
}

private fun TimetableClass.isCompletedOn(selectedDate: LocalDate, now: LocalDateTime): Boolean {
    val endTime = time.toTimeRange()?.second ?: return false
    return when {
        selectedDate.isBefore(now.toLocalDate()) -> true
        selectedDate.isAfter(now.toLocalDate()) -> false
        else -> now.toLocalTime().isAfter(endTime) || now.toLocalTime() == endTime
    }
}

private fun TimetableClass.statusLabel(): String =
    if (time.isBlank()) "CLASS" else "NEXT"

private fun GradeCourse.displayCreditsLabel(): String {
    val numericCredits = Regex("\\d+(?:\\.\\d+)?")
        .findAll(credits)
        .map { it.value }
        .toList()
    val creditsValue = numericCredits.lastOrNull()
        ?: credits.takeIf { it.isNotBlank() }
        ?: return "Credits"
    return "$creditsValue Credits"
}

private fun GradeCourse.numericCredits(): Double =
    Regex("\\d+(?:\\.\\d+)?")
        .find(credits)
        ?.value
        ?.toDoubleOrNull()
        ?: 0.0

private fun GradeCourse.isEarnedCredit(): Boolean {
    val normalized = grade.trim().uppercase(Locale.US)
    return normalized.isNotBlank() && normalized !in setOf("F", "N", "N1", "N2", "N3", "N4")
}

private fun Double.formatCredits(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }

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

private fun List<GradeCourse>.toGradeCounts(): List<Pair<String, Int>> {
    val standardOrder = listOf("S", "A", "B", "C", "D", "E", "F", "P", "N")
    val counts = groupingBy { course ->
        course.grade.trim().uppercase(Locale.US).ifBlank { "--" }
    }.eachCount()
    val extraGrades = counts.keys
        .filterNot { it in standardOrder }
        .sorted()
    return (standardOrder + extraGrades).map { grade -> grade to (counts[grade] ?: 0) }
}

private fun termSortValue(term: String): Int {
    val normalized = term.lowercase(Locale.US)
    val year = Regex("""20\d{2}""")
        .findAll(normalized)
        .mapNotNull { it.value.toIntOrNull() }
        .maxOrNull()
        ?: return 0
    val monthRank = when {
        normalized.contains("december") || normalized.contains(" dec ") || normalized.contains("winter") -> 12
        normalized.contains("november") || normalized.contains(" nov ") -> 11
        normalized.contains("october") || normalized.contains(" oct ") -> 10
        normalized.contains("september") || normalized.contains(" sept ") || normalized.contains(" sep ") || normalized.contains("fall") || normalized.contains("autumn") -> 9
        normalized.contains("august") || normalized.contains(" aug ") -> 8
        normalized.contains("july") || normalized.contains(" jul ") -> 7
        normalized.contains("june") || normalized.contains(" jun ") -> 6
        normalized.contains("may") || normalized.contains("summer") -> 5
        normalized.contains("april") || normalized.contains(" apr ") -> 4
        normalized.contains("march") || normalized.contains(" mar ") -> 3
        normalized.contains("february") || normalized.contains(" feb ") -> 2
        normalized.contains("january") || normalized.contains(" jan ") || normalized.contains("spring") -> 1
        else -> 0
    }
    return year * 100 + monthRank
}
