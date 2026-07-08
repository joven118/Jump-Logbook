package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.database.entities.JumpLogEntity

actual object ExternalShareManager {
    actual fun shareJumpExternally(jump: JumpLogEntity) {
        println("Sharing jump #${jump.jumpNumber} on Desktop")
    }

    actual fun shareText(text: String) {
        println("Sharing text on Desktop: $text")
    }
}
