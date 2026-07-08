package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.database.entities.JumpLogEntity
import com.V2Skydivejump.app.database.entities.UserEntity
import com.V2Skydivejump.app.database.entities.DzWaiverEntity

interface PdfManager {
    fun generateJumpPdf(user: UserEntity, jump: JumpLogEntity, ratings: String)
    fun generateWaiverPdf(dzo: UserEntity, waiver: DzWaiverEntity)
    fun printWaiverPdf(dzo: UserEntity, waiver: DzWaiverEntity)
}

expect fun getPdfManager(): PdfManager
