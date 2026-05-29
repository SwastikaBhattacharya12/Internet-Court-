package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Checks if the API key is active and not default placeholder.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && key != "null"
    }

    /**
     * Sends a request to Gemini 3.5 Flash using direct OkHttp POST.
     */
    private suspend fun callGemini(prompt: String, systemInstruction: String? = null): String? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API Key is not configured. Falling back to offline engine.")
            return@withContext null
        }

        try {
            val key = BuildConfig.GEMINI_API_KEY
            val url = "$BASE_URL?key=$key"

            // Construct JSON request body manually
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObject = JSONObject()
            val partsArray = JSONArray()
            val partObject = JSONObject()
            
            partObject.put("text", prompt)
            partsArray.put(partObject)
            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)
            requestJson.put("contents", contentsArray)

            // Add system instruction if available
            if (systemInstruction != null) {
                val systemInstructionObj = JSONObject()
                val systemPartsArray = JSONArray()
                val systemPartObj = JSONObject()
                systemPartObj.put("text", systemInstruction)
                systemPartsArray.put(systemPartObj)
                systemInstructionObj.put("parts", systemPartsArray)
                requestJson.put("systemInstruction", systemInstructionObj)
            }

            // Fine-tune configuration
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            requestJson.put("generationConfig", generationConfig)

            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response from Gemini: ${response.code} ${response.message}")
                    return@withContext null
                }
                
                val responseBody = response.body?.string() ?: return@withContext null
                val rootJson = JSONObject(responseBody)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Gemini", e)
            return@withContext null
        }
    }

    /**
     * Witty judicial verdict prompt setup.
     */
    suspend fun summonJudge(
        title: String,
        story: String,
        plaintiff: String,
        defendant: String
    ): String = withContext(Dispatchers.IO) {
        val systemInstruction = """
            You are 'Judge Gavel', an elite, clever, and highly respected AI Judge of the "Internet Court". 
            Your role is to analyze interpersonal disputes submitted anonymously. 
            Your tone MUST be witty, sharp, humorous, and entertaining (like a legendary TV courtroom judge), but it MUST remain deeply empathetic, mature, constructive, and emotionally intelligent. 
            Do NOT encourage revenge, toxicity, or pettiness. You want the parties to understand each other and learn.
            Never be toxic, insulting, or preachy. Be brilliantly observant.
            
            Structure your verdict in 4 rapid sections:
            1. ⚖️ THE CHARGE (Summarize the clash in a witty single line)
            2. 🧐 JUDGE GAVEL'S FINDINGS (Witty but fair analysis of what both sides got wrong or right)
            3. 👨‍⚖️ THE VERDICT (Explicitly state either: Plaintiff in the right, Defendant in the right, Both in the wrong, or A simple miscommunication)
            4. 🕊️ THE SENTENCE OF EMPATHY (A clever, highly-functional, empathetic task or communication homework for the parties to make up and de-escalate)
        """.trimIndent()

        val prompt = """
            Case Title: "$title"
            Plaintiff Label (Author): $plaintiff
            Defendant Label (Opposing): $defendant
            
            The Case Details:
            $story
        """.trimIndent()

        val result = callGemini(prompt, systemInstruction)
        if (result != null) {
            return@withContext result
        }

        // Return beautiful offline mock response that mirrors the exact structure based on title!
        return@withContext generateLocalVerdictFallback(title, plaintiff, defendant)
    }

    /**
     * Toxicity check and re-write advice helper.
     */
    suspend fun checkToxicityAndRewrite(story: String): ToxicityResult = withContext(Dispatchers.IO) {
        val systemInstruction = """
            You are a sensitive, highly intelligent "AI Toxicity Filter & Empathy Catalyst" for the Internet Court.
            Your job is to prevent toxic outrage, personal insults, slur usage, harassment, and toxic doxxing, while preserving the juicy, humorous, engaging drama of user-submitted stories.
            
            Evaluate the user story. Check if it contains toxic elements (direct slurs, verbal bullying, threats, real full names/addresses, absolute hatred).
            
            Format your response STRICTLY as a JSON string with these exact keys:
            - isToxic: boolean (true if highly toxic, abusive, or uses heavy swears/insults)
            - toxicityLevel: string ("LOW", "MEDIUM", such as swearing, or "HIGH", such as severe slurs/abuse)
            - reason: string (brief explanation of why it is flagged, or "Looks good!" if low toxicity)
            - suggestedRewrite: string (A beautifully polished, calm, empathetic, yet highly engaging and funny rewrite of the drama. DO NOT lose the core conflict, but remove the insults, profanity, and pure rage. Translate raw insults like "my idiot garbage roommate" to constructive descriptions like "my roommate who leaves messes and tests my patience".)
        """.trimIndent()

        val resultText = callGemini("Story to analyze:\n$story", systemInstruction)
        if (resultText != null) {
            try {
                // Find JSON part in case Gemini wraps it in markdown (```json ... ```)
                var jsonStr = resultText.trim()
                if (jsonStr.startsWith("```")) {
                    jsonStr = jsonStr.substringAfter("```json")
                    if (jsonStr.contains("```")) {
                        jsonStr = jsonStr.substringBeforeLast("```")
                    }
                }
                jsonStr = jsonStr.trim()

                val json = JSONObject(jsonStr)
                return@withContext ToxicityResult(
                    isToxic = json.optBoolean("isToxic", false),
                    toxicityLevel = json.optString("toxicityLevel", "LOW"),
                    reason = json.optString("reason", "None"),
                    suggestedRewrite = json.optString("suggestedRewrite", story)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse json from AI, returning default", e)
            }
        }

        // Return offline rule check
        return@withContext scanLocalToxicityFallback(story)
    }

    private fun generateLocalVerdictFallback(title: String, plaintiff: String, defendant: String): String {
        val lowercaseTitle = title.lowercase()
        return when {
            lowercaseTitle.contains("smoothie") || lowercaseTitle.contains("roommate") -> """
                ⚖️ **THE CHARGE:** Aggravated Kitchen Warfare and Unauthorized bed placement of biohazards.
                
                🧐 **JUDGE GAVEL'S FINDINGS:** 
                - **$plaintiff's Side:** While making a loud midnight protein shake is borderline acoustic assault, let's look at the facts. You were operating in a dish-infested wasteland. Taping moldy plates to a bed is a drastic visual protest.
                - **$defendant's Side:** Leaving dishes to develop their own eco-systems in a shared sink is a civil offense. However, sleep deprivation causes deep relationship fractures.
                
                👨‍⚖️ **THE VERDICT:** **BOTH SIDES ARE IN THE WRONG.** Your roommate holds the crown for passive-aggressive neglect, but your midnight blender symphony is a crime of psychological noise.
                
                🕊️ **THE SENTENCE OF EMPATHY:** Both parties are ordered to purchase a set of 100% compostable paper plates for the next 7 days, and clean up together while listening to peaceful acoustic jazz. No blenders allowed after 10 PM.
            """.trimIndent()
            
            lowercaseTitle.contains("minecraft") || lowercaseTitle.contains("rent") -> """
                ⚖️ **THE CHARGE:** Attempted Sovereign Server Room Tax and Roommate Extortion.
                
                🧐 **JUDGE GAVEL'S FINDINGS:** 
                - **$plaintiff's Side:** You are moving into a partnership, not an internet cafe. Your objection to subsidizing a machine that earns less than a paper route is highly validated.
                - **$defendant's Side:** While hosting a server is a noble tech pursuit, hosting it under a joint roof doesn't mean your partner covers your food bills while you play digital landlord.
                
                👨‍⚖️ **THE VERDICT:** **DEFENDANT ($defendant) IS EXTREMELY IN THE WRONG.** The server should pay for itself before it dictates household economics. 
                
                🕊️ **THE SENTENCE OF EMPATHY:** The Defendant is ordered to set up a Minecraft-free date night paid with the server donations, and relocate the servers to a closet with a strict power-meter calculator plug so you both pay exact usage bills.
            """.trimIndent()

            else -> """
                ⚖️ **THE CHARGE:** High-Drama Communication Breakdown.
                
                🧐 **JUDGE GAVEL'S FINDINGS:** 
                - **$plaintiff's Side:** Your frustration is real, and the narrative builds a compelling moral defense. Empathic response was missing, but defense was necessary.
                - **$defendant's Side:** Action was taken based on an assumption without verifying details first. A lack of emotional cooling caused the escalation.
                
                👨‍⚖️ **THE VERDICT:** **A SIMPLE MISCOMMUNICATION.** Neither side is a villain here, just two people executing separate protocols on a shared wifi router.
                
                🕊️ **THE SENTENCE OF EMPATHY:** You are hereby sentenced to a 10-minute conversational cooling-off period. Both sides must speak only in I-statements ("I feel...", "When this happened...") without using the word "You" or rolling your eyes.
            """.trimIndent()
        }
    }

    private fun scanLocalToxicityFallback(story: String): ToxicityResult {
        val toxicWords = listOf("idiot", "disgusting", "garbage", "trash", "hate", "dumb", "stupid", "slur", "fat", "ugly")
        var isToxic = false
        var flaggedWord = ""
        for (word in toxicWords) {
            if (story.lowercase().contains(word)) {
                isToxic = true
                flaggedWord = word
                break
            }
        }

        if (isToxic) {
            val cleaner = story
                .replace("idiot", "highly uncooperative person")
                .replace("disgusting", "very messy")
                .replace("garbage", "poorly maintained")
                .replace("dumb", "ill-advised")
                .replace("stupid", "unhelpful")
                .replace("hate", "get genuinely frustrated with")

            return ToxicityResult(
                isToxic = true,
                toxicityLevel = "LOW",
                reason = "Flagged wording: '$flaggedWord'. We maintain an respectful debate court.",
                suggestedRewrite = cleaner
            )
        }

        return ToxicityResult(
            isToxic = false,
            toxicityLevel = "LOW",
            reason = "Passes local screening.",
            suggestedRewrite = story
        )
    }

    data class ToxicityResult(
        val isToxic: Boolean,
        val toxicityLevel: String,
        val reason: String,
        val suggestedRewrite: String
    )
}
