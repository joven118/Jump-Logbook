package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.database.entities.JumpLogEntity
import com.V2Skydivejump.app.database.entities.UserEntity
import com.V2Skydivejump.app.database.entities.DzWaiverEntity

class DesktopPdfManager : PdfManager {
    override fun generateJumpPdf(user: UserEntity, jump: JumpLogEntity, ratings: String) {
        // Desktop implementation (e.g., using iText or just printing to console for now)
        println("Generating PDF for Jump #${jump.jumpNumber} for ${user.name} on Desktop")
    }

    override fun generateWaiverPdf(dzo: UserEntity, waiver: DzWaiverEntity) {
        println("Generating PDF for Waiver ${waiver.title} for ${dzo.dzName} on Desktop")
    }

    override fun printWaiverPdf(dzo: UserEntity, waiver: DzWaiverEntity) {
        println("Printing PDF for Waiver ${waiver.title} for ${dzo.dzName} on Desktop")
    }
}

actual fun getPdfManager(): PdfManager = DesktopPdfManager()
