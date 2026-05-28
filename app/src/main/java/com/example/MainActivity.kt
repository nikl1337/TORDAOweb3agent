package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AgentViewModel
import com.example.ui.ViewModelFactory
import com.example.data.MintedNft
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll

// Premium Theme Palette
val SolDarkBg = Color(0xFF0C0D14)
val SolDarkCard = Color(0xFF141624)
val SolPurple = Color(0xFF9945FF)
val SolGreen = Color(0xFF14F195)
val SolTextWhite = Color(0xFFF0F2FA)
val SolSecGrey = Color(0xFF8D90A5)
val SolRed = Color(0xFFFF4B72)
val SolCyan = Color(0xFF00F0FF)

fun getRandomPositiveEmoji(): String {
    val list = listOf("👍", "🤠", "💯", "🦋", "🐸", "🔨", "🌟", "🚀", "🎉", "🔥", "🙌", "🤝", "🏆", "✨", "🎈")
    return list.random()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isLoading by remember { mutableStateOf(true) }
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = SolDarkBg,
                    surface = SolDarkCard,
                    primary = SolPurple,
                    secondary = SolGreen
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SolDarkBg
                ) {
                    if (isLoading) {
                        SplashLoader(onLoadingComplete = { isLoading = false })
                    } else {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel: AgentViewModel = viewModel(factory = ViewModelFactory(context))

    val sponsorshipsList by viewModel.sponsorships.collectAsState()
    val logsList by viewModel.logs.collectAsState()
    val migratedCoinsList by viewModel.migratedCoins.collectAsState()

    val showBanner by viewModel.showAutoNotificationBanner.collectAsState()
    val bannerToken by viewModel.latestAutoToken.collectAsState()
    val bannerMcap by viewModel.latestAutoMcap.collectAsState()

    // Real-time alert states for pump.fun Raydium migrations
    val showMigrationPopup by viewModel.showMigrationPopup.collectAsState()
    val popupNewCoinSymbol by viewModel.popupNewCoinSymbol.collectAsState()
    val popupNewCoinName by viewModel.popupNewCoinName.collectAsState()
    val popupNewCoinMcap by viewModel.popupNewCoinMcap.collectAsState()
    val popupNewCoinVol by viewModel.popupNewCoinVol.collectAsState()
    val popupSecondsRemaining by viewModel.popupSecondsRemaining.collectAsState()

    var activeTab by remember { mutableStateOf("Donations") }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(SolGreen, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TORDAOweb3 AGENT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SolTextWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.clearDataSets()
                            Toast.makeText(context, "Data sets reset! ${getRandomPositiveEmoji()}", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Cache", tint = SolSecGrey)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SolDarkBg)
            )
        },
        bottomBar = {
            Surface(
                color = SolDarkCard,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("premium_scroll_bottom_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        "Donations" to ("Donate" to Icons.Default.VolunteerActivism),
                        "News" to ("News Feed" to Icons.Default.Newspaper),
                        "Radar" to ("Radar" to Icons.Default.Radar),
                        "Quantum" to ("Quantum" to Icons.Default.GraphicEq),
                        "Gallery" to ("Gallery" to Icons.Default.PhotoLibrary),
                        "Scheduler" to ("Schedule" to Icons.Default.Schedule),
                        "RPC" to ("RPC Node" to Icons.Default.Dns)
                    )
                    tabs.forEach { (tabKey, pair) ->
                        val (label, icon) = pair
                        val isSelected = activeTab == tabKey
                        val tintColor = when(tabKey) {
                            "Donations" -> SolGreen
                            "News", "Radar" -> SolPurple
                            "Quantum", "Gallery" -> SolCyan
                            "Scheduler" -> SolRed
                            "RPC" -> SolGreen
                            else -> SolPurple
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) tintColor.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { activeTab = tabKey }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("nav_item_$tabKey")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "$label Tab Icon",
                                    tint = if (isSelected) tintColor else SolSecGrey,
                                    modifier = Modifier.size(18.dp)
                                )
                                AnimatedVisibility(visible = isSelected) {
                                    Row {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            color = tintColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Spotted Banner
            AnimatedVisibility(
                visible = showBanner,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, SolGreen, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E19)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "System alert icon",
                                tint = SolGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "🚨 RADAR MEMECOIN DETECTED!",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = SolGreen,
                                    fontSize = 11.sp
                                )
                                Text(
                                    "Scan alert: $$bannerToken spotted on pump.fun. Valuation: $${String.format("%,.0f", bannerMcap)}",
                                    color = SolTextWhite,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.showAutoNotificationBanner.value = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close alert board link", tint = SolSecGrey, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    "Donations" -> SponsorshipTab(
                        sponsorshipsList = sponsorshipsList,
                        onSponsorSubmit = { name, handle, bid, text, sig ->
                            viewModel.logSponsorSelfRegistration(name, handle, bid, text, sig)
                        }
                    )
                    "News" -> NewsFeedTab(
                        migratedCoinsList = migratedCoinsList
                    )
                    "Radar" -> RadarTab(
                        viewModel = viewModel
                    )
                    "Quantum" -> QuantumTab(
                        viewModel = viewModel
                    )
                    "Gallery" -> GalleryTab(
                        viewModel = viewModel
                    )
                    "Scheduler" -> SchedulerTab(
                        viewModel = viewModel
                    )
                    "RPC" -> DiagnosticsTab(
                        viewModel = viewModel,
                        logsList = logsList
                    )
                }
            }
        }
    }

    // Heads-up Alert Overlay
    AnimatedVisibility(
            visible = showMigrationPopup,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(999f)
                .padding(top = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(2.dp, SolRed, RoundedCornerShape(16.dp))
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .testTag("heads_up_migration_popup"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0C11)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header row with high warning alert icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning alert",
                            tint = SolRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PUMP.FUN MIGRATION TO RAYDIUM",
                            color = SolRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Coin Display
                    Text(
                        text = popupNewCoinSymbol,
                        color = SolGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = popupNewCoinName,
                        color = SolTextWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row with Stats: MCAP and Traded Volume
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C0D14), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x30FF4B72), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MIGRATED MCAP",
                                color = SolSecGrey,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$$${String.format(Locale.US, "%,.0f", popupNewCoinMcap)}",
                                color = SolGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TRADED VOLUME",
                                color = SolSecGrey,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$$${String.format(Locale.US, "%,.0f", popupNewCoinVol)}",
                                color = SolCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button countdown display and dismiss
                    if (popupSecondsRemaining > 0) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2C151A),
                                disabledContainerColor = Color(0xFF2C151A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = SolRed,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "VIBRATING AGGRESSIVELY (${popupSecondsRemaining}s remaining...)",
                                    color = SolRed.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.stopVibration()
                                viewModel.showMigrationPopup.value = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("silence_dismiss_migration_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = "Silence alert",
                                    tint = SolTextWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SILENCE & DISMISS ALERT",
                                    color = SolTextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
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
fun NewsFeedTab(
    migratedCoinsList: List<com.example.data.MigratedCoin>
) {
    val context = LocalContext.current
    
    // Filter coins to only select those launched within the last 24 hours AND meeting migration status.
    // Migration status means actualMarketCap >= 40_000.0 (already done in simulation/viewmodel, but we verify here)
    val now = remember { System.currentTimeMillis() }
    val eligibleCoins = remember(migratedCoinsList) {
        migratedCoinsList.filter {
            (now - it.launchedTimestamp <= 24 * 3600 * 1000L) && (it.actualMarketCap >= 40000.0)
        }.sortedByDescending { it.volumeTraded }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // News Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, Color(0x309945FF), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SOLANA ACTIVE MIGRATION NEWS FEED",
                        fontFamily = FontFamily.Monospace,
                        color = SolGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tracking pump.fun tokens launched < 24 hrs ago that crossed the $40K migration limit successfully.",
                        color = SolSecGrey,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Empty state
        if (eligibleCoins.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = "No news active",
                            tint = SolSecGrey.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No newly migrated coins detected in the last 24 hours.",
                            color = SolSecGrey,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(eligibleCoins, key = { it.tokenAddress }) { coin ->
                val timeSinceMigratedMs = now - coin.migratedTimestamp
                val timeAgoFormatted = remember(timeSinceMigratedMs) {
                    val durationSec = timeSinceMigratedMs / 1000
                    if (durationSec < 60) {
                        "Just now"
                    } else {
                        val minutes = durationSec / 60
                        if (minutes < 60) {
                            "$minutes minutes ago"
                        } else {
                            val hours = minutes / 60
                            val remMins = minutes % 60
                            if (hours == 1L) {
                                "1 hour $remMins minutes ago"
                            } else {
                                "$hours hours $remMins minutes ago"
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x1F8D90A5), RoundedCornerShape(12.dp))
                        .testTag("news_feed_coin_card_${coin.symbol}"),
                    colors = CardDefaults.cardColors(containerColor = SolDarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // 1. Author and Time Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFF1F2235), RoundedCornerShape(50))
                                        .border(1.dp, SolPurple, RoundedCornerShape(50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = coin.symbol.take(2),
                                        color = SolGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = coin.name,
                                        color = SolTextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "@${coin.symbol}_Sentinel",
                                        color = SolPurple,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Time Ago Badge style
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFF4B72).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, SolRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MIGRATED $timeAgoFormatted",
                                    color = SolRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Market Cap, Volume and ATH Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0C0D14), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0x159945FF), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ACTUAL MCAP",
                                    color = SolSecGrey,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$$${String.format(Locale.US, "%,.0f", coin.actualMarketCap)}",
                                    color = SolGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "VOLUME TRADED",
                                    color = SolSecGrey,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$$${String.format(Locale.US, "%,.0f", coin.volumeTraded)}",
                                    color = SolCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ALL-TIME HIGH",
                                    color = SolSecGrey,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$$${String.format(Locale.US, "%,.0f", coin.athMarketCap)}",
                                    color = SolPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. Twitter Message Body
                        Text(
                            text = coin.twitterText,
                            color = SolTextWhite,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4. Linked image (if present)
                        if (coin.twitterImageUrl.isNotBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .border(1.dp, Color(0xFF1B1B2A), RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                AsyncImage(
                                    model = coin.twitterImageUrl,
                                    contentDescription = "Twitter Attached Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // 5. Clickable Website Button / badge
                        if (coin.linkedWebsite.isNotBlank()) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(coin.linkedWebsite)).apply {
                                        setPackage("com.android.chrome")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // fall back
                                        try {
                                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(coin.linkedWebsite))
                                            context.startActivity(fallbackIntent)
                                        } catch (e2: Exception) {
                                            Toast.makeText(context, "Cannot open: ${coin.linkedWebsite}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("visit_website_button_${coin.symbol}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x2014F195)),
                                border = BorderStroke(1.dp, SolGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Website Link Icon",
                                        tint = SolGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Visit Official: ${coin.linkedWebsite}",
                                        color = SolGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SponsorshipTab(
    sponsorshipsList: List<com.example.data.Sponsorship>,
    onSponsorSubmit: (String, String, Double, String, String) -> Unit
) {
    val context = LocalContext.current
    val donationAddress = "3fSTqzEgtCbnpJn4j2yHTkyXiEHXbcgEvQgouVx2SXvV"

    // Form inputs state
    var sponsorNameInput by remember { mutableStateOf("") }
    var tgHandleInput by remember { mutableStateOf("") }
    var bidSolInput by remember { mutableStateOf("") }
    var messageTextInput by remember { mutableStateOf("") }
    var txSignatureInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Hero Card containing Solana receiving address
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(SolPurple, SolGreen)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TORDAOweb3 DEVELOPMENT SUPPORT",
                        fontFamily = FontFamily.Monospace,
                        color = SolGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Support the development of the ultimate autonomous Solana-only Web3 Sentinel. Copy the official receive code below to donate SOL / SPL Tokens directly to the fund.",
                        color = SolSecGrey,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Address display box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090A0F), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x309945FF), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SOLANA DEV WALLET",
                                fontFamily = FontFamily.Monospace,
                                color = SolSecGrey,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = donationAddress,
                                color = SolTextWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("receiving_wallet_address")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("Donation Address", donationAddress)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "Wallet address copied! 🚀 ${getRandomPositiveEmoji()}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("copy_address_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SolPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy button icon", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy Donation Address", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Interactive Sponsor / Backer Logging Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x208D90A5), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "REGISTER YOUR SOL SPONSORSHIP",
                        color = SolTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Already simulated a transaction? Register your contribution in our live local directory of sponsors below.",
                        color = SolSecGrey,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sponsorNameInput,
                        onValueChange = { sponsorNameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sponsor_name_input"),
                        label = { Text("Your Sponsor Name / Pseudonym", color = SolSecGrey, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SolTextWhite,
                            unfocusedTextColor = SolTextWhite,
                            focusedBorderColor = SolPurple,
                            unfocusedBorderColor = Color(0x309945FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tgHandleInput,
                            onValueChange = { tgHandleInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sponsor_tg_input"),
                            label = { Text("Telegram / X Handle", color = SolSecGrey, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SolTextWhite,
                                unfocusedTextColor = SolTextWhite,
                                focusedBorderColor = SolPurple,
                                unfocusedBorderColor = Color(0x309945FF)
                            )
                        )

                        OutlinedTextField(
                            value = bidSolInput,
                            onValueChange = { bidSolInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sponsor_bid_input"),
                            label = { Text("SOL Amount", color = SolSecGrey, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SolTextWhite,
                                unfocusedTextColor = SolTextWhite,
                                focusedBorderColor = SolPurple,
                                unfocusedBorderColor = Color(0x309945FF)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = messageTextInput,
                        onValueChange = { messageTextInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sponsor_text_input"),
                        label = { Text("Sponsorship / Support Message", color = SolSecGrey, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SolTextWhite,
                            unfocusedTextColor = SolTextWhite,
                            focusedBorderColor = SolPurple,
                            unfocusedBorderColor = Color(0x309945FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = txSignatureInput,
                        onValueChange = { txSignatureInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sponsor_signature_input"),
                        label = { Text("Transaction Signature (Optional)", color = SolSecGrey, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SolTextWhite,
                            unfocusedTextColor = SolTextWhite,
                            focusedBorderColor = SolPurple,
                            unfocusedBorderColor = Color(0x309945FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (sponsorNameInput.isBlank()) {
                                Toast.makeText(context, "Please enter a sponsor name.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val bidVal = bidSolInput.toDoubleOrNull() ?: 0.0
                            if (bidVal <= 0.0) {
                                Toast.makeText(context, "Sponsorship requires > 0 SOL.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onSponsorSubmit(
                                sponsorNameInput,
                                tgHandleInput,
                                bidVal,
                                messageTextInput,
                                txSignatureInput
                            )
                            sponsorNameInput = ""
                            tgHandleInput = ""
                            bidSolInput = ""
                            messageTextInput = ""
                            txSignatureInput = ""
                            Toast.makeText(context, "Sponsorship registered locally! 🌟 ${getRandomPositiveEmoji()}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_sponsor_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SolGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Verify & Broadcast To Live Board", color = SolDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. Live Sponsors Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Leaderboard, contentDescription = "Board icon", tint = SolGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "LIVE SPONSOR BOARD & COMMENDATIONS",
                    color = SolTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Empty state or list
        if (sponsorshipsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No registered sponsors yet. Be the first to load the directory!", color = SolSecGrey, fontSize = 11.sp)
                }
            }
        } else {
            items(sponsorshipsList) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x1F14F195), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = SolDarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.sponsorName,
                                    fontWeight = FontWeight.Bold,
                                    color = SolTextWhite,
                                    fontSize = 13.sp
                                )
                                if (item.telegramHandle.isNotBlank()) {
                                    Text(
                                        text = item.telegramHandle,
                                        color = SolGreen,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color(0x1A14F195), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${item.bidAmountSol} SOL",
                                    color = SolGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (item.messageText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"${item.messageText}\"",
                                color = SolSecGrey,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tx: ${item.txSignature}",
                            color = Color(0x608D90A5),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bottom space so scrolling feels airy
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun RadarTab(
    viewModel: AgentViewModel
) {
    var contractInput by remember { mutableStateOf("") }
    val isScanning by viewModel.isScanningToken.collectAsState()
    val scanSymbol by viewModel.scanTokenSymbol.collectAsState()
    val scanDetails by viewModel.scanResultText.collectAsState()
    val isPumpMatch by viewModel.isPumpFunMatch.collectAsState()

    val isAnalyzingWeb3News by viewModel.isAnalyzingWeb3News.collectAsState()
    val web3AgentFocusType by viewModel.web3AgentFocusType.collectAsState()
    val web3Sentiment by viewModel.web3Sentiment.collectAsState()
    val web3ImpactScore by viewModel.web3ImpactScore.collectAsState()
    val web3ViralTweetsList by viewModel.web3ViralTweetsList.collectAsState()
    val web3AgentAnalysisDigest by viewModel.web3AgentAnalysisDigest.collectAsState()

    val web3FeedbackAccuracy by viewModel.web3FeedbackAccuracy.collectAsState()
    val web3FeedbackSamplesCount by viewModel.web3FeedbackSamplesCount.collectAsState()
    val web3FeedbackLogs by viewModel.web3FeedbackLogs.collectAsState()
    val web3CompetitorSymbol by viewModel.web3CompetitorSymbol.collectAsState()
    val web3CompetitorName by viewModel.web3CompetitorName.collectAsState()
    val web3CompetitorTweet by viewModel.web3CompetitorTweet.collectAsState()
    val web3CompetitorComparisonProsCons by viewModel.web3CompetitorComparisonProsCons.collectAsState()
    val web3SponsorMatchStatus by viewModel.web3SponsorMatchStatus.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, Color(0x208D90A5), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "PUMP.FUN MIGRED RADAR SPEED-SCAN",
                        fontFamily = FontFamily.Monospace,
                        color = SolGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Analyze contract transitions to DEX pools. Targeted detection range is $40k – $300k MCAP.",
                        color = SolSecGrey,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Scan Input card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x159945FF), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Agent intelligence search warning note
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x159945FF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x309945FF), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Search Note Info Icon",
                                    tint = SolPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AGENT INTELLIGENCE NOTE",
                                    color = SolPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Search for names of any crypto coin to see actual news or type in general requests. The AI Sentinel Agent dynamically fetches coin-specific or relevant tweets, web feeds, and sentiment data regarding the input coin or request.",
                                color = SolTextWhite,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = contractInput,
                        onValueChange = { contractInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contract_address_input"),
                        label = { Text("Solana Address, Coin Name (BTC, SOL), or Topic", color = SolSecGrey, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SolTextWhite,
                            unfocusedTextColor = SolTextWhite,
                            focusedBorderColor = SolPurple,
                            unfocusedBorderColor = Color(0x309945FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("TARGETED PUMP.FUN SELECTION (40K$ - 300K$ RANGE):", color = SolSecGrey, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val pumpPresets = listOf(
                            "TORFROG" to "TORFROG99x81Pmp...pump",
                            "CIRQCEL" to "CiRq91xP12V1vPmp...pump",
                            "PEPEPUMP" to "PePu919aHv82Pmp...pump",
                            "GIGAFROG" to "G1gaF6v7B9Pmp...pump"
                        )
                        pumpPresets.forEach { (sym, ca) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0x3014F195), RoundedCornerShape(20.dp))
                                    .background(Color(0x0514F195), RoundedCornerShape(20.dp))
                                    .clickable {
                                        contractInput = ca
                                        viewModel.scanTokenPrice(ca)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sym, color = SolGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.scanTokenPrice(contractInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("radar_scan_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SolPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(color = SolTextWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("RUN SENTINEL AGENT SEARCH", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Scan Results card
        if (scanSymbol.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SolGreen, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh scan symbol info", tint = SolGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = scanSymbol,
                                        fontWeight = FontWeight.Bold,
                                        color = SolGreen,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (isPumpMatch) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF14F195).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .border(1.dp, SolGreen, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "PUMP.FUN ON-WATCH MATCH 🎯",
                                                color = SolGreen,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = scanDetails,
                                    color = SolTextWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.testTag("scan_details_box")
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Web3 News Viral Sentinel Agent Panel ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, Color(0xFF9945FF).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10121F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Web3 Agent Icon",
                            tint = SolPurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WEB3 VIRAL SENTINEL AGENT",
                            fontWeight = FontWeight.Bold,
                            color = SolTextWhite,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF9945FF).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                color = SolPurple,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (scanSymbol.isEmpty()) {
                        // Empty / Idle State
                        Text(
                            text = "FOCUS BANDWIDTH RATIO: 80% Top 300 CMC / 20% KOL Hot Memecoins",
                            color = SolSecGrey,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Custom Asymmetric Focus Slider Meter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0x308D90A5))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(0.8f)
                                    .background(SolCyan)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(0.2f)
                                    .background(SolPurple)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "80% TOP 300 COINMARKETCAP",
                                color = SolCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "20% KOL MEMECOINS",
                                color = SolPurple,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Scan any Solana token contract address to activate the Sentinel news agent. The agent crawls X streams to isolate high-conviction signals and viral posts that directly influence market volume.",
                            color = SolSecGrey,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    } else if (isAnalyzingWeb3News) {
                        // Analyzing / Scanning State
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = SolPurple,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "🤖 WEB3 SENTINEL RUNNING SYNTHESIS...",
                                color = SolPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("agent_scanning_text")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Matching influencer tickers & tracing smart-money alpha feeds...",
                                color = SolSecGrey,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        // Scan complete: display results from Agent
                        Text(
                            text = web3AgentFocusType,
                            color = if (web3AgentFocusType.contains("Top 300")) SolCyan else SolPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x1F2A3050), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("VIRAL INFLUENCE INDEX", color = SolSecGrey, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$web3ImpactScore",
                                        color = SolTextWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(" / 10", color = SolSecGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (web3Sentiment.contains("BULLISH")) Color(0x1F14F195) else Color(0x1FFF4B72),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = web3Sentiment,
                                    color = if (web3Sentiment.contains("BULLISH")) SolGreen else SolRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Analysis Digest Box
                        Text(
                            text = "AGENT CROSS-SYNTHESIS SUMMARY:",
                            color = SolSecGrey,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = web3AgentAnalysisDigest,
                            color = SolTextWhite,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier
                                .border(1.dp, Color(0xFF2A2D3D), RoundedCornerShape(6.dp))
                                .background(Color(0xFF0C0D14))
                                .padding(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sponsor status row / cross check
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (web3SponsorMatchStatus.contains("VERIFIED SPONSOR")) SolGreen.copy(alpha = 0.5f) else Color(0xFF2A2D3D), RoundedCornerShape(6.dp))
                                .background(if (web3SponsorMatchStatus.contains("VERIFIED SPONSOR")) Color(0x1914F195) else Color(0x1A10121F))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (web3SponsorMatchStatus.contains("VERIFIED SPONSOR")) Icons.Default.CheckCircle else Icons.Default.Gavel,
                                contentDescription = "Sponsor Cross Check",
                                tint = if (web3SponsorMatchStatus.contains("VERIFIED SPONSOR")) SolGreen else SolSecGrey,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "DAO SPONSOR CORRELATION STATUS",
                                    color = if (web3SponsorMatchStatus.contains("VERIFIED SPONSOR")) SolGreen else SolSecGrey,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = web3SponsorMatchStatus,
                                    color = SolTextWhite,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "IDENTIFIED VIRAL TWEETS (X) FEED:",
                            color = SolSecGrey,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Render each tweet in a gorgeous tweet-style bubble
                        web3ViralTweetsList.forEach { tweet ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFF15182A), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x118D90A5), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(if (tweet.isKolBought) SolPurple else SolCyan),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            tweet.author.take(1),
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tweet.author, color = SolTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(tweet.handle, color = SolSecGrey, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(tweet.timestampText, color = SolSecGrey, fontSize = 9.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tweet.text,
                                    color = SolTextWhite,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Likes",
                                            tint = SolRed,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${tweet.likes}", color = SolSecGrey, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Retweets",
                                            tint = SolGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${tweet.retweets}", color = SolSecGrey, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    if (tweet.isKolBought) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0x1FFF4B72), RoundedCornerShape(4.dp))
                                                .border(1.dp, SolRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                "🔥 KOL BOUGHT",
                                                color = SolRed,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "🔍 Sentinel Impact Insight: ${tweet.impactAnalysis}",
                                    color = SolGreen,
                                    fontSize = 9.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F141F), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Feedback Tuning:",
                                        color = SolSecGrey,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { viewModel.submitUserFeedbackOnSentiment(tweet.author, true) },
                                            modifier = Modifier.height(24.dp).testTag("feedback_accurate_btn_${tweet.author}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3314F195)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("👍 Accurate", color = SolGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.submitUserFeedbackOnSentiment(tweet.author, false) },
                                            modifier = Modifier.height(24.dp).testTag("feedback_wrong_btn_${tweet.author}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF4B72)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("👎 Missed", color = SolRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFF202230), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Competitor Comparison Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SolCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F111E))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Competitor Comparison",
                                        tint = SolCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "COMPETITOR COMPARISON: ${web3CompetitorName.uppercase()} ($web3CompetitorSymbol)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = SolCyan
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = web3CompetitorComparisonProsCons,
                                    color = SolTextWhite,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier
                                        .background(Color(0xFF07080E), RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                )

                                web3CompetitorTweet?.let { compTweetcomp ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "COMPETITOR REPRESENTATIVE TWEET FEED:",
                                        color = SolSecGrey,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF15182A), RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0x228D90A5), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(compTweetcomp.author, color = SolTextWhite, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(compTweetcomp.handle, color = SolSecGrey, fontSize = 9.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(compTweetcomp.text, color = SolTextWhite, fontSize = 10.sp, lineHeight = 13.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🔍 Comp Insight: ${compTweetcomp.impactAnalysis}",
                                            color = SolCyan,
                                            fontSize = 9.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFF202230), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Feedback Metrics Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SolCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1322))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "Feedback Loop",
                                            tint = SolCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SELF-IMPROVING SYNAPSE LOOP",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = SolTextWhite
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(SolCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "ONLINE",
                                            color = SolCyan,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("MODEL ACCURACY RATING", color = SolSecGrey, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("$web3FeedbackAccuracy%", color = SolGreen, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("REINFORCEMENT TUNES", color = SolSecGrey, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("$web3FeedbackSamplesCount Samples", color = SolTextWhite, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ACTIVE LEARNING TELEMETRY (CONSOLE OUT):",
                                    color = SolSecGrey,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Real console list
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .background(Color(0xFF07080F), RoundedCornerShape(4.dp))
                                        .border(1.dp, Color(0xFF1E2135), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(web3FeedbackLogs.size) { index ->
                                            Text(
                                                text = web3FeedbackLogs[index],
                                                color = Color(0xFFA0C070),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quantum superposition simulation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x208D90A5), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "CIRQCEL QUANTUM ENTROPY MODEL",
                        fontFamily = FontFamily.Monospace,
                        color = SolGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Simulating momentum rotations using a Google Cirq circuit archetype (Hadamard state on q0, entangling logic for pump.fun stability):",
                        color = SolSecGrey,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090A0F), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x309945FF), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = """
                                [Qubit Configuration]
                                q0: Momentum status (|H> phase representation) 
                                q1: Retained liquidity correlation
                                
                                [Repetitions] 100 Sim reps
                                [Projected Volume Shift (RZ)] Pi/4 (Stable)
                                
                                >>> WEIGHTED ENTROPY: 62.45%
                                >>> SENTIMENT: ACCELERATE BULLISH ACCUMULATION
                            """.trimIndent(),
                            color = SolTextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DiagnosticsTab(
    viewModel: AgentViewModel,
    logsList: List<com.example.data.SystemLog>
) {
    var rpcInputUrl by remember { mutableStateOf("https://api.mainnet-beta.solana.com") }
    val rpcStatus by viewModel.solanaRpcStatus.collectAsState()
    val rpcSlot by viewModel.solanaLastSlot.collectAsState()
    val rpcEpoch by viewModel.solanaEpoch.collectAsState()
    val isRcpScanning by viewModel.isScanningRpc.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // RPC configuration
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, Color(0x208D90A5), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "SOLANA RPC ENDPOINT CONNECTION",
                        fontFamily = FontFamily.Monospace,
                        color = SolPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rpcInputUrl,
                        onValueChange = { 
                            rpcInputUrl = it 
                            viewModel.updateTelemetryProperties(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rpc_url_input"),
                        label = { Text("Custom Solana Node RPC", color = SolSecGrey, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SolTextWhite,
                            unfocusedTextColor = SolTextWhite,
                            focusedBorderColor = SolPurple,
                            unfocusedBorderColor = Color(0x309945FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("STATUS: $rpcStatus", color = SolGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            if (rpcSlot > 0) {
                                Text("Last Slot: $rpcSlot", color = SolSecGrey, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("Epoch: $rpcEpoch", color = SolSecGrey, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Button(
                            onClick = { viewModel.triggerSolanaRpcScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = SolPurple),
                            modifier = Modifier.testTag("query_rpc_button")
                        ) {
                            if (isRcpScanning) {
                                CircularProgressIndicator(color = SolTextWhite, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Test Node", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live systems APM logs
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SolPurple, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "INTEGRATED TELEMETRY DIRECTORY (LIVE APM)",
                    color = SolTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        items(logsList) { log ->
            val colorLevel = when (log.logLevel) {
                "SUCCESS" -> SolGreen
                "WARN" -> SolRed
                else -> SolPurple
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1B1F38), RoundedCornerShape(4.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090A0F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "[${log.serviceName}]",
                            color = colorLevel,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        Text(
                            text = formatter.format(Date(log.timestamp)),
                            color = SolSecGrey,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.message,
                        color = SolTextWhite,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuantumTab(viewModel: AgentViewModel) {
    val quantumHistoryState by viewModel.quantumHistory.collectAsState()
    val quantumAmplitudesState by viewModel.quantumAmplitudes.collectAsState()
    val quantumEntropyValueState by viewModel.quantumEntropyValue.collectAsState()

    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isWalletConnected by remember { mutableStateOf(false) }
    var selectedWallet by remember { mutableStateOf("Phantom") }
    var isMintingNFT by remember { mutableStateOf(false) }
    var mintProgress by remember { mutableStateOf(0f) }
    var mintStatusString by remember { mutableStateOf("") }
    var mintTxSignature by remember { mutableStateOf("") }

    var livelyFilterEnabled by remember { mutableStateOf(true) }
    var randomGatesLabel by remember { mutableStateOf("H(q0) -> T(q0, q1 -> q2) -> X(q2)") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            viewModel.addLog("Quantum_Minter", "Image uploaded successfully for Quantum compilation.", "SUCCESS")
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Applet Banner Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, Color(0x1C14F195), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SolCyan, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI LIVING VISUALIZER (RESTORED & LIVELY) 🕊️",
                            fontFamily = FontFamily.Monospace,
                            color = SolCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Renders dynamic quantum probability densities, phase coordinate systems and live Toffoli gates. Driven by on-chain microstructured states.",
                        color = SolSecGrey,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Live Canvas
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xFF07080E), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x1A00F0FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background grid coordinates
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val gridPaint = Color(0x0C8D90A5)
                    // draw horizontal grid lines
                    for (gridY in 0 until h.toInt() step 40) {
                        drawLine(color = gridPaint, start = androidx.compose.ui.geometry.Offset(0f, gridY.toFloat()), end = androidx.compose.ui.geometry.Offset(w, gridY.toFloat()), strokeWidth = 0.5.dp.toPx())
                    }
                    // draw vertical grid lines
                    for (gridX in 0 until w.toInt() step 40) {
                        drawLine(color = gridPaint, start = androidx.compose.ui.geometry.Offset(gridX.toFloat(), 0f), end = androidx.compose.ui.geometry.Offset(gridX.toFloat(), h), strokeWidth = 0.5.dp.toPx())
                    }
                }

                // Natural Shape Organic Rendering
                Canvas(modifier = Modifier.size(170.dp).testTag("quantum_canvas")) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val centerOffset = androidx.compose.ui.geometry.Offset(cx, cy)
                    val baseRadius = 55.dp.toPx()
                    
                    val path = androidx.compose.ui.graphics.Path()
                    val entropyMod = quantumEntropyValueState.toFloat() // 0.0 to 3.0
                    
                    for (angleDeg in 0..360 step(5)) {
                        val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
                        // Perturb shape complexly
                        val waveOffset = (kotlin.math.sin(angleRad * 5f + animatedPhase) * 12f * entropyMod) +
                                         (kotlin.math.cos(angleRad * 3f - animatedPhase * 1.5f) * 6f * (1f + entropyMod))
                        val r = baseRadius + waveOffset.toFloat()
                        val x = cx + r * kotlin.math.cos(angleRad).toFloat()
                        val y = cy + r * kotlin.math.sin(angleRad).toFloat()
                        if (angleDeg == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    path.close()
                    
                    val brush = Brush.radialGradient(
                        colors = listOf(
                            SolCyan.copy(alpha = 0.45f),
                            SolPurple.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = baseRadius * 1.6f
                    )
                    drawPath(path, brush)
                    
                    // Draw orbits
                    drawCircle(
                        color = Color(0x1F9945FF),
                        radius = baseRadius + 30.dp.toPx(),
                        center = centerOffset,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    )

                    // Draw outer nodes base on amplitude probability
                    for (i in 0 until 8) {
                        val amp = quantumAmplitudesState[i]
                        val probability = (amp.real * amp.real + amp.imag * amp.imag).toFloat()
                        if (probability > 0.001f) {
                            val nodeAngleOffset = (i * (2 * Math.PI / 8.0)).toFloat()
                            val nodeAngle = nodeAngleOffset + animatedPhase * 0.25f
                            val nodeRadius = baseRadius + 28.dp.toPx() + (probability * 15f)
                            val nodeX = cx + nodeRadius * kotlin.math.cos(nodeAngle.toDouble()).toFloat()
                            val nodeY = cy + nodeRadius * kotlin.math.sin(nodeAngle.toDouble()).toFloat()
                            val nodeOffset = androidx.compose.ui.geometry.Offset(nodeX, nodeY)
                            
                            val phaseAngle = amp.phase()
                            val phaseHue = (((phaseAngle + Math.PI) / (2 * Math.PI)) * 360f).toFloat()
                            val nodeColor = Color.hsv(phaseHue, 0.75f, 0.95f)
                            
                            drawLine(
                                color = nodeColor.copy(alpha = 0.4f),
                                start = centerOffset,
                                end = nodeOffset,
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            drawCircle(
                                color = nodeColor.copy(alpha = 0.15f),
                                radius = (8.dp.toPx() + (probability * 20f).toFloat()),
                                center = nodeOffset
                            )
                            drawCircle(
                                color = nodeColor,
                                radius = (3.dp.toPx() + (probability * 8f).toFloat()),
                                center = nodeOffset
                            )
                        }
                    }
                }

                // Superposition text status overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "ENTROPY: ${String.format(Locale.US, "%.4f", quantumEntropyValueState)} / 3.00",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = SolCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "STATES LIVE: ${quantumAmplitudesState.count { (it.real*it.real + it.imag*it.imag) > 0.01 }} ACTIVE COHERENT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = SolSecGrey
                    )
                }
            }
        }

        // Solana Dynamic GIF NFT Minter Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = "Quantum Dynamic NFT Hub",
                                tint = SolCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "QUANTUM GIF NFT MINTER HUB",
                                color = SolCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(if (isWalletConnected) Color(0x2014F195) else Color(0x20FF4B72), RoundedCornerShape(4.dp))
                                .border(1.dp, if (isWalletConnected) SolGreen else SolRed, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isWalletConnected) "CONNECTED ($selectedWallet)" else "DISCONNECTED",
                                color = if (isWalletConnected) SolGreen else SolRed,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wallet selector & Connect controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090A0F), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0x1F8D90A5), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Selector for Phantom Wallet
                            Button(
                                onClick = { selectedWallet = "Phantom" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedWallet == "Phantom") Color(0x339945FF) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (selectedWallet == "Phantom") SolPurple else Color.Transparent),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp).testTag("select_phantom_wallet")
                            ) {
                                Text("Phantom", color = if (selectedWallet == "Phantom") SolTextWhite else SolSecGrey, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            // Selector for Jupiter Wallet
                            Button(
                                onClick = { selectedWallet = "Jupiter" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedWallet == "Jupiter") Color(0x3314F195) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (selectedWallet == "Jupiter") SolGreen else Color.Transparent),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp).testTag("select_jupiter_wallet")
                            ) {
                                Text("Jupiter", color = if (selectedWallet == "Jupiter") SolTextWhite else SolSecGrey, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Connect / Disconnect Action
                        Button(
                            onClick = {
                                if (isWalletConnected) {
                                    isWalletConnected = false
                                    viewModel.addLog("Solana_Wallet", "$selectedWallet wallet disconnected safely.", "INFO")
                                } else {
                                    isWalletConnected = true
                                    val simulatedAddress = if (selectedWallet == "Phantom") "Phan68vTx...Z7aK" else "Jup94rQm...vX8P"
                                    viewModel.addLog("Solana_Wallet", "$selectedWallet wallet handshake successful. Connected: $simulatedAddress", "SUCCESS")
                                    Toast.makeText(context, "$selectedWallet wallet connected! ${getRandomPositiveEmoji()}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWalletConnected) Color(0x20FF4B72) else SolPurple
                            ),
                            border = BorderStroke(1.dp, if (isWalletConnected) SolRed else SolPurple),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(28.dp).testTag("connect_wallet_btn")
                        ) {
                            Text(
                                text = if (isWalletConnected) "Disconnect" else "Connect Wallet",
                                color = SolTextWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title section
                    Text(
                        "1. SOURCE IMAGE SELECTION",
                        color = SolSecGrey,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Select and Upload Local Image
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1C2E)),
                            border = BorderStroke(0.5.dp, SolCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("upload_image_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = "Add image", tint = SolCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedImageUri != null) "Change Photo File" else "Upload Local Photo",
                                    color = SolCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (selectedImageUri != null) {
                            Button(
                                onClick = { 
                                    selectedImageUri = null 
                                    viewModel.addLog("Quantum_Minter", "Image selection cleared.", "INFO")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x20FF4B72)),
                                border = BorderStroke(0.5.dp, SolRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.height(38.dp).testTag("clear_image_btn")
                            ) {
                                Text("Clear", color = SolRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lively dynamic compiled preview of GIF
                    Text(
                        "2. LIVELY GIF METADATA & GROK PREMIUM ANIMATIONS",
                        color = SolSecGrey,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // GIF simulation preview card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF040509), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x338D90A5), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            // Loaded user image with Lively Grok premium filter overlay
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Uploaded local photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().testTag("user_photo_preview")
                                )

                                // Grok Premium dynamic hue/color shifts filter (always lively when enabled)
                                if (livelyFilterEnabled) {
                                    val cyclicColor = Color.hsv(
                                        hue = (((animatedPhase / (2 * Math.PI)) * 360f) % 360f).toFloat(),
                                        saturation = 0.55f,
                                        value = 0.9f
                                    ).copy(alpha = 0.22f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(cyclicColor)
                                    )

                                    // Render dynamic flowing matrix lines/shapes to represent "Grok level active edits"
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val progressFactor = (kotlin.math.sin(animatedPhase.toDouble() * 1.5).toFloat() + 1f) / 2f
                                        val lineY = progressFactor * size.height
                                        
                                        // Glitch/laser tracking beam
                                        drawLine(
                                            color = SolCyan,
                                            start = androidx.compose.ui.geometry.Offset(0f, lineY),
                                            end = androidx.compose.ui.geometry.Offset(size.width, lineY),
                                            strokeWidth = 2.dp.toPx()
                                        )

                                        // Glowing pulse
                                        drawCircle(
                                            color = SolPurple.copy(alpha = 0.2f),
                                            radius = 40.dp.toPx() + (progressFactor * 30.dp.toPx()),
                                            center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                                        )
                                    }
                                }

                                // Interactive hologram grid coordinates
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val gridPaint = Color(0x1F00F0FF)
                                    for (y in 0 until size.height.toInt() step 30) {
                                        drawLine(color = gridPaint, start = androidx.compose.ui.geometry.Offset(0f, y.toFloat()), end = androidx.compose.ui.geometry.Offset(size.width, y.toFloat()), strokeWidth = 0.3.dp.toPx())
                                    }
                                }

                                // Interactive tech overlay & watermarks (containing the random operators sequence)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color(0xD005060B))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "🧬 QUANTUM OPERATORS SEQUENCE WATERMARK",
                                            color = SolCyan,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = randomGatesLabel,
                                            color = SolGreen,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                            // Default beautiful abstract geometric placeholder
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sizeScale = (kotlin.math.cos(animatedPhase.toDouble()).toFloat() + 1f) / 2f
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val radius = 50.dp.toPx() + sizeScale * 20.dp.toPx()
                                
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(SolPurple, SolCyan, SolGreen, SolPurple),
                                        center = androidx.compose.ui.geometry.Offset(cx, cy)
                                    ),
                                    radius = radius,
                                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                                )
                                
                                drawCircle(
                                    color = Color(0x1100F0FF),
                                    radius = radius * 0.8f,
                                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                                )
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "NO IMAGE SELECTED",
                                    color = SolSecGrey,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Upload a photo to compile custom coin-based Toffoli filters",
                                    color = SolSecGrey.copy(alpha = 0.7f),
                                    fontSize = 8.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Premium Filter toggle & Scrambler controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = livelyFilterEnabled,
                                onCheckedChange = { livelyFilterEnabled = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SolCyan,
                                    uncheckedColor = SolSecGrey,
                                    checkmarkColor = Color.Black
                                ),
                                modifier = Modifier.size(24.dp).testTag("checkbox_lively_filter")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Grok Premium Lively Matrix",
                                color = SolTextWhite,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = {
                                val gateList = listOf("H", "X", "Toffoli")
                                val indices = listOf(0, 1, 2)
                                val listSize = (2..4).random()
                                var scrambled = ""
                                for (step in 0 until listSize) {
                                    val g = gateList.random()
                                    val idx1 = indices.random()
                                    scrambled += when (g) {
                                        "H" -> "H(q$idx1)"
                                        "X" -> "X(q$idx1)"
                                        else -> {
                                            val idx2 = (indices - idx1).random()
                                            val target = (indices - idx1 - idx2).firstOrNull() ?: 2
                                            "T(q$idx1,q$idx2 -> q$target)"
                                        }
                                    }
                                    if (step < listSize - 1) scrambled += " -> "
                                }
                                randomGatesLabel = scrambled
                                viewModel.addLog("Quantum_Minter", "Gates inputs randomized: $scrambled", "INFO")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F8D90A5)),
                            border = BorderStroke(0.5.dp, SolSecGrey),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(24.dp).testTag("scramble_gates_btn")
                        ) {
                            Text("Randomize Gates", color = SolSecGrey, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Fee & Cost breakdown
                    Text(
                        "3. TRANSACTION FEE BREAKDOWN",
                        color = SolSecGrey,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090A0F), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0x1F8D90A5), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mint Donation (To TOR DAO Wallet)", color = SolSecGrey, fontSize = 9.5.sp)
                            Text("0.01 SOL", color = SolCyan, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dynamic Storage Rent Fee", color = SolSecGrey, fontSize = 9.5.sp)
                            Text("0.02 SOL", color = SolPurple, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x198D90A5)))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Outflow", color = SolTextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("0.03 SOL", color = SolGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rent Disclaimer Text
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(0.5.dp, Color(0x33FFD700)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13100B))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️ Rent Disclaimer", color = Color(0xFFF39C12), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "0.02 SOL is allocated for Solana's rent-exempt storage criteria. This balance can be fully reclaimed/redeemed anytime via the Sol Incinerator platform by burning or closing the associated token storage accounts.",
                                color = Color(0xFFCCAA66),
                                fontSize = 8.sp,
                                lineHeight = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Minting Execution & Progress states
                    if (isMintingNFT || mintProgress > 0f) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mintStatusString,
                                    color = if (mintProgress >= 1f) SolGreen else SolSecGrey,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${(mintProgress * 100).toInt()}%",
                                    color = SolCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = mintProgress,
                                modifier = Modifier.fillMaxWidth().height(4.dp).testTag("mint_progress_bar"),
                                color = SolGreen,
                                trackColor = Color(0xFF090A0F)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    if (mintTxSignature.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF05100B), RoundedCornerShape(6.dp))
                                .border(0.5.dp, SolGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("MINT SUCCESSFUL 🏆", color = SolGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Signature: $mintTxSignature",
                                    color = SolTextWhite,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Mint Action Buttons State
                    val scope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            if (!isWalletConnected) {
                                // Fast wallet autoconnect wrapper
                                isWalletConnected = true
                                val simulatedAddress = if (selectedWallet == "Phantom") "Phan68vTx...Z7aK" else "Jup94rQm...vX8P"
                                viewModel.addLog("Solana_Wallet", "Automatic handshake trigger via Minting execution.", "SUCCESS")
                                viewModel.addLog("Solana_Wallet", "$selectedWallet wallet connected: $simulatedAddress", "SUCCESS")
                                Toast.makeText(context, "$selectedWallet wallet connected automatically! ${getRandomPositiveEmoji()}", Toast.LENGTH_SHORT).show()
                            } else {
                                // Run actual sequential compilation flow
                                scope.launch {
                                    isMintingNFT = true
                                    mintProgress = 0.05f
                                    mintStatusString = "Handshaking with secure $selectedWallet interface..."
                                    mintTxSignature = ""
                                    
                                    delay(900)
                                    mintProgress = 0.25f
                                    mintStatusString = "Injecting lively image frames via Grok Premium filters..."
                                    
                                    delay(1100)
                                    mintProgress = 0.45f
                                    mintStatusString = "Watermarking $randomGatesLabel sequence on master GIF..."
                                    
                                    delay(1000)
                                    mintProgress = 0.70f
                                    mintStatusString = "Publishing payload & routing 0.01 SOL to TOR DAO wallet..."
                                    
                                    delay(900)
                                    mintProgress = 0.88f
                                    mintStatusString = "Allocating 0.02 SOL reclaimable storage fee..."
                                    
                                    delay(800)
                                    mintProgress = 1.0f
                                    val txSig = "3NFT" + UUID.randomUUID().toString().replace("-", "").take(16).uppercase(Locale.US) + "Sig"
                                    mintTxSignature = txSig
                                    mintStatusString = "Compilation Complete! Mint Successful ${getRandomPositiveEmoji()}"
                                    isMintingNFT = false
                                    
                                    val imageUriStr = selectedImageUri?.toString() ?: ""
                                    val connectedWalletAddress = if (selectedWallet == "Phantom") "Phan68vTx...Z7aK" else "Jup94rQm...vX8P"
                                    viewModel.addMintedNft(
                                        imageUri = imageUriStr,
                                        quantumConfig = randomGatesLabel,
                                        walletAddress = connectedWalletAddress,
                                        txSignature = txSig
                                    )

                                    viewModel.addLog("Quantum_NFT", "Minted lively NFT GIF with gates sequence successfully! Signature: $txSig", "SUCCESS")
                                    viewModel.addLog("TorDao_DAO", "Transferred 0.01 SOL donation safely from $selectedWallet wallet.", "SUCCESS")
                                    viewModel.addLog("SolIncinerator", "Locked 0.02 SOL storage rent. Reclaim anytime at solincinerator.com", "INFO")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("execute_mint_nft_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWalletConnected) SolGreen else SolPurple
                        ),
                        enabled = !isMintingNFT && (selectedImageUri != null)
                    ) {
                        Text(
                            text = if (selectedImageUri == null) {
                                "First Upload Image to Mint"
                            } else if (!isWalletConnected) {
                                "Autoconnect & Mint (0.03 SOL)"
                            } else {
                                "Mint Quantum NFT GIF (0.03 SOL)"
                            },
                            color = if (isWalletConnected && selectedImageUri != null) Color.Black else SolTextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Qubits Circuit Diagram Configuration
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x208D90A5), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "INTERACTIVE QUANTUM GATE CIRCUIT",
                        fontFamily = FontFamily.Monospace,
                        color = SolPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // 3 schematic wire tracks
                    for (qIndex in 0 until 3) {
                        val ampQStat = quantumAmplitudesState.mapIndexed { index, amp ->
                            // Check probability of qubit qIndex in index being 1
                            val isBitSet = (index and (1 shl qIndex)) != 0
                            if (isBitSet) amp.real * amp.real + amp.imag * amp.imag else 0.0
                        }.sum()
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(Color(0xFF090A0F), RoundedCornerShape(6.dp))
                                .border(0.5.dp, Color(0x1F8D90A5), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "q$qIndex |0> ───", 
                                color = SolSecGrey, 
                                fontSize = 10.sp, 
                                fontFamily = FontFamily.Monospace
                            )
                            
                            // Interactive gate controls
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { viewModel.applyHadamard(qIndex) },
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("apply_hadamard_$qIndex"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3000F0FF)),
                                    border = BorderStroke(0.5.dp, SolCyan),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("H", color = SolCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Button(
                                    onClick = { viewModel.applyPauliX(qIndex) },
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("apply_x_$qIndex"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x309945FF)),
                                    border = BorderStroke(0.5.dp, SolPurple),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("X", color = SolPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                            
                            // Individual state prob gauge
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .background(Color(0xFF141624), RoundedCornerShape(3.dp))
                                    .border(0.5.dp, Color(0x0C8D90A5), RoundedCornerShape(3.dp))
                                    .padding(vertical = 2.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "P(|1>): ${String.format(Locale.US, "%.0f%%", ampQStat * 100.0)}",
                                    color = if (ampQStat > 0.05) SolGreen else SolSecGrey,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // CC-NOT joint triggers and Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.applyToffoli(0, 1, 2) },
                            modifier = Modifier
                                .height(32.dp)
                                .weight(1f)
                                .testTag("apply_toffoli_q0q1_q2"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F9945FF)),
                            border = BorderStroke(1.dp, SolPurple),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Apply Toffoli (c0,c1->t2)", color = SolPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { viewModel.resetQuantumCircuit() },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("reset_quantum_circuit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1FFF4B72)),
                            border = BorderStroke(1.dp, SolRed),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Reset Space", color = SolRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Live amplitudes vector breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x208D90A5), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SUPERPOSITION COEFFICIENTS (AMPLITUDE VECTORS)",
                        fontFamily = FontFamily.Monospace,
                        color = SolCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    for (i in 0 until 8) {
                        val amp = quantumAmplitudesState[i]
                        val prob = amp.real * amp.real + amp.imag * amp.imag
                        val stateStr = when (i) {
                            0 -> "000"
                            1 -> "001"
                            2 -> "010"
                            3 -> "011"
                            4 -> "100"
                            5 -> "101"
                            6 -> "110"
                            else -> "111"
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "|$stateStr> ",
                                color = SolPurple,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp)
                            )
                            Text(
                                "(${String.format(Locale.US, "%5.2f", amp.real)}, ${String.format(Locale.US, "%5.2f", amp.imag)}j)",
                                color = SolSecGrey,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(96.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .background(Color(0xFF0C0D14), RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(prob.toFloat().coerceIn(0f, 1f))
                                        .background(SolGreen, RoundedCornerShape(3.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${String.format(Locale.US, "%.1f%%", prob * 100.0)}",
                                color = SolGreen,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        // Applied operations block log
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x208D90A5), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "QUANTUM DISPATCH LOGS",
                        fontFamily = FontFamily.Monospace,
                        color = SolSecGrey,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090A0F), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            quantumHistoryState.reversed().forEach { log ->
                                Text(
                                    text = ">>> $log",
                                    color = SolSecGrey,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun GalleryTab(viewModel: AgentViewModel) {
    val context = LocalContext.current
    val mintedList by viewModel.mintedNfts.collectAsState()
    
    // Group and count mints per wallet address
    val walletCounts = remember(mintedList) {
        mintedList.groupBy { it.walletAddress }.mapValues { it.value.size }
    }
    
    // Calculate the rank tier of each wallet (highest size = rank 1, etc.)
    val rankedWallets = remember(walletCounts) {
        walletCounts.entries
            .sortedByDescending { it.value }
            .map { it.key }
    }
    
    // Sort items by:
    // 1. Total NFTs minted by this wallet address (descending)
    // 2. Wallet address itself (stable grouping)
    // 3. Timestamp (newest first)
    val sortedNfts = remember(mintedList, walletCounts) {
        mintedList.sortedWith { a, b ->
            val countA = walletCounts[a.walletAddress] ?: 0
            val countB = walletCounts[b.walletAddress] ?: 0
            if (countA != countB) {
                countB.compareTo(countA)
            } else {
                val walletComp = a.walletAddress.compareTo(b.walletAddress)
                if (walletComp != 0) {
                    walletComp
                } else {
                    b.timestamp.compareTo(a.timestamp)
                }
            }
        }
    }

    var selectedNft by remember { mutableStateOf<MintedNft?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SolDarkBg)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "QUANTUM NFT REGULATION LEDGER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SolGreen
                )
                Text(
                    text = "Dynamic Wallet Ranking System • More Mints = Higher Rank ${getRandomPositiveEmoji()}",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 9.5.sp,
                    color = SolSecGrey
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (sortedNfts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "No Mints",
                        tint = SolSecGrey,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No custom NFTs minted yet.",
                        color = SolSecGrey,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Grid displays 2 items per line
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(sortedNfts.size) { index ->
                    val nft = sortedNfts[index]
                    val mintCount = walletCounts[nft.walletAddress] ?: 0
                    val walletRankIdx = rankedWallets.indexOf(nft.walletAddress) + 1
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                            .clickable { selectedNft = nft },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SolDarkCard),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (walletRankIdx == 1) SolGreen.copy(alpha = 0.6f) else SolPurple.copy(alpha = 0.4f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(Color(0xFF090A0F)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (nft.imageUri.isNotEmpty()) {
                                        AsyncImage(
                                            model = nft.imageUri,
                                            contentDescription = "Minted NFT Visual",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                                        listOf(SolPurple.copy(alpha = 0.8f), SolCyan.copy(alpha = 0.8f))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "GIF Visualizer",
                                                color = SolTextWhite,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Watermark sequence overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .background(Color.Black.copy(alpha = 0.75f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = nft.quantumConfig.take(16) + if (nft.quantumConfig.length > 16) "..." else "",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            color = SolCyan,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Addr: ${nft.walletAddress.take(6)}...${nft.walletAddress.takeLast(4)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = SolTextWhite,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tx: ${nft.txSignature.take(10)}...",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = SolSecGrey,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Rank badge overlay displaying rank index and count
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .background(
                                        if (walletRankIdx == 1) SolGreen else SolPurple,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "R#$walletRankIdx ($mintCount Mints)",
                                    fontSize = 7.5.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Manifest Popup Dialog
    val nft = selectedNft
    if (nft != null) {
        val mintCount = walletCounts[nft.walletAddress] ?: 0
        val walletRankIdx = rankedWallets.indexOf(nft.walletAddress) + 1
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedNft = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard),
                border = androidx.compose.foundation.BorderStroke(2.dp, SolGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🧬 QUANTUM NFT MANIFEST",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SolGreen,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color(0xFF090A0F), RoundedCornerShape(8.dp))
                            .border(1.dp, SolSecGrey.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (nft.imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = nft.imageUri,
                                contentDescription = "Full visualizer preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                "LIVELY GIF PREVIEW",
                                color = SolSecGrey,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090A0F), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "QUANTUM CIRCUIT SEQUENCER:",
                            fontSize = 8.sp,
                            color = SolSecGrey,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = nft.quantumConfig,
                            fontSize = 11.sp,
                            color = SolCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().height(1.dp).background(Color(0x22FFFFFF)))

                        Text(
                            text = "MINTER SOLANA WALLET:",
                            fontSize = 8.sp,
                            color = SolSecGrey,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nft.walletAddress,
                                fontSize = 10.sp,
                                color = SolTextWhite,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Minter Address", nft.walletAddress)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Wallet copied! ${getRandomPositiveEmoji()}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Wallet",
                                    tint = SolGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().height(1.dp).background(Color(0x22FFFFFF)))

                        Text(
                            text = "WALLET ENGAGEMENT RANKING:",
                            fontSize = 8.sp,
                            color = SolSecGrey,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Rank #$walletRankIdx out of all active minters ($mintCount successful NFT mints)",
                            fontSize = 10.sp,
                            color = SolGreen,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().height(1.dp).background(Color(0x22FFFFFF)))

                        Text(
                            text = "TRANSACTION SIGNATURE:",
                            fontSize = 8.sp,
                            color = SolSecGrey,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = nft.txSignature,
                            fontSize = 9.sp,
                            color = SolSecGrey,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().height(1.dp).background(Color(0x22FFFFFF)))

                        Text(
                            text = "MINT TIMESTAMP:",
                            fontSize = 8.sp,
                            color = SolSecGrey,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        val dateString = remember(nft.timestamp) {
                            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(nft.timestamp))
                        }
                        Text(
                            text = dateString,
                            fontSize = 9.sp,
                            color = SolTextWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedNft = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SolGreen),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("CLOSE MANIFEST", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun SchedulerTab(viewModel: AgentViewModel) {
    val schedulerSlotsState by viewModel.schedulerSlots.collectAsState()
    val isVerifying by viewModel.isVerifyingNews.collectAsState()
    val verificationLog by viewModel.verificationProcessLog.collectAsState()
    val confidence by viewModel.verificationConfidence.collectAsState()

    var activeEditingId by remember { mutableStateOf(-1) }
    var activeEditingText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // News Agent Panel Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, Color(0x1AFF4B72), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SolRed, RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "GCP & SOLANA WEB3 NEWS AGENT",
                                color = SolRed,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0x1A14F195), RoundedCornerShape(4.dp))
                                .border(1.dp, SolGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "ONLINE 🛰️",
                                color = SolGreen,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Responsible for high-fidelity news extraction & autonomous social verification syncing. Powered by the zero-hallucination verification pipeline.",
                        color = SolSecGrey,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Live Zero Hallucination Scanner Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x208D90A5), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SolDarkCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "ZERO-HALLUCINATION VERIFIER ENGINE",
                        color = SolGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.runZeroHallucinationSweep() },
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("run_verifier_sweep_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SolGreen),
                            enabled = !isVerifying
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            } else {
                                Text("Execute Security Sweep", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (confidence > 0.0) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF0F1E19), RoundedCornerShape(4.dp))
                                    .border(1.dp, SolGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "CONFIDENCE: ${confidence}% 🛡️",
                                    color = SolGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (verificationLog.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF090A0F), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                verificationLog.forEach { logLine ->
                                    Text(
                                        text = logLine,
                                        color = if (logLine.contains("Complete")) SolGreen else SolSecGrey,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Scheduler Control Group
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "DAILY CORE SCHEDULER (18 SLOTS)",
                    color = SolTextWhite,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                
                Button(
                    onClick = { viewModel.rescheduleAllSlots() },
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("reschedule_all_slots_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x309945FF)),
                    border = BorderStroke(1.dp, SolPurple),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("Reschedule Pending", color = SolPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Slots grid list
        items(schedulerSlotsState) { slot ->
            val slotBg = when (slot.status) {
                "TRANSMITTED" -> Color(0xFF071115)
                "ACTIVE" -> Color(0xFF0E1A14)
                "LOCKED" -> Color(0xFF1E0F14)
                else -> Color(0xFF19130D)
            }
            val slotBorder = when (slot.status) {
                "TRANSMITTED" -> SolCyan
                "ACTIVE" -> SolGreen
                "LOCKED" -> SolRed
                else -> Color(0xFFF39C12)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, slotBorder.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = slotBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SLOT #${slot.id} ",
                                fontWeight = FontWeight.Bold,
                                color = SolTextWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF0C0D14), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    slot.timeLabel,
                                    color = SolSecGrey,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(slotBorder.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, slotBorder.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                slot.status,
                                color = slotBorder,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeEditingId == slot.id) {
                        OutlinedTextField(
                            value = activeEditingText,
                            onValueChange = { activeEditingText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slot_edit_input_${slot.id}"),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SolTextWhite,
                                unfocusedTextColor = SolTextWhite,
                                focusedBorderColor = SolPurple,
                                unfocusedBorderColor = Color(0x309945FF)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { activeEditingId = -1 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Cancel", color = SolSecGrey, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateScheduleSlot(slot.id, activeEditingText)
                                    activeEditingId = -1
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SolPurple),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("slot_save_btn_${slot.id}")
                            ) {
                                Text("Save Draft", color = SolTextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = slot.draftText,
                            color = SolTextWhite,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { 
                                        activeEditingId = slot.id 
                                        activeEditingText = slot.draftText
                                    },
                                    modifier = Modifier
                                        .height(26.dp)
                                        .testTag("slot_modify_btn_${slot.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1D2A)),
                                    border = BorderStroke(0.5.dp, SolSecGrey),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit manual slot", tint = SolTextWhite, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit Draft", color = SolTextWhite, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                                
                                Button(
                                    onClick = { viewModel.toggleScheduleSlot(slot.id) },
                                    modifier = Modifier
                                        .height(26.dp)
                                        .testTag("slot_toggle_btn_${slot.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1D2A)),
                                    border = BorderStroke(0.5.dp, SolSecGrey),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = if (slot.isEnabled) "Mute Slot" else "Unmute Slot", 
                                        color = if (slot.isEnabled) SolRed else SolGreen, 
                                        fontSize = 8.sp, 
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (slot.status != "TRANSMITTED") {
                                Button(
                                    onClick = { viewModel.broadcastScheduleSlot(slot.id) },
                                    modifier = Modifier
                                        .height(26.dp)
                                        .testTag("slot_broadcast_btn_${slot.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3014F195)),
                                    border = BorderStroke(0.5.dp, SolGreen),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Broadcast Now", color = SolGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SplashLoader(onLoadingComplete: () -> Unit) {
    var progress by remember { mutableStateOf(0.0f) }
    var currentLog by remember { mutableStateOf("Initializing neural network...") }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        val logs = listOf(
            "🧠 Booting neural networks and model weights...",
            "🛰️ Handshaking with Solana mainnet RPC nodes...",
            "🔐 Opening encrypted sentiment databanks...",
            "🛡️ Loading Sentinel filter algorithms...",
            "📈 Synchronizing DAO sponsorship metrics...",
            "🚀 Deploying Web3 Viral news radar loops...",
            "✨ Feed synchronized! Launching Sentinel Agent."
        )
        
        for (i in 1..100) {
            delay(20) // total ~2.0 seconds
            progress = i / 100f
            when (i) {
                1 -> currentLog = logs[0]
                15 -> currentLog = logs[1]
                35 -> currentLog = logs[2]
                55 -> currentLog = logs[3]
                70 -> currentLog = logs[4]
                85 -> currentLog = logs[5]
                98 -> currentLog = logs[6]
            }
        }
        delay(300)
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF07080F),
                        Color(0xFF140D2D),
                        Color(0xFF030408)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulseScale)
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(SolPurple, SolCyan, SolGreen, SolPurple)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .shadow(16.dp, RoundedCornerShape(24.dp), clip = true)
                    .background(Color(0xFF0A0B14))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "TORDAOweb3 News Agent Logo",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "TORDAOweb3 NEWS AGENT",
                color = SolTextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Text(
                text = "NEURAL VIRAL SENTINEL",
                color = SolPurple,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            Column(
                modifier = Modifier.width(280.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SYNAPSE STATUS:",
                        color = SolSecGrey,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        color = SolGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                LinearProgressIndicator(
                    progress = animatedProgress,
                    color = SolCyan,
                    trackColor = Color(0x308D90A5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = currentLog,
                    color = Color(0xFF90D060),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF06070B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .border(1.dp, Color(0xFF151824), RoundedCornerShape(6.dp))
                )
            }
        }
    }
}
