package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val story: String,
    val category: String,
    val plaintiffLabel: String,
    val defendantLabel: String,
    val votesPlaintiff: Int,
    val votesDefendant: Int,
    val votesBothWrong: Int,
    val votesNoneWrong: Int,
    val userVotedOption: Int = 0, // 0 = not voted, 1 = Plaintiff, 2 = Defendant, 3 = Both Wrong, 4 = None Wrong
    val aiJudgeVerdict: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val gavelPoints: Int = 100,
    val streakCount: Int = 0,
    val lastVotedTimestamp: Long = 0L
)
