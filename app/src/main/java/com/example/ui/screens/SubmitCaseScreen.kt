package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun SubmitCaseScreen(
    viewModel: CourtViewModel,
    onSuccessSubmitted: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var plaintiffLabel by remember { mutableStateOf("Me (Author)") }
    var defendantLabel by remember { mutableStateOf("Opponent") }
    var selectedCategory by remember { mutableStateOf("Romance") }
    var story by remember { mutableStateOf("") }

    val categories = listOf("Romance", "Roommates", "Workplace", "Family", "Friendship", "School")

    val checkingToxicity by viewModel.toxicityChecking.collectAsState()
    val toxicityResult by viewModel.toxicityResult.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var triggerEmptyStateError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LITIGATION • DOCKET SUBMISSION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CourtroomGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "FILE A COMPLAINT",
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
            Text(
                text = "State your grievance anonymously. Submissions are auto-filtered for toxicity via Gemini to keep internet court debates creative, funny, and empathetic.",
                fontSize = 12.sp,
                color = TextGray
            )

            // Category Selector Label
            Column {
                Text(
                    text = "DISPUTE CATEGORY",
                    style = MaterialTheme.typography.labelMedium,
                    color = CourtroomGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CourtroomGold else DeepCharcoal)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else LightSlate,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) MidnightSlate else PureIce,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Dispute Title") },
                placeholder = { Text("e.g. Smoothie Blender Noise of Doom") },
                colors = textFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_title_input")
            )

            // Labels Setup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = plaintiffLabel,
                    onValueChange = { plaintiffLabel = it },
                    label = { Text("Your Label (Plaintiff)") },
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("submit_author_input")
                )

                OutlinedTextField(
                    value = defendantLabel,
                    onValueChange = { defendantLabel = it },
                    label = { Text("Opponent Label") },
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("submit_opponent_input")
                )
            }

            // Story Details
            OutlinedTextField(
                value = story,
                onValueChange = {
                    story = it
                    // Reset toxicity matches on active typing
                    viewModel.clearToxicityText()
                },
                label = { Text("What is the drama?") },
                placeholder = { Text("Describe the situation with all juicy moral details, but keep real names, addresses, and extreme target harassment words out of it...") },
                minLines = 5,
                maxLines = 10,
                colors = textFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_story_input")
            )

            // AI TOXICITY INTERFACE
            AnimatedVisibility(visible = checkingToxicity) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CourtroomGold.copy(alpha = 0.1f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = CourtroomGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI Toxicity Filter checking story and removing insults...",
                        fontSize = 11.sp,
                        color = CourtroomGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LaunchedEffect(toxicityResult) {
                // If the user story has no issues and is checked clean, we are good!
            }

            toxicityResult?.let { result ->
                Card(
                    colors = CardColors(
                        containerColor = if (result.isToxic) AngerRed.copy(alpha = 0.12f) else EmpathyGreen.copy(alpha = 0.12f),
                        contentColor = PureIce,
                        disabledContainerColor = DeepCharcoal,
                        disabledContentColor = TextGray
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (result.isToxic) AngerRed.copy(alpha = 0.4f) else EmpathyGreen.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result.isToxic) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = "Toxicity Rating",
                                tint = if (result.isToxic) AngerRed else EmpathyGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (result.isToxic) "Toxicity Shield Flagged Content" else "Passed Toxicity Filter!",
                                fontWeight = FontWeight.Bold,
                                color = if (result.isToxic) AngerRed else EmpathyGreen,
                                fontSize = 13.sp
                            )
                        }

                        if (result.isToxic) {
                            Text(
                                text = "Issue: ${result.reason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PureIce.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚖️ AI Gavel Sugggests Calmer Rewrite:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = CourtroomGold
                            )
                            Text(
                                text = result.suggestedRewrite,
                                style = MaterialTheme.typography.bodySmall,
                                color = PureIce
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    story = result.suggestedRewrite
                                    viewModel.clearToxicityText()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmpathyGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Apply AI Suggested Rewrite", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Your tone is constructive. It matches the platform rules of healthy moral commentary without hate or cruelty.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PureIce.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // ACTION BUTTONS
            Spacer(modifier = Modifier.height(12.dp))

            if (triggerEmptyStateError) {
                Text(
                    text = "Please write a Title and a Story before running tests or filing complaints!",
                    color = AngerRed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Button: Toxicity precheck
                OutlinedButton(
                    onClick = {
                        if (story.isBlank()) {
                            triggerEmptyStateError = true
                        } else {
                            triggerEmptyStateError = false
                            viewModel.performToxicityAnalysis(story)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CourtroomGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("submit_precheck_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield Check Icon",
                        tint = CourtroomGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Toxicity Run", color = CourtroomGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Primary Button: Post
                Button(
                    onClick = {
                        if (title.isBlank() || story.isBlank()) {
                            triggerEmptyStateError = true
                        } else {
                            triggerEmptyStateError = false
                            // Submit to DB!
                            viewModel.postCaseDirect(
                                title = title,
                                story = story,
                                category = selectedCategory,
                                plaintiff = plaintiffLabel,
                                defendant = defendantLabel
                            )
                            // Award user +15 bonus for posting!
                            viewModel.addGavelPoints(20)
                            onSuccessSubmitted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CourtroomGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("submit_post_btn")
                ) {
                    Text(
                        text = if (isSubmitting) "Filing Case..." else "File Complaint (+20 pts)",
                        color = MidnightSlate,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CourtroomGold,
    unfocusedBorderColor = LightSlate,
    focusedLabelColor = CourtroomGold,
    unfocusedLabelColor = TextGray,
    focusedTextColor = PureIce,
    unfocusedTextColor = PureIce,
    focusedContainerColor = DeepCharcoal,
    unfocusedContainerColor = DeepCharcoal
)
