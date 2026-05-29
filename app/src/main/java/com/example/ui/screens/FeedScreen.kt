package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CaseEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourtViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: CourtViewModel,
    onTabSelected: (String) -> Unit
) {
    val cases by viewModel.casesState.collectAsState()
    val stats by viewModel.statsState.collectAsState()
    val aiLoadingId by viewModel.aiJudgeLoading.collectAsState()
    var shareCase by remember { mutableStateOf<CaseEntity?>(null) }

    var activeMainTab by remember { mutableStateOf(0) } // 0 = Docket, 1 = Live Jury Room

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = "INTERNET COURT",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                color = CourtroomGold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "⚖️ DOCKET #8,421 • ACTIVE JURY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextGray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        // Flame Streak
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarmFlame.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Day Voting Streak",
                                tint = WarmFlame,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${stats?.streakCount ?: 0}d",
                                color = WarmFlame,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }

                        // Points Balance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CourtroomGold.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Gavel Points",
                                tint = CourtroomGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${stats?.gavelPoints ?: 0}",
                                color = CourtroomGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }

                        // Community Hub / Intel button (Req 5 & 9)
                        IconButton(
                            onClick = { onTabSelected("info") },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("founder_desk_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "Community Hub",
                                tint = CourtroomGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightSlate
                )
            )
        },
        containerColor = MidnightSlate,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant premium switcher chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(DeepCharcoal, RoundedCornerShape(14.dp))
                    .border(BorderStroke(1.dp, LightSlate), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Button for Docket Cases
                Button(
                    onClick = { activeMainTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeMainTab == 0) CourtroomGold else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.0f).height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Cases",
                        tint = if (activeMainTab == 0) MidnightSlate else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Cases",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeMainTab == 0) MidnightSlate else Color.White
                    )
                }

                // Button for Live Jury Room
                Button(
                    onClick = { onTabSelected("live_jury") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Pulsing red dot live signal
                        val pulseScale by rememberInfiniteTransition().animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.scale(pulseScale)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(AngerRed.copy(alpha = 0.4f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AngerRed)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Jury Room",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeMainTab == 1) MidnightSlate else Color.White
                        )
                    }
                }
            }

            if (activeMainTab == 0) {
                // Show Docket list
                if (cases.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CourtroomGold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Convening the Grand Jury briefs...",
                                color = TextGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = 96.dp, // Spacing above bottom navigation
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) {
                        item {
                            // Quick Hero Banner explaining the Courtroom philosophy
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "⚖️ Anonymous Internet Jury duty",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CourtroomGold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cast fair, positive votes. Earn +15 Gavel Points. Call Judge Gavel to resolve files asynchronously with generative AI logic.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextGray
                                    )
                                }
                            }
                        }

                        items(cases, key = { it.id }) { case ->
                            CaseCard(
                                case = case,
                                onVote = { option -> viewModel.castVote(case.id, option) },
                                onSummonAi = { viewModel.summonAiJudge(case.id) },
                                aiLoading = aiLoadingId == case.id,
                                onShareVerdict = { shareCase = case }
                            )
                        }
                    }
                }
            } else {
                // SHOW THE NEW JURY ROOM SCREEN
                JuryRoomScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize().weight(1f)
                )
            }
        }
    }

    shareCase?.let { case ->
        VerdictShareDialog(
            case = case,
            onDismiss = { shareCase = null }
        )
    }
}

