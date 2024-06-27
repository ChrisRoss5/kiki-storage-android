package dev.k1k1.kikistorage.firebase

import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import dev.k1k1.kikistorage.BuildConfig

/*
ANDROID AI
https://developer.android.com/ai/generativeai

Google AI Client SDK:
https://ai.google.dev/gemini-api/docs/get-started/tutorial?lang=android#kotlin

Vertex AI for Firebase:
https://firebase.google.com/docs/vertex-ai/get-started?platform=android&hl=en&authuser=0#add-sdk

IMPORTANT LIMITS:
https://firebase.google.com/docs/vertex-ai/gemini-models#detailed-info
*/

object Gemini {
    /*val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.geminiApiKey
    )*/

    const val MODEL_NAME = "gemini-1.5-flash-001"

    val generativeModel = Firebase.vertexAI.generativeModel(MODEL_NAME)
}