package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

class CaseRepository(private val caseDao: CaseDao) {

    val allCases: Flow<List<CaseEntity>> = caseDao.getAllCases()
    val userStats: Flow<UserStatsEntity?> = caseDao.getUserStatsFlow()

    suspend fun getCaseById(id: Int): CaseEntity? {
        return caseDao.getCaseById(id)
    }

    suspend fun insertCase(case: CaseEntity): Long {
        return caseDao.insertCase(case)
    }

    suspend fun updateCase(case: CaseEntity) {
        caseDao.updateCase(case)
    }

    suspend fun voteOnCase(caseId: Int, option: Int): Boolean {
        val case = caseDao.getCaseById(caseId) ?: return false
        if (case.userVotedOption != 0) return false // Already voted

        // Update vote counts
        val updatedCase = when (option) {
            1 -> case.copy(votesPlaintiff = case.votesPlaintiff + 1, userVotedOption = 1)
            2 -> case.copy(votesDefendant = case.votesDefendant + 1, userVotedOption = 2)
            3 -> case.copy(votesBothWrong = case.votesBothWrong + 1, userVotedOption = 3)
            4 -> case.copy(votesNoneWrong = case.votesNoneWrong + 1, userVotedOption = 4)
            else -> return false
        }
        caseDao.updateCase(updatedCase)

        // Game/Points adjustment
        val currentStats = caseDao.getUserStats() ?: UserStatsEntity()
        val earnedPoints = 15
        
        // Calculate streak
        val currentTime = System.currentTimeMillis()
        val lastVoted = currentStats.lastVotedTimestamp
        val diffInMs = currentTime - lastVoted
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs)

        val newStreak = when {
            lastVoted == 0L -> 1 // First vote ever
            diffInDays <= 1 -> {
                // Voted today or yesterday
                if (diffInDays == 1L) {
                    currentStats.streakCount + 1
                } else {
                    currentStats.streakCount // Checked in today already, retain current streak
                }
            }
            else -> 1 // Broken streak, reset to 1
        }

