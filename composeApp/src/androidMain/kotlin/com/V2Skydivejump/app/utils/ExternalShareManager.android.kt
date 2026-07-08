package com.V2Skydivejump.app.utils

import android.content.Context
import android.content.Intent
import com.V2Skydivejump.app.database.entities.JumpLogEntity

actual object ExternalShareManager {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context
    }

    actual fun shareJumpExternally(jump: JumpLogEntity) {
        val shareText = """
            🪂 Just logged Jump #${jump.jumpNumber} on Skydive Jump!
            📍 Location: ${jump.dzName?.ifBlank { jump.location } ?: jump.location}
            ✈️ Aircraft: ${jump.aircraftType ?: "N/A"}
            ⛰️ Exit Alt: ${jump.exitAltitudeAgl}ft
            ⏱️ Freefall: ${jump.freefallTimeSeconds}s
            
            #skydivejump #skydiving #blueskies
        """.trimIndent()
        shareText(shareText)
    }

    actual fun shareText(text: String) {
        val context = appContext ?: return
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share via:")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
