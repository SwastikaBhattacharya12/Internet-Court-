package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourtViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: CourtViewModel) {
    val stats by viewModel.statsState.collectAsState()

    var activeQuizSolved by remember { mutableStateOf(false) }
    var pickedOption by remember { mutableStateOf<Int?>(null) } // 1 = YES, 2 = NO
    val correctQuizOption = 1 // YES

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PREDICTIONS • WORKSHOP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CourtroomGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "JURY ACADEMY",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hot streak banner
            Card(
                colors = CardDefaults.cardColors(containerColor = WarmFlame.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, WarmFlame.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Day Voting Streak",
                        tint = WarmFlame,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "${stats?.streakCount ?: 1}-Day Courtroom Streak!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = WarmFlame
                            )
                        )
                        Text(
                            text = "Vote daily to protect your streak. Complete prediction quizzes to double your points multipliers!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prediction Challenge card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LightSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_quiz_card")
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CourtroomGold.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DAILY WORKSHOP SPECIAL",
                                color = CourtroomGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Reward: 25 pts",
                            color = EmpathyGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Case Study: The Cat Godfather Dispute",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "A cat-owner goes out for a 1-hour walk, leaving the screen door slightly loose. Her roommate comes back, doesn't lock the deadlock, and forgets to verify the screen. The cat sneaks out into the adjacent pet-safe hallway, where a neighbor returns it within 5 minutes.\n\nNow, the owner is accusing the roommate of 'attempted cat negligence'. Is the roommate 100% in the wrong here?",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = PureIce.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "PREDICT THE JURY'S CONSENSUS:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextGray,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!activeQuizSolved) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // YES is actually option 1
                            Button(
                                onClick = {
                                    pickedOption = 1
                                    activeQuizSolved = true
                                    viewModel.addGavelPoints(25)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quiz_opt_1")
                            ) {
                                Text(
                                    text = "❌ NO (Jury said Owner was negligent too for leaving screen door loose)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    pickedOption = 2
                                    activeQuizSolved = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quiz_opt_2")
                            ) {
                                Text(
                                    text = "⚖️ YES (Jury voted Roommate was 100% at fault for not checking deadlock)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Answer feedback
                        val userGuessedCorrectly = pickedOption == correctQuizOption
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (userGuessedCorrectly) EmpathyGreen.copy(alpha = 0.15f)
                                        else AngerRed.copy(alpha = 0.15f)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (userGuessedCorrectly) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = "Quiz Status",
                                            tint = if (userGuessedCorrectly) EmpathyGreen else AngerRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (userGuessedCorrectly) "CORRECT! +25 Gavel Points" else "INCORRECT PREDICTION",
                                            fontWeight = FontWeight.Black,
                                            color = if (userGuessedCorrectly) EmpathyGreen else AngerRed,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Text(
                                        text = "Consensus results showed that 68% of users voted BOTH parties made mistakes. Jurors argued that leaving the screen door loose initially is the true root cause, and charging your roommate with godfather negligence is overly dramatic since the cat was safe and within the partition. Empathetic dialogue was suggested.",
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                        color = PureIce
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    activeQuizSolved = false
                                    pickedOption = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Load Next Case Problem", color = MidnightSlate, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bonus Streak Booster Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LightSlate),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = "Tips",
                        tint = CourtroomGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Did you know? Extending your streak to 7 days opens the 'Supreme Council' voting tier where votes carry 2x weight!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