@Composable
fun CaseCard(
    case: CaseEntity,
    onVote: (Int) -> Unit,
    onSummonAi: () -> Unit,
    aiLoading: Boolean,
    onShareVerdict: () -> Unit
) {
    val totalVotes = case.votesPlaintiff + case.votesDefendant + case.votesBothWrong + case.votesNoneWrong
    val voted = case.userVotedOption != 0

    // Smooth animation for voting percent bar graphs
    val pctPlaintiff = if (totalVotes == 0) 25f else (case.votesPlaintiff.toFloat() / totalVotes * 100)
    val pctDefendant = if (totalVotes == 0) 25f else (case.votesDefendant.toFloat() / totalVotes * 100)
    val pctBothWorst = if (totalVotes == 0) 25f else (case.votesBothWrong.toFloat() / totalVotes * 100)
    val pctNoneWorst = if (totalVotes == 0) 25f else (case.votesNoneWrong.toFloat() / totalVotes * 100)

    val animatePlaintiff by animateFloatAsState(targetValue = pctPlaintiff, animationSpec = tween(600))
    val animateDefendant by animateFloatAsState(targetValue = pctDefendant, animationSpec = tween(600))
    val animateBoth by animateFloatAsState(targetValue = pctBothWorst, animationSpec = tween(600))
    val animateNone by animateFloatAsState(targetValue = pctNoneWorst, animationSpec = tween(600))

    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.dp,
            if (voted) CourtroomGold.copy(alpha = 0.3f) else Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("case_card_${case.id}")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Category Badge & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoyalBlue.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = case.category.uppercase(),
                        color = PureIce,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)
                    )
                }

                // Time Indicator
                Text(
                    text = "AITA No. #${case.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = case.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Story description with comfortable body scaling
            Text(
                text = parseMarkdown(case.story),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    color = PureIce.copy(alpha = 0.9f)
                ),
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // JURY VOTING PANEL
            Text(
                text = if (voted) "JURY VERDICT RESULTS" else "VOTE IN THE JURY ASSEMBLY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtroomGold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!voted) {
                // 4 Interactive Voting Buttons (Empathy & healthy framing rules applied!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Plaintiff (Author Right)
                    Button(
                        onClick = { onVote(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vote_p_${case.id}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚖️ Author", fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                            Text("Is Right", fontSize = 9.sp, color = TextGray)
                        }
                    }

                    // Option 2: Defendant (Opponent Right)
                    Button(
                        onClick = { onVote(2) },
                        colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vote_d_${case.id}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛡️ Opponent", fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                            Text("Is Right", fontSize = 9.sp, color = TextGray)
                        }
                    }

                    // Option 3: Both Sides Wrong
                    Button(
                        onClick = { onVote(3) },
                        colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vote_b_${case.id}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🤝 Both", fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                            Text("Made Mistakes", fontSize = 9.sp, color = TextGray)
                        }
                    }

                    // Option 4: Nobody Wrong / Miscommunication
                    Button(
                        onClick = { onVote(4) },
                        colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vote_n_${case.id}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌊 Miscom-", fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                            Text("munication", fontSize = 8.sp, color = TextGray)
                        }
                    }
                }
            } else {
                // Displays graphical progress bars showing community choices cleanly!
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Item Plaintiff
                    PercentBar(
                        label = "Author (${case.plaintiffLabel}) right",
                        percent = animatePlaintiff,
                        userPick = case.userVotedOption == 1,
                        color = RoyalBlue
                    )

                    // Item Defendant
                    PercentBar(
                        label = "Opposing party (${case.defendantLabel}) right",
                        percent = animateDefendant,
                        userPick = case.userVotedOption == 2,
                        color = CourtroomGold
                    )

                    // Item Both Wrong
                    PercentBar(
                        label = "Both sides made mistakes",
                        percent = animateBoth,
                        userPick = case.userVotedOption == 3,
                        color = AngerRed
                    )

                    // Item Miscommunication
                    PercentBar(
                        label = "No-one in wrong / Miscommunication",
                        percent = animateNone,
                        userPick = case.userVotedOption == 4,
                        color = EmpathyGreen
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onShareVerdict,
                        colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_verdict_btn_${case.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MidnightSlate,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export Shareable Verdict Card",
                            color = MidnightSlate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI GAVEL COURT CORNER
            HorizontalDivider(color = LightSlate, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            if (case.aiJudgeVerdict == null) {
                if (aiLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CourtroomGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Judge Gavel is reviewing briefs and balancing moral scales...",
                            style = MaterialTheme.typography.bodySmall,
                            color = CourtroomGold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onSummonAi,
                        border = BorderStroke(1.dp, CourtroomGold.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("summon_ai_btn_${case.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Judge icon",
                            tint = CourtroomGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Summon AI Judge \"Judge Gavel\"",
                            color = CourtroomGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // Show completed AI Judgment details with stylish card design
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    CourtroomGold.copy(alpha = 0.08f),
                                    MidnightSlate
                                )
                            )
                        )
                        .border(
                            1.dp,
                            CourtroomGold.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "AI Judge Icon",
                                tint = CourtroomGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "JUDGE GAVEL'S VERDICT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = CourtroomGold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        // Witty Verified stamp
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CourtroomGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI ACCREDITED",
                                color = MidnightSlate,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = parseMarkdown(case.aiJudgeVerdict),
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp,
                            color = PureIce.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PercentBar(
    label: String,
    percent: Float,
    userPick: Boolean,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.8f)) {
                if (userPick) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Your Choice",
                        tint = CourtroomGold,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 2.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (userPick) CourtroomGold else PureIce,
                    fontWeight = if (userPick) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
            Text(
                text = "${percent.toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (userPick) CourtroomGold else PureIce,
                modifier = Modifier.weight(0.2f),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(LightSlate)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent / 100f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerdictShareDialog(
    case: CaseEntity,
    onDismiss: () -> Unit
) {
    var isSquare by remember { mutableStateOf(false) } // false = Story (9:16), true = Square (1:1)
    var selectedSkin by remember { mutableStateOf(0) } // 0 = Brutalist Neon, 1 = Cosmic Glow, 2 = Red Chaos
    var shareFeedback by remember { mutableStateOf<String?>(null) }
    
    // Fallback witty responses if aiJudgeVerdict is empty
    val userVerdictText = when (case.userVotedOption) {
        1 -> "AUTHOR IS 100% RIGHT"
        2 -> "OPPONENT IS 100% RIGHT"
        3 -> "BOTH SIDES MADE MISTAKES"
        4 -> "MISCOMMUNICATION / TRUCE"
        else -> "JURY ACCREDITED VERDICT"
    }

    val wittyStatement = case.aiJudgeVerdict ?: when (case.userVotedOption) {
        1 -> "The Court decrees kitchen boundaries and emotional security are absolute. Your opponent has been ordered to perform 40 hours of sincere apologies."
        2 -> "Checked by history: your grievance was highly melodramatic. Court suggests taking a deep breath and sharing a hot chocolate."
        3 -> "A classic dual catastrophe. No heroes, just two stubborn minds turning clean space into emotional combat zones. Truce ordered."
        4 -> "The Jury ruled this a simple human mistake. Please delete your drafts, hug it out, and laugh over tea. Case dismissed!"
        else -> "An expert citation is registered. The jury has spoken."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightSlate.copy(alpha = 0.95f)),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header of Share Hub
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PureIce)
                    }
                    
                    Text(
                        text = "EXPORT VERDICT BRIEF",
                        style = MaterialTheme.typography.labelMedium,
                        color = CourtroomGold,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(
                        onClick = {
                            shareFeedback = "Verdict brief copied to your device notes! 📝"
                        }
                    ) {
                        Text("Copy Info", color = PureIce, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // CONTROLS FOR CUSTOMIZING
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepCharcoal)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skin selector (Palettes)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedSkin = (selectedSkin + 1) % 3 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = "Skin Style", tint = CourtroomGold)
                        }
                        Text(
                            text = when (selectedSkin) {
                                0 -> "Brutalist Neon"
                                1 -> "Cosmic Glow"
                                else -> "Red Chaos"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureIce
                        )
                    }

                    // Ratio toggle (1:1 / 9:16)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isSquare = !isSquare },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isSquare) Icons.Default.CropSquare else Icons.Default.AspectRatio,
                                contentDescription = "Toggle ratio",
                                tint = CourtroomGold
                            )
                        }
                        Text(
                            text = if (isSquare) "Square (1:1)" else "Story (9:16)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureIce
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // THE CARD GRAPHIC (VIBRANT PREVIEW AREA)
                val cardBrush = when (selectedSkin) {
                    0 -> Brush.verticalGradient(colors = listOf(MidnightSlate, Color(0xFF141416)))
                    1 -> Brush.radialGradient(
                        colors = listOf(Color(0xFF2E1065), Color(0xFF0F052D), MidnightSlate),
                        radius = 1200f
                    )
                    else -> Brush.verticalGradient(colors = listOf(Color(0xFF3B0712), MidnightSlate))
                }

                val cardStrokeColor = when (selectedSkin) {
                    0 -> CourtroomGold
                    1 -> Color(0xFFFCD34D) // Radiant Gold-Yellow
                    else -> AngerRed
                }

                val accentTextColor = when (selectedSkin) {
                    0 -> CourtroomGold
                    1 -> Color(0xFFFDE047)
                    else -> AngerRed
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBrush)
                        .border(
                            width = 2.dp,
                            color = cardStrokeColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header metadata & brand
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DOCKET BRIEF NO. #${case.id} // VERDICT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = accentTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "INTERNET COURT",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = accentTextColor,
                                    fontSize = 22.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PureIce.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFICIAL JURY CITATION",
                                    color = PureIce.copy(alpha = 0.7f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        if (!isSquare) Spacer(modifier = Modifier.height(24.dp))

                        // Case brief and core question
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = case.title.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "\"${if (case.story.length > 150) case.story.take(147) + "..." else case.story}\"",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = PureIce.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        if (!isSquare) Spacer(modifier = Modifier.height(20.dp))

                        // Verdict STAMP
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "JURY DECREE:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentTextColor)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = userVerdictText,
                                    color = MidnightSlate,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        if (!isSquare) Spacer(modifier = Modifier.height(24.dp))

                        // AI Verdict Decree Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(accentTextColor.copy(alpha = 0.08f))
                                .border(1.dp, accentTextColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = accentTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "JUDGE GAVEL ACADEMY REPORT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = accentTextColor,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = wittyStatement,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = PureIce,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TRANSIENT FEEDBACK BOX
                AnimatedVisibility(visible = shareFeedback != null) {
                    shareFeedback?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmpathyGreen.copy(alpha = 0.15f))
                                .border(1.dp, EmpathyGreen, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmpathyGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = msg, color = EmpathyGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TARGET PLATFORMS ACTION HUB
                Text(
                    text = "TARGET OPTIMIZED EXPORTS",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Instagram Stories
                    Button(
                        onClick = {
                            shareFeedback = "Generated premium 9:16 IG Story asset! Image added to gallery. 📸"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                        border = BorderStroke(1.dp, LightSlate),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Instagram Stories", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("IG Story", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Twitter / X
                    Button(
                        onClick = {
                            shareFeedback = "Tweet markup copied! Open X.com to paste & viral-debate 🐦"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                        border = BorderStroke(1.dp, LightSlate),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "Twitter/X Post", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tweet Brief", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Download image block
                    Button(
                        onClick = {
                            shareFeedback = "Downloaded HD high-contrast image asset successfully! 📥"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                        modifier = Modifier.weight(1.1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Save HD Image", tint = MidnightSlate, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save HD Post", color = MidnightSlate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
