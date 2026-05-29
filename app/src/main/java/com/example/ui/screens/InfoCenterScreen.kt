package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiService
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourtViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCenterScreen(viewModel: CourtViewModel) {
    var showTrending by remember { mutableStateOf(false) }
    var showLiveStages by remember { mutableStateOf(false) }
    var showTopJurors by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showEvents by remember { mutableStateOf(false) }

    val isGeminiLive = GeminiService.isApiKeyConfigured()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "COMMUNITY INTEL • HUB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CourtroomGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "COMMUNITY HUB",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = CourtroomGold
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightSlate
                )
            )
        },
        containerColor = MidnightSlate
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Header - Redesigned to be futuristic, trustworthy, minimal and centered (Req 4)
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                border = BorderStroke(1.dp, if (isGeminiLive) EmpathyGreen.copy(alpha = 0.3f) else CourtroomGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isGeminiLive) EmpathyGreen.copy(alpha = 0.12f) else CourtroomGold.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Gemini Icon",
                            tint = if (isGeminiLive) EmpathyGreen else CourtroomGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CO-ARBITER JUDGING STATUS",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isGeminiLive) EmpathyGreen else CourtroomGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isGeminiLive) "GEMINI 3.5 FLASH • ONLINE" else "OFFLINE MOCK MODE ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = TextGray
                        )
                    }

                    Text(
                        text = if (isGeminiLive) "Awesome! Your secrets are fully configured. The app is sending requests straight to Gemini 3.5 Flash for live judgment summaries and toxicity filtration."
                               else "The app is currently running in offline mock fallback mode because no custom GEMINI_API_KEY is detected in your Secrets panel yet. Enter a key to enable live AI generated answers!",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                        color = PureIce.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // DIAGNOSTICS CONTROL CENTER
            Text(
                text = "DEMO SIMULATOR PLAYGROUND",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtroomGold,
                    letterSpacing = 1.5.sp
                )
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Alter database values live to simulate consensus outcomes:",
                        fontSize = 11.sp,
                        color = TextGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.addGavelPoints(100) },
                            colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("claim_free_pts")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = MidnightSlate,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Grant +100 pts", color = MidnightSlate, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.resetAppStore() },
                            colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reset_votes_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Demobase", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SOCIAL COMMUNTIY HUB (Req 5)
            Text(
                text = "INTERNET COURT SOCIAL DIRECTORY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtroomGold,
                    letterSpacing = 1.5.sp
                )
            )

            // Trending Disputes Card
            BriefExpansionCard(
                title = "🔥 Trending Internet Disputes",
                expanded = showTrending,
                onClick = { showTrending = !showTrending }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimelineItem("No. 1 Drama: Midnight Blender Rage", "Over 2,400 active jurors debating whether midnight protein shakes constitute acoustic assault. ESH (Everyone Sucks Here) currently leads the verdict polls.")
                    TimelineItem("No. 2 Drama: Joint Account Extortion", "Roommate hosting a local Minecraft server demanding others compensate the shared electric utility. Verdict: Opponent is 98% in the wrong!")
                    TimelineItem("No. 3 Drama: Passive-aggressive Dish sticky-notes", "A kitchen stalemate: taping moldy plates to bedroom doors leads to a deep communal debate on communication etiquette.")
                }
            }

            // Live Stage Jury Rooms Card
            BriefExpansionCard(
                title = "🎙️ Live Stage Jury Rooms",
                expanded = showLiveStages,
                onClick = { showLiveStages = !showLiveStages }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimelineItem("Room A: Roommate Hygiene Tribunal [LIVE]", "Active vocal panel resolving moldy sink warfare. 14 jurors currently on stage speaking. Audience voting is open.")
                    TimelineItem("Room B: Shared Laundry Theft Hearings [Starts in 2h]", "Scheduled defense of a renter accused of leaving damp loads in shared dryers for 48 consecutive hours.")
                    TimelineItem("Room C: The Sriracha Infraction [Scheduled]", "A lightweight comedy debate: roommate accused of using 90% of a joint condiment bottle without replacement.")
                }
            }

            // Top Jurors Card
            BriefExpansionCard(
                title = "🏆 Elite Juror Standings",
                expanded = showTopJurors,
                onClick = { showTopJurors = !showTopJurors }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimelineItem("Rank 1: @GavelGigaChad (12,850 XP)", "Maintained a 94d voting streak with an exceptional 89.2% consensus accuracy on corporate workplace files.")
                    TimelineItem("Rank 2: @SocratesOnline (9,410 XP)", "Elite jurist awarded 42 custom wisdom medallions by the Grand Jury council. Recognized for helpful explanatory notes.")
                    TimelineItem("Rank 3: @EthicWrangler (8,200 XP)", "Consensus moderator of the Romance and Friendship subdivisions. Consistently de-escalates heated disputes.")
                }
            }

            // Historic Verdicts Card
            BriefExpansionCard(
                title = "📁 Consolidated Case Archives",
                expanded = showHistory,
                onClick = { showHistory = !showHistory }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimelineItem("Case #8,419: Net electric bills split", "Closed. Consensus: defendant ordered to relocate GPU rigs or compute precise sub-meter calculations for fair payments.")
                    TimelineItem("Case #8,415: The stolen lunch box", "Closed. Verdict: defendant guilty of unprompted sandwich confiscation. Ordered to replenish pantry with luxury snacks.")
                    TimelineItem("Case #8,412: Left on read for 72 hours", "Closed. Case dismissed: communication breakdown caused by natural battery depletion. Truce declared.")
                }
            }

            // Upcoming events Card
            BriefExpansionCard(
                title = "📅 Scheduled Arena Events",
                expanded = showEvents,
                onClick = { showEvents = !showEvents }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimelineItem("Friday 8PM: Tech Channel Roast Battle", "Guest moderation by top hardware reviewers. The jury will decide whether RGB setup aesthetics deserve high courtroom fines.")
                    TimelineItem("Sunday 4PM: Couples Therapy Comedy Stage", "A lighthearted communal courtroom helping partners settle playful chores (dishwashing rotation vs dog walking duties) in real-time.")
                }
            }
        }
    }
}

@Composable
fun BriefExpansionCard(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
        border = BorderStroke(1.dp, LightSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = CourtroomGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun TimelineItem(headline: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MidnightSlate)
            .border(1.dp, LightSlate, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = headline, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CourtroomGold)
        Text(text = description, fontSize = 11.sp, color = PureIce.copy(alpha = 0.9f), lineHeight = 15.sp)
    }
}
