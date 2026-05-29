package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourtViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(viewModel: CourtViewModel) {
    val stats by viewModel.statsState.collectAsState()

    // Mock competitors
    val competitors = listOf(
        LeaderboardEntry("JusticeSarah", 1420, 18, "Supreme Court Elite"),
        LeaderboardEntry("GavelMaster99", 1150, 12, "Bailiff of Drama"),
        LeaderboardEntry("NoDramaPls", 930, 9, "Amicus Brief Advocate"),
        LeaderboardEntry("You (Jury Intern)", stats?.gavelPoints ?: 150, stats?.streakCount ?: 3, "Jury Duty Intern", isUser = true),
        LeaderboardEntry("SmoothieMaker", 110, 2, "Courtroom Clown"),
        LeaderboardEntry("SaltySocks", 45, 1, "Litigation Intern")
    ).sortedByDescending { it.points }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LEAGUE STANDINGS • DIVISION A",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CourtroomGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GAVEL LEAGUE",
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
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 96.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                // League Category
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "League Trophy",
                            tint = CourtroomGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "🏆 Golden Jury Barristers Division",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Top 3 players rank up to the Supreme Council at the end of the day. Earn points by voting!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ACTIVE LEADERBOARD COUNTS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CourtroomGold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            itemsIndexed(competitors) { index, entry ->
                LeaderboardRow(index + 1, entry)
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    val isTop3 = rank <= 3
    val rankBadgeUrlColor = when (rank) {
        1 -> CourtroomGold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> TextGray
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isUser) CourtroomGold.copy(alpha = 0.12f) else DeepCharcoal
        ),
        border = BorderStroke(
            1.dp,
            if (entry.isUser) CourtroomGold.copy(alpha = 0.4f) else Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_$rank")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank and name
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Rank Number / Medal
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isTop3) rankBadgeUrlColor.copy(alpha = 0.2f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        fontWeight = FontWeight.Bold,
                        color = if (isTop3) rankBadgeUrlColor else TextGray,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Competitor Profile Details
                Column {
                    Text(
                        text = entry.name,
                        fontWeight = if (entry.isUser) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (entry.isUser) CourtroomGold else Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Rank: ${entry.rankName}",
                        fontSize = 10.sp,
                        color = TextGray
                    )
                }
            }

            // Streak and Points values
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Streak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = WarmFlame,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${entry.streak}d",
                        color = WarmFlame,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Points total
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Points Balance",
                        tint = CourtroomGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${entry.points} pts",
                        color = CourtroomGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

data class LeaderboardEntry(
    val name: String,
    val points: Int,
    val streak: Int,
    val rankName: String,
    val isUser: Boolean = false
)
