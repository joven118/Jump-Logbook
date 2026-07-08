package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.database.entities.JumpLogEntity

actual object ExternalShareManager {
    actual fun shareJumpExternally(jump: JumpLogEntity) {
        // iOS implementation would use UIActivityViewController
        println("iOS share requested for Jump #${jump.jumpNumber}")
    }
}
