package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourtViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// High-fidelity structures for simulated comments
data class CourtComment(
    val id: String,
    val username: String,
    val text: String,
    val badge: String = "JURY",
    val badgeColor: Color = RoyalBlue,
    val isSystem: Boolean = false,
    val isCensored: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun JuryRoomScreen(
    viewModel: CourtViewModel,
    onTabSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Current layout tab: 0 = live stage, 1 = ranks & gamification, 2 = safety / anti-toxicity, 3 = premium
    var activeSubTab by remember { mutableStateOf(0) }
    
    // Stats and accounts linked to DB user stats
    val dbStats by viewModel.statsState.collectAsState()
    val startingPoints = dbStats?.gavelPoints ?: 150
    val startingStreak = dbStats?.streakCount ?: 3
    
    // Live reactive states for the room
    var juryConsensus by remember { mutableStateOf(58) } // Percentage voting for Plaintiff (Author Right)
    var userObjectionActive by remember { mutableStateOf(false) }
    var activeVerdictVerdictText by remember { mutableStateOf<String?>(null) }
    var userVoteOption by remember { mutableStateOf(1) } // 1 = Left side, 2 = Right side
    var voiceQueueStatus by remember { mutableStateOf(0) } // 0 = Not queued, 1 = Queued (pos #3), 2 = On stage!
    var localXP by remember { mutableStateOf(1250) } // XP accumulation simulation
    var showXpToast by remember { mutableStateOf<String?>(null) }
    var currentEvidenceDrop by remember { mutableStateOf<String?>(null) }
    var customDraftComment by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Set of clever comments that loop through Twitch-style chat
    val dynamicComments = remember {
        mutableStateListOf(
            CourtComment("1", "AngryGamer_99", "Objection! Smoothie blender at midnight is a literal acoustics crime! 😂", "PROSECUTOR", AngerRed),
            CourtComment("2", "PeacekeeperSarah", "But laying moldy plates on a bed is biological warfare guys! both wrong", "ANALYST", EmpathyGreen),
            CourtComment("3", "AI_Judge_Givel_Fan", "Waiting for Judge Gavel to crush them with legal wisdom, let's go!", "ELITE", CourtroomGold),
            CourtComment("4", "NoSimpZone", "Tbh roommate took 5 warnings and did nothing. Deserved bed treatment.", "DEFENDER", RoyalBlue),
            CourtComment("5", "SaltyRoommate_Stander", "No way, blenders at midnight violate landlord noise clauses!", "JURY", TextGray)
        )
    }

    // New simulated comments loop to keep the room feeling ALIVE
    LaunchedEffect(Unit) {
        val simulatedStatements = listOf(
            "Roommate should get sentenced to doing laundry for 10 weeks!",
            "OBJECTION! Can we verify if the roommate actually slept through it?",
            "The Minecraft host rent debate is even spicier, check room #102!",
            "I'm switching to Author right, we need justice for dirty sinks 💀",
            "This Twitch style courtroom chat is addictive as hell!",
            "Just earned +25 XP. I am aiming for True Detective rank tonight!",
            "Our manager wrote someone up for a 6 minute slack response yesterday, real life drama is crazy.",
            "Judge Gavel please release the golden mallet verdict!",
            "Can we drop the noise decibel evidence already?",
            "Roommate leaving cereal bowls to mold is absolute gross behavior."
        )
        val usernames = listOf("Darth_Voter", "GavelGlow", "Salsa_Juror", "Minecraft_Mom", "BrutalistAesthetic", "LegalEagle_AI", "XpJunkie", "Empathetic_Dude")
        val badges = listOf("ROOKIE", "DETECTIVE", "CHROME", "JURY", "LEGEND")
        val badgeColors = listOf(TextGray, CourtroomGold, RoyalBlue, EmpathyGreen, AngerRed)

        var idx = 0
        while (true) {
            try {
                delay(3800)
                if (activeSubTab == 0) {
                    val randUser = usernames.random()
                    val randBadgeIdx = (0..4).random()
                    dynamicComments.add(
                        CourtComment(
                            id = "sim_${System.currentTimeMillis()}_${(1000..9999).random()}",
                            username = randUser,
                            text = simulatedStatements[idx % simulatedStatements.size],
                            badge = badges[randBadgeIdx],
                            badgeColor = badgeColors[randBadgeIdx]
                        )
                    )
                    if (dynamicComments.isNotEmpty() && dynamicComments.size > 22) {
                        dynamicComments.removeAt(0)
                    }
                    
                    // Slightly oscillate consensus
                    val shift = (-2..2).random()
                    juryConsensus = (juryConsensus + shift).coerceIn(15, 85)
                }
                idx++
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Prevent loop exceptions from causing a crash
            }
        }
    }

    // Shake offset animations for Objection!
    val shakeOffset by animateDpAsState(
        targetValue = if (userObjectionActive) 12.dp else 0.dp,
        animationSpec = keyframes {
            durationMillis = 500
            0.dp at 0
            (-10).dp at 100
            10.dp at 200
            (-8).dp at 300
            8.dp at 400
            0.dp at 500
        }
    )

    // Layout nested in Box to isolate receiver environments and hover overlays cleanly
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightSlate)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Stats header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepCharcoal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AngerRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE COURT STAGE • 1.2K ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Founder Info Desk",
                        tint = CourtroomGold,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onTabSelected("info") }
                    )
                }

                // Scoreboard tracker
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = WarmFlame.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = WarmFlame,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${startingStreak}d",
                                color = WarmFlame,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = CourtroomGold.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Points",
                                tint = CourtroomGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${startingPoints} GP",
                                color = CourtroomGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tabs row without custom indicator to prevent indicator offset problems
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = DeepCharcoal,
                contentColor = CourtroomGold,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = "Stage", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lobby STAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = "Ranks", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Jury Ranks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = "Safety", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anti-Toxic", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = "Premium", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Premium", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
            HorizontalDivider(color = LightSlate, thickness = 1.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (activeSubTab) {
                0 -> {
                    // MAIN ACTION LOBBY STAGE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .offset(y = shakeOffset)
                    ) {
                        // Current case header brief
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(DeepCharcoal, Color.Transparent)
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RoyalBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "CASE STUDY #8421 • TARGET TOPIC",
                                        color = PureIce,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "The Roommate's Midnight Smoothie of Vengeance",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Blender noise at 12 AM vs Placing moldy dishes on bed as silent protest.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray
                                )
                            }
                        }

                        // JURY BOX VIEW
                        Text(
                            text = "ACTIVE COURTROOM CHANNELS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CourtroomGold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Speaker 1: Plaintiff (Author)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepCharcoal)
                                    .border(
                                        width = if (userVoteOption == 1) 2.dp else 1.dp,
                                        color = if (userVoteOption == 1) RoyalBlue else LightSlate,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val pulseScale by rememberInfiniteTransition().animateFloat(
                                            initialValue = 0.95f,
                                            targetValue = 1.15f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1200, easing = LinearOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .scale(pulseScale)
                                                .clip(CircleShape)
                                                .background(RoyalBlue.copy(alpha = 0.25f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(RoyalBlue)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Plaintiff",
                                                tint = PureIce,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "CyanGamer",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "PLAINTIFF (AUTHOR)",
                                        fontSize = 8.sp,
                                        color = TextGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🎙️ SPEAKING LIVE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmpathyGreen
                                    )
                                }
                            }

                            // Center: AI Judge Gavel Seat
                            Box(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepCharcoal)
                                    .border(2.dp, CourtroomGold, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(CourtroomGold.copy(alpha = 0.15f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Gavel,
                                            contentDescription = "AI Judge Gavel",
                                            tint = CourtroomGold,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "JUDGE GAVEL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CourtroomGold
                                    )
                                    Text(
                                        text = "AI LAW RECTOR",
                                        fontSize = 8.sp,
                                        color = TextGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CourtroomGold)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE FILTER",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MidnightSlate
                                        )
                                    }
                                }
                            }

                            // Speaker 2: Defendant (Roommate)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepCharcoal)
                                    .border(
                                        width = if (userVoteOption == 2) 2.dp else 1.dp,
                                        color = if (userVoteOption == 2) AngerRed else LightSlate,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(LightSlate)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Defendant",
                                            tint = TextGray,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Roommate_Salty",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "DEFENDANT",
                                        fontSize = 8.sp,
                                        color = TextGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🔇 MUTED",
                                        fontSize = 8.sp,
                                        color = TextGray
                                    )
                                }
                            }
                        }

                        // COURTROOM PARTICIPANT COUNTERS (Requirement 8)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .border(1.dp, LightSlate, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ LIVE COURT ROSTER COUNTERS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CourtroomGold,
                                        letterSpacing = 1.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Pulsing Green LED indicator
                                        val ledPulse by rememberInfiniteTransition().animateFloat(
                                            initialValue = 0.5f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(800, easing = LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .scale(ledPulse)
                                                .clip(CircleShape)
                                                .background(EmpathyGreen)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SYNC OK", color = EmpathyGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = LightSlate.copy(alpha = 0.3f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("JURY ASSEMBLY", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("1,442 Active", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("AI EXECUTIVE BENCH", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("2 Active Judges", color = CourtroomGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("DEBATERS QUEUED", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("5 In Waiting", color = RoyalBlue, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("ACTIVE OBJECTIONS", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("12 Sustained", color = AngerRed, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        // REAL TIME VOTE METER (Consensus Graph)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DeepCharcoal)
                                .border(1.dp, LightSlate, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "⚖️ JURY VOTE CONSENSUS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureIce
                                    )
                                    Text(
                                        text = "Active Jury Box: 48 voters",
                                        fontSize = 9.sp,
                                        color = TextGray
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                val animatedBarSize by animateFloatAsState(targetValue = juryConsensus / 100f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(LightSlate)
                                ) {
                                    Row(modifier = Modifier.fillMaxSize()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(animatedBarSize.coerceAtLeast(0.05f))
                                                .clip(RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp))
                                                .background(RoyalBlue)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight((1f - animatedBarSize).coerceAtLeast(0.05f))
                                                .clip(RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp))
                                                .background(AngerRed)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Author Right: ${juryConsensus}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue
                                    )
                                    Text(
                                        text = "Roommate Right: ${100 - juryConsensus}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AngerRed
                                    )
                                }
                            }
                        }

                        // AI JUDGE INTERACTIVE CHAMBER (Requirement 7)
                        Spacer(modifier = Modifier.height(12.dp))
                        var aiConsultationLoading by remember { mutableStateOf(false) }
                        var aiConsultationCustomInput by remember { mutableStateOf("") }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .border(1.dp, CourtroomGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.SmartToy, contentDescription = "AI Judge", tint = CourtroomGold, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "JUDGE GAVEL'S AI INTUITION CHAMBER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CourtroomGold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ask AI Judge Gavel direct, specialized questions about Roommate's Blender and Dish arguments:",
                                    fontSize = 11.sp,
                                    color = TextGray,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Preset Prompt Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        "Blender vs Bed" to "Blender noise at 12 AM vs moldy plates in bed is a clear clash of volumetric disturbance and biochemical warfare. Bed mold is ruled a 2x major health violation!",
                                        "Rent Penalty?" to "Under common internet lease sense, roommate leaving plates is fined 20% of internet fee. Author running blender at mid-night is sentenced to 2 weeks of using high-speed silent shaker jars.",
                                        "Who is AITA?" to "Verdict: ESH (Everyone Sucks Here). Roommate has passive-aggressive tendencies. Author lacks acoustics empathy. Order a visual chore wheel immediately."
                                    ).forEach { (label, response) ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(LightSlate)
                                                .clickable {
                                                    aiConsultationLoading = true
                                                    coroutineScope.launch {
                                                        delay(1200)
                                                        aiConsultationLoading = false
                                                        activeVerdictVerdictText = "👨‍⚖️ **JUDGE GAVEL DECREE:**\n\n$response"
                                                        localXP += 100
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = label, color = CourtroomGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    TextField(
                                        value = aiConsultationCustomInput,
                                        onValueChange = { aiConsultationCustomInput = it },
                                        placeholder = { Text("Consult Gavel...", fontSize = 11.sp, color = TextGray) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = MidnightSlate,
                                            unfocusedContainerColor = MidnightSlate,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    
                                    Button(
                                        onClick = {
                                            if (aiConsultationCustomInput.isNotBlank()) {
                                                val input = aiConsultationCustomInput
                                                aiConsultationCustomInput = ""
                                                aiConsultationLoading = true
                                                focusManager.clearFocus()
                                                coroutineScope.launch {
                                                    delay(1500)
                                                    aiConsultationLoading = false
                                                    val wittyReplies = listOf(
                                                        "Regarding your specific grievance: \"$input\". The scales of internet law indicate an extreme lack of communications etiquette. I hereby sentence the roommate to doing all the sweeping!",
                                                        "I have audited \"$input\". The common communal workspace code dictates any midnight acoustics are strictly banned. No exceptions!",
                                                        "Interesting brief: \"$input\". Judge Gavel sustains that passive aggressive responses are toxic to household health. Please agree on a weekly trash schedule."
                                                    )
                                                    activeVerdictVerdictText = "👨‍⚖️ **JUDGE GAVEL DECREE:**\n\n${wittyReplies.random()}"
                                                    localXP += 100
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(46.dp)
                                    ) {
                                        if (aiConsultationLoading) {
                                            CircularProgressIndicator(color = MidnightSlate, modifier = Modifier.size(16.dp))
                                        } else {
                                            Icon(Icons.Default.Send, contentDescription = "Consult", tint = MidnightSlate, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // JUROR SPEED ACTION HUBS
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "JUROR CONTROLS (TAP FOR POINTS & XP!)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CourtroomGold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. OBJECTION! Button
                            Button(
                                onClick = {
                                    if (!userObjectionActive) {
                                        userObjectionActive = true
                                        localXP += 100
                                        viewModel.addGavelPoints(25)
                                        coroutineScope.launch {
                                            showXpToast = "🚨 OBJECTION! +25 Gavel Points & +100 XP"
                                            dynamicComments.add(
                                                CourtComment(
                                                    id = "obj_${System.currentTimeMillis()}_${(1000..9999).random()}",
                                                    username = "SYSTEM JURY",
                                                    text = "Juror placed an official Objection! AI Judge reviewing...",
                                                    badge = "SYSTEM",
                                                    badgeColor = CourtroomGold,
                                                    isSystem = true
                                                )
                                            )
                                            delay(500)
                                            userObjectionActive = false
                                            delay(1000)
                                            dynamicComments.add(
                                                CourtComment(
                                                    id = "jg_${System.currentTimeMillis()}_${(1000..9999).random()}",
                                                    username = "JUDGE GAVEL",
                                                    text = "Objection Sustained! Let's de-escalate roommate clutter before building bioweapons.",
                                                    badge = "AI JUDGE",
                                                    badgeColor = CourtroomGold,
                                                    isSystem = false
                                                )
                                            )
                                            delay(1500)
                                            showXpToast = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AngerRed),
                                border = BorderStroke(2.dp, PureIce),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .testTag("button_objection")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Report,
                                        contentDescription = "Objection!",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "OBJECTION!",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // 2. SWITCH SIDES
                            Button(
                                onClick = {
                                    userVoteOption = if (userVoteOption == 1) 2 else 1
                                    juryConsensus = if (userVoteOption == 1) (juryConsensus + 15).coerceAtMost(85) else (juryConsensus - 15).coerceAtLeast(15)
                                    localXP += 50
                                    viewModel.addGavelPoints(15)
                                    coroutineScope.launch {
                                        showXpToast = "🔄 Sides Switched! +15 Gavel Points & +50 XP"
                                        delay(1500)
                                        showXpToast = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                                border = BorderStroke(1.dp, CourtroomGold.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("button_switch")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "Switch", tint = CourtroomGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Switch Side", fontSize = 11.sp, color = CourtroomGold, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 3. EVIDENCE DROP
                            Button(
                                onClick = {
                                    val evidenceTypes = listOf(
                                        "Blender noise graph (92 dB midnight benchmark)",
                                        "Moldy cereal plate photographic report (Exhibit A)",
                                        "Shared space lease clause citation for noise",
                                        "Roommate text history: 5 unreturned sink warnings"
                                    )
                                    val selectedEv = evidenceTypes.random()
                                    currentEvidenceDrop = selectedEv
                                    localXP += 150
                                    viewModel.addGavelPoints(20)
                                    coroutineScope.launch {
                                        showXpToast = "📁 Evidence Dropped! +20 Gavel Points"
                                        dynamicComments.add(
                                            CourtComment(
                                                id = "ev_${System.currentTimeMillis()}_${(1000..9999).random()}",
                                                username = "SYSTEM",
                                                text = "New Evidence filed: $selectedEv",
                                                badge = "EVIDENCE",
                                                badgeColor = RoyalBlue,
                                                isSystem = true
                                            )
                                        )
                                        delay(5000)
                                        currentEvidenceDrop = null
                                        showXpToast = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(48.dp)
                                    .testTag("button_evidence")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Folder, contentDescription = "Evidence", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Evidence Drop", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Speaking queue controls
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    voiceQueueStatus = (voiceQueueStatus + 1) % 3
                                    if (voiceQueueStatus == 2) {
                                        localXP += 250
                                        coroutineScope.launch {
                                            showXpToast = "🎙️ You are now ON STAGE! Mic active. +250 XP"
                                            delay(1500)
                                            showXpToast = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (voiceQueueStatus) {
                                        1 -> CourtroomGold
                                        2 -> EmpathyGreen
                                        else -> DeepCharcoal
                                    }
                                ),
                                border = BorderStroke(1.dp, if (voiceQueueStatus == 0) CourtroomGold else Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (voiceQueueStatus) {
                                            1 -> Icons.Default.PlayArrow
                                            2 -> Icons.Default.Mic
                                            else -> Icons.Default.List
                                        },
                                        contentDescription = "Queue status",
                                        tint = if (voiceQueueStatus == 0) CourtroomGold else MidnightSlate,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (voiceQueueStatus) {
                                            1 -> "Queued (Speaker #3)"
                                            2 -> "ON STAGE - LIVE"
                                            else -> "Join Speaking Queue"
                                        },
                                        color = if (voiceQueueStatus == 0) CourtroomGold else MidnightSlate,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        activeVerdictVerdictText = "Generating verdict from AI Judge Gavel..."
                                        val judgment = "⚖️ **THE LIVE DECREE:** Midnight Blender noise is ruled a petty lease violation, but placing biological mold vectors inside roommates bedding is a class-3 hygiene sin!\n\n**SENTENCE:** Plaintiff must gift Defendant silicone noise-dampening earplugs. Defendant must complete 12 hours of sink stewardship duties or get evicted by internet vote!"
                                        activeVerdictVerdictText = judgment
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Verdict", tint = CourtroomGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Solicit AI Verdict", color = PureIce, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        // ADVANCED SPEAKING QUEUE STATE VISUALIZATIONS (Requirement 9, 10)
                        AnimatedVisibility(visible = voiceQueueStatus != 0) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (voiceQueueStatus == 2) EmpathyGreen else CourtroomGold,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    if (voiceQueueStatus == 1) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.HourglassEmpty, contentDescription = "Waiting", tint = CourtroomGold, modifier = Modifier.size(16.dp))
                                            Text("JUROR DISPUTE WAIT LIST DEBATE", color = CourtroomGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Position: #3 out of 14. Your briefs have been audited as 'highly constructive'. You will be given a 60-second microphone slot soon.",
                                            color = PureIce,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            listOf(
                                                "Draft filed" to true,
                                                "Audited OK" to true,
                                                "Live Spot" to false
                                            ).forEachIndexed { index, (step, checked) ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                        contentDescription = null,
                                                        tint = if (checked) EmpathyGreen else TextGray,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = step, color = if (checked) Color.White else TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                if (index < 2) {
                                                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(LightSlate))
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val micDotPulse by rememberInfiniteTransition().animateFloat(
                                                    initialValue = 0.4f,
                                                    targetValue = 1.2f,
                                                    animationSpec = infiniteRepeatable(
                                                        animation = tween(600, easing = LinearOutSlowInEasing),
                                                        repeatMode = RepeatMode.Reverse
                                                    )
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .scale(micDotPulse)
                                                        .clip(CircleShape)
                                                        .background(AngerRed)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("YOU ARE BROADCASTING LIVE", color = AngerRed, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                            }
                                            Text("Spotlight time: 42s", color = EmpathyGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Sound levels: Balanced. Say why roommates' midnight blending lacks acoustics-empathy! 1,240 people are listening in real-time.",
                                            color = PureIce,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        val voicePhaseTransition = rememberInfiniteTransition(label = "voice_phase")
                                        val voicePhase by voicePhaseTransition.animateFloat(
                                            initialValue = 0f,
                                            targetValue = 6.2831853f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1200, easing = LinearEasing),
                                                repeatMode = RepeatMode.Restart
                                             ),
                                             label = "phase"
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            (1..16).forEach { bId ->
                                                VoiceBar(
                                                    bId = bId,
                                                    phase = voicePhase,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Evidence overbox
                        AnimatedVisibility(visible = currentEvidenceDrop != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RoyalBlue.copy(alpha = 0.15f))
                                    .border(1.dp, RoyalBlue, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Folder, contentDescription = "Ev", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("NEW EVIDENCE FILED BY JUROR", color = RoyalBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(text = currentEvidenceDrop ?: "", color = PureIce, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // AI Verdict Overlay
                        AnimatedVisibility(visible = activeVerdictVerdictText != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.verticalGradient(listOf(DeepCharcoal, MidnightSlate)))
                                    .border(2.dp, CourtroomGold, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "👨‍⚖️ LIVE AI JUDGMENT VERDICT",
                                            fontWeight = FontWeight.Black,
                                            color = CourtroomGold,
                                            fontSize = 12.sp
                                        )
                                        IconButton(
                                            onClick = { activeVerdictVerdictText = null },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = parseMarkdown(activeVerdictVerdictText),
                                        color = PureIce,
                                        fontSize = 11.sp,
                                        lineHeight = 17.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                showXpToast = "Highlight clip saved! 🎥 Ready for TikTok/Insta Reels!"
                                                delay(2000)
                                                showXpToast = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = "Clip", tint = MidnightSlate, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Extract AI TikTok Highlight Clip", color = MidnightSlate, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // TWITCH-STYLE CHAT TITLE
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎙️ AUDIENCE REACTION CHAT (LIVE FEED)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmpathyGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI GUARD ACTIVE",
                                    color = EmpathyGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Reaction chat log
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .border(1.dp, LightSlate, RoundedCornerShape(16.dp))
                        ) {
                            val listState = rememberLazyListState()
                            
                            LaunchedEffect(dynamicComments.size) {
                                if (dynamicComments.isNotEmpty()) {
                                    try {
                                        listState.animateScrollToItem(dynamicComments.size - 1)
                                    } catch (e: Exception) {
                                        try {
                                            listState.scrollToItem(dynamicComments.size - 1)
                                        } catch (ex: Exception) {
                                            // Silently catch layout-measure race condition crashes
                                        }
                                    }
                                }
                            }

                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = dynamicComments,
                                    key = { chat -> chat.id }
                                ) { chat ->
                                    if (chat.isSystem) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(chat.badgeColor.copy(alpha = 0.12f))
                                                .border(0.5.dp, chat.badgeColor, RoundedCornerShape(8.dp))
                                                .padding(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Notifications, contentDescription = "Sys", tint = chat.badgeColor, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = chat.text,
                                                    color = chat.badgeColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(chat.badgeColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = chat.badge,
                                                    color = chat.badgeColor,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = chat.username,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (chat.isCensored) "<CENSORED BY AI JUDGE: Respectful language requested! 🛡️>" else chat.text,
                                                color = if (chat.isCensored) CourtroomGold else PureIce,
                                                fontSize = 11.sp,
                                                fontWeight = if (chat.isCensored) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // AUDIENCE REACTION BAR (Requirement 6, 10)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TAP TO REACT LIVE:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CourtroomGold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            
                            val reactions = listOf(
                                "🔥" to WarmFlame,
                                "🚨" to AngerRed,
                                "😂" to CourtroomGold,
                                "😮" to RoyalBlue,
                                "👍" to EmpathyGreen,
                                "⚖️" to PureIce
                            )
                            
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                reactions.forEach { (emoji, color) ->
                                    var isAnimated by remember { mutableStateOf(false) }
                                    val scale by animateFloatAsState(
                                        targetValue = if (isAnimated) 1.25f else 1.0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow),
                                        finishedListener = { isAnimated = false }
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .scale(scale)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.15f))
                                            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
                                            .clickable {
                                                isAnimated = true
                                                localXP += 15
                                                viewModel.addGavelPoints(2)
                                                coroutineScope.launch {
                                                    showXpToast = "Reacted $emoji! +2 GP & +15 XP"
                                                    dynamicComments.add(
                                                        CourtComment(
                                                            id = "react_${System.currentTimeMillis()}_${(1000..9999).random()}",
                                                            username = "You (Juror)",
                                                            text = "reacted with dual $emoji $emoji $emoji!",
                                                            badge = "REACTION",
                                                            badgeColor = color
                                                        )
                                                    )
                                                    delay(1500)
                                                    showXpToast = null
                                                }
                                            }
                                            .padding(horizontal = 9.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // CHAT ENTRY ROW
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = customDraftComment,
                                onValueChange = { customDraftComment = it },
                                placeholder = { Text("Write constructive argument...", fontSize = 12.sp, color = TextGray) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, CourtroomGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DeepCharcoal,
                                    unfocusedContainerColor = DeepCharcoal,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )

                            Button(
                                onClick = {
                                    if (customDraftComment.isNotBlank()) {
                                        val finalCommentText = customDraftComment
                                        val containsSlur = finalCommentText.lowercase().contains("idiot") ||
                                                finalCommentText.lowercase().contains("garbage") ||
                                                finalCommentText.lowercase().contains("dumb") ||
                                                finalCommentText.lowercase().contains("trash")
                                        
                                        dynamicComments.add(
                                            CourtComment(
                                                id = "user_${System.currentTimeMillis()}_${(1000..9999).random()}",
                                                username = "You (Juror)",
                                                text = finalCommentText,
                                                badge = "JURY PRO",
                                                badgeColor = CourtroomGold,
                                                isCensored = containsSlur
                                            )
                                        )
                                        
                                        if (containsSlur) {
                                            coroutineScope.launch {
                                                showXpToast = "🚨 Toxicity detected! Content auto-moderated."
                                                delay(2200)
                                                showXpToast = null
                                            }
                                        } else {
                                            localXP += 60
                                            viewModel.addGavelPoints(10)
                                            coroutineScope.launch {
                                                showXpToast = "+10 Gavel Points & +60 XP earned!"
                                                delay(1500)
                                                showXpToast = null
                                            }
                                        }
                                        
                                        customDraftComment = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = MidnightSlate, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }

                1 -> {
                    // JURY RANKS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CourtroomGold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(CourtroomGold.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = "Rank",
                                        tint = CourtroomGold,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "CURRENT JUROR LEVEL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                )
                                Text(
                                    text = "TRUTH DETECTIVE (LVL 4)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CourtroomGold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = 0.65f,
                                    color = CourtroomGold,
                                    trackColor = LightSlate,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "1,650 / 2,500 Courtroom XP",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                        }

                        Text(
                            text = "JURY EXPERIENCE RANKS & MILESTONES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureIce,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        val ranks = listOf(
                            RankModel("Rookie Juror", "Starting rank. Earned upon joining first live debate.", true),
                            RankModel("Truth Detective", "Unlocked at 1,000 XP. Grants access to Live Objections.", true),
                            RankModel("Court Analyst", "Unlocked at 5,000 XP. Grants exclusive Evidence file uploads.", false),
                            RankModel("Master Prosecutor", "Unlocked at 10,000 XP. Lets you command live verdicts.", false),
                            RankModel("Elite Judge", "Unlocked at 25,000 XP. Moderate channels & ban safety warning violators.", false)
                        )

                        ranks.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DeepCharcoal)
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (r.active) CourtroomGold.copy(alpha = 0.15f) else LightSlate)
                                ) {
                                    Icon(
                                        imageVector = if (r.active) Icons.Default.CheckCircle else Icons.Default.Lock,
                                        contentDescription = "Status",
                                        tint = if (r.active) CourtroomGold else TextGray,
                                        modifier = Modifier.size(18.dp).align(Alignment.Center)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = r.title,
                                        color = if (r.active) Color.White else TextGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = r.desc,
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                2 -> {
                    // ANTI-TOXIC SERVICE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.border(1.dp, EmpathyGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Safe",
                                    tint = EmpathyGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                                Column {
                                    Text(
                                        text = "AI COURTHOUSE ANTI-TOXIC GUARANTEES",
                                        color = EmpathyGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "We actively filter toxic language, slurs, doxxing, harassment, and harassment mobs. Empathy and constructive arguments are rewarded here.",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "ANTI-TOXIC ENVIRONMENT PILLARS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureIce
                        )

                        val pillars = listOf(
                            PillarModel("1. Real-Time Auto Censorship", "Raw insults such as 'idiot manager' are immediately rewritten or censored as 'highly uncooperative person' by the local AI Judge scanner before being outputted.", Icons.Default.FlashOn),
                            PillarModel("2. Emotional Cooldown Penalty", "Repetitive Objections or toxicity triggers a temporary 10-second mute window to allow the user or jurors to cool down.", Icons.Default.AccessTime),
                            PillarModel("3. Empathy Rewards Multiplying", "Constructive, highly rational arguments loaded with words like 'Please', 'I feel', and 'Truce' earn a 2x Gavel points multiplier.", Icons.Default.Favorite),
                            PillarModel("4. Verification & Guard System", "Our moderator AI acts in the background to avoid dogpiling or cancel culture, encouraging wholesome civil mediation over pure outrage.", Icons.Default.CardMembership)
                        )

                        pillars.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DeepCharcoal)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(imageVector = p.icon, contentDescription = null, tint = CourtroomGold, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(text = p.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = p.desc, color = TextGray, fontSize = 11.sp, lineHeight = 15.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                3 -> {
                    // PREMIUM
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF3B0764), Color(0xFF0F052D), MidnightSlate),
                                        radius = 800f
                                    )
                                )
                                .border(2.dp, CourtroomGold, RoundedCornerShape(24.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(CourtroomGold.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = "Ultra Premium",
                                        tint = CourtroomGold,
                                        modifier = Modifier.size(36.dp).align(Alignment.Center)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "INTERNET COURT ULTRA",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CourtroomGold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "THE SUPREME COURT UPGRADE PACK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureIce
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Unlock professional legal custom gavel skins, premium AI voices for Judge Gavel, cinematic entrance soundboards, and deep private jury room lobbies.",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            showXpToast = "💎 Custom court bundle purchased successfully!"
                                            delay(2000)
                                            showXpToast = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Upgrade Now ($4.99 / Week)", color = MidnightSlate, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = "EXCLUSIVE CO-CREATIVE PERKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureIce,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        val perks = listOf(
                            PerkModel("🔥 Custom 3D Glowing Gavel Skin", "Equip neon pink, gold plated, or futuristic diamond mallets that glow on every OBJECTION! tap.", Icons.Default.FlashOn),
                            PerkModel("🗣️ AI Judge Gavel Voice Packs", "Have your verdicts delivered live in legendary Judge Judy or Arnold Schwarzenegger impressions.", Icons.Default.VolumeUp),
                            PerkModel("🏠 Private Jury Room Locks", "Create private rooms dedicated to friend group drama, roommate clashes, or office arguments.", Icons.Default.Lock),
                            PerkModel("📈 Private Jury Room Data Analytics", "Access expert breakdowns of argument metrics, emotional sentiment, and jury consensus ratios over time.", Icons.Default.BarChart)
                        )

                        perks.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DeepCharcoal)
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = p.icon, contentDescription = null, tint = CourtroomGold, modifier = Modifier.size(24.dp))
                                Column {
                                    Text(text = p.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = p.desc, color = TextGray, fontSize = 11.sp, lineHeight = 15.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        } // Closing the inner Box body
    } // Closing the inner Column layout

        // Floating global toast layout positioned perfectly inside the root Box container
        AnimatedVisibility(
            visible = showXpToast != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .zIndex(100f)
        ) {
            showXpToast?.let { toastMsg ->
                Surface(
                    color = CourtroomGold,
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 12.dp,
                    modifier = Modifier.border(2.dp, PureIce, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = "XP", tint = MidnightSlate, modifier = Modifier.size(16.dp))
                        Text(
                            text = toastMsg,
                            color = MidnightSlate,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Cinematic Full Screen "OBJECTION!" Flash Overlay (Priority 3, 4 & 5)
        AnimatedVisibility(
            visible = userObjectionActive,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(200)),
            exit = fadeOut(animationSpec = (tween(250))) + scaleOut(targetScale = 1.1f, animationSpec = (tween(250))),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(200f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(4.dp, AngerRed)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AngerRed),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .padding(16.dp)
                        .border(10.dp, AngerRed.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Gavel slam",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                        Text(
                            text = "OBJECTION!",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 38.sp,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            text = "AI CO-ARBITER JUDGING ONLINE...",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PureIce.copy(alpha = 0.9f),
                                letterSpacing = 1.5.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } // Closing the root Box layout
}

data class RankModel(
    val title: String,
    val desc: String,
    val active: Boolean
)

data class PillarModel(
    val title: String,
    val desc: String,
    val icon: ImageVector
)

data class PerkModel(
    val title: String,
    val desc: String,
    val icon: ImageVector
)

@Composable
fun VoiceBar(bId: Int, phase: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                val uniqueOffset = bId * 0.4f
                val sine = kotlin.math.sin(phase + uniqueOffset)
                scaleY = 0.2f + 0.8f * ((sine + 1f) / 2f)
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1.0f)
            }
            .clip(RoundedCornerShape(1.dp))
            .background(EmpathyGreen)
    )
}
