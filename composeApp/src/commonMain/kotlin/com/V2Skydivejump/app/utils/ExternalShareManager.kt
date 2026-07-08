package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.database.entities.JumpLogEntity

expect object ExternalShareManager {
    fun shareJumpExternally(jump: JumpLogEntity)
    fun shareText(text: String)
}