        val updatedStats = currentStats.copy(
            gavelPoints = currentStats.gavelPoints + earnedPoints,
            streakCount = if (newStreak == 0) 1 else newStreak,
            lastVotedTimestamp = currentTime
        )
        caseDao.insertOrUpdateUserStats(updatedStats)
        return true
    }

    suspend fun claimDailyReward(rewardAmount: Int): Boolean {
        val stats = caseDao.getUserStats() ?: UserStatsEntity()
        caseDao.insertOrUpdateUserStats(stats.copy(gavelPoints = stats.gavelPoints + rewardAmount))
        return true
    }

    suspend fun addPoints(amount: Int) {
         val stats = caseDao.getUserStats() ?: UserStatsEntity()
         caseDao.insertOrUpdateUserStats(stats.copy(gavelPoints = stats.gavelPoints + amount))
    }

    suspend fun clearAllVotes() {
        val cases = caseDao.getAllCases().firstOrNull() ?: return
        for (case in cases) {
            caseDao.updateCase(case.copy(userVotedOption = 0, aiJudgeVerdict = null))
        }
        val stats = caseDao.getUserStats() ?: UserStatsEntity()
        caseDao.insertOrUpdateUserStats(stats.copy(streakCount = 3, lastVotedTimestamp = System.currentTimeMillis() - 86400000))
    }

    suspend fun updateAiVerdict(caseId: Int, verdict: String) {
        val case = caseDao.getCaseById(caseId) ?: return
        caseDao.updateCase(case.copy(aiJudgeVerdict = verdict))
    }

    suspend fun checkAndPrepopulate() {
        val cases = caseDao.getAllCases().firstOrNull() ?: emptyList()
        if (cases.isEmpty()) {
            val defaultList = listOf(
                CaseEntity(
                    title = "The Roommate's Midnight Smoothie of Vengeance",
                    story = "My roommate leaves dirty dishes in our shared sink for literal days until they mold. After asking nicely five times, I took all his moldy cereal bowls, placed them neatly on his bed, and proceeded to make a spinach protein smoothie in our loudest blender right next to his bedroom door at midnight.\n\nHe is absolutely furious and says putting the moldy dishes on his bed is a toxic biohazard and making noise is a lease violation. Am I the bad roommate here?",
                    category = "Roommates",
                    plaintiffLabel = "Midnight Smoothie Maker",
                    defendantLabel = "Dirty Dish Roommate",
                    votesPlaintiff = 142,
                    votesDefendant = 89,
                    votesBothWrong = 412,
                    votesNoneWrong = 12,
                    userVotedOption = 0
                ),
                CaseEntity(
                    title = "Boyfriend Wants Me to Pay Rent for His Minecraft Server Room",
                    story = "We are moving into an apartment together. He has proposed setting up a secondary 'dedicated hosting server room' for his Minecraft community. He claims that because his servers consume 30% of our utility power and occupy our only spare closet, I should pay 40% of the rent while he pays 60%, BUT he also expects me to cover all of our joint food bills because his servers 'reinvest core bandwidth capital' back into his business.\n\nHis server earns about $200 a month in community donations, whereas rent is $2,200. He says I don’t support his career as an indie host administrator. Is this fair?",
                    category = "Romance",
                    plaintiffLabel = "Frustrated Girlfriend",
                    defendantLabel = "Minecraft Administrator",
                    votesPlaintiff = 1290,
                    votesDefendant = 42,
                    votesBothWrong = 95,
                    votesNoneWrong = 10,
                    userVotedOption = 0
                ),
                CaseEntity(
                    title = "Malicious Compliance With Public Slack Status",
                    story = "My manager recently enforced a strict rule that everyone on our team must respond to Slack messages within 5 minutes, or we get written up. Yesterday, we had an unexpected live building emergency evacuation drill. I posted a broad 'Leaving building due to fire alarm' status in general. \n\nFive minutes later, my manager messaged me: 'Hey, did you finish that spreadsheet?' I didn't reply because I was in the parking lot. He posted a public message in general: 'I guess some people are too busy for our clients during work hours.' In response, I posted a hilarious selfie of myself wearing a neon hardhat next to a firetruck, with 200 colleagues in the background, titled: 'Live stream of my spreadsheet thinking fire!' Now HR wants to speak to both of us. Was my photo unprofessionally petty?",
                    category = "Workplace",
                    plaintiffLabel = "Hardhat Employee",
                    defendantLabel = "5-Minute Manager",
                    votesPlaintiff = 823,
                    votesDefendant = 104,
                    votesBothWrong = 12,
                    votesNoneWrong = 300,
                    userVotedOption = 0
                ),
                CaseEntity(
                    title = "My Mom Charges Me $5 Per Load of Laundry Since I Graduated",
                    story = "I recently graduated from university and moved back home to save up for rent. I pay her $300 a month to live in my childhood bedroom. However, last weekend she printed out a 'Price List' and taped it to the basement washing machine. It says: 'Wash: $2.50. Dry: $2.50. Fabric Softener premium: $1.00.'\n\nI told her she was nickel-and-diming her own child. She responded that in the real world, laundromats are even more expensive and she's teaching me financial adulthood. Am I being entitled for wanting to wash my socks for free at my parents' house?",
                    category = "Family",
                    plaintiffLabel = "Fresh Graduate",
                    defendantLabel = "Laundromat Mother",
                    votesPlaintiff = 345,
                    votesDefendant = 801,
                    votesBothWrong = 45,
                    votesNoneWrong = 18,
                    userVotedOption = 0
                ),
                CaseEntity(
                    title = "Catan Betrayal of Romantic Proportions",
                    story = "During our weekend board game night, my boyfriend of two years traded all his wheat to our common friend just to block me from building a settlement in Settlers of Catan. This specific block allowed our friend to win the entire game. My boyfriend laughed it off saying 'it's just a game, babe' and 'table talk shouldn't carry into real life'. \n\nBut here's the kicker: we had a bet that the loser had to wash dishes for a week. Now he expects me to do the dishes even though he practically orchestrated my defeat through deliberate collusion. I refused and went to sleep on the guest couch. He thinks I have anger management issues. Am I wrong for taking Catan too seriously?",
                    category = "Romance",
                    plaintiffLabel = "Catan Couch-Sleeper",
                    defendantLabel = "Catan Backstabber",
                    votesPlaintiff = 412,
                    votesDefendant = 340,
                    votesBothWrong = 52,
                    votesNoneWrong = 128,
                    userVotedOption = 0
                )
            )
            caseDao.insertCases(defaultList)
        }

        val stats = caseDao.getUserStats()
        if (stats == null) {
            caseDao.insertOrUpdateUserStats(
                UserStatsEntity(
                    id = 1,
                    gavelPoints = 150,
                    // Give them a starting 3-day streak to highlight the Duolingo aesthetic
                    streakCount = 3,
                    lastVotedTimestamp = System.currentTimeMillis() - 86400000 // Yesterday
                )
            )
        }
    }
}
