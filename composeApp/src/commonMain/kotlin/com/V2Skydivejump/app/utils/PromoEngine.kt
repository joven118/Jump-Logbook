package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.database.entities.PromotionEntity
import com.V2Skydivejump.app.database.entities.UserEntity

object PromoEngine {

    fun validatePromo(
        promo: PromotionEntity,
        user: UserEntity,
        currentTime: Long = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
    ): PromoValidationResult {
        if (!promo.isActive) return PromoValidationResult.Invalid("Promotion is no longer active.")
        if (currentTime < promo.startDate) return PromoValidationResult.Invalid("Promotion has not started yet.")
        if (currentTime > promo.endDate) return PromoValidationResult.Invalid("Promotion has expired.")
        if (!promo.isUnlimited && promo.currentRedemptions >= promo.maxRedemptions) {
            return PromoValidationResult.Invalid("Promotion has reached maximum usage limit.")
        }

        // Eligibility Rules
        val isEligible = when (promo.eligibilityRules) {
            "ALL" -> true
            "FIRST_TIME" -> user.baseJumpCount == 0
            "LICENSED" -> user.licenseNumber.isNotBlank()
            "STUDENT" -> user.licenseNumber.isBlank() || user.licenseNumber.lowercase().contains("student")
            "MEMBER" -> user.membershipLevel != "Standard"
            else -> true
        }

        if (!isEligible) return PromoValidationResult.Invalid("You are not eligible for this promotion.")

        return PromoValidationResult.Valid(promo)
    }

    fun calculateDiscount(price: Double, promo: PromotionEntity): Double {
        return when (promo.promoType) {
            "PERCENTAGE" -> price * (promo.value / 100.0)
            "FIXED" -> promo.value
            "FREE_SLOT" -> price // Full price discount
            else -> 0.0
        }
    }

    fun getMembershipDiscount(membershipLevel: String): Double {
        return when (membershipLevel) {
            "Silver" -> 0.05
            "Gold" -> 0.10
            "Platinum" -> 0.15
            else -> 0.0
        }
    }

    fun calculateLoyaltyPoints(amount: Double, activity: String): Int {
        val multiplier = when (activity) {
            "BOOKING" -> 10
            "JUMP_COMPLETE" -> 50
            "AFF_LEVEL" -> 100
            "GEAR_RENTAL" -> 5
            else -> 1
        }
        return (amount / 100.0).toInt() * multiplier
    }
}

sealed class PromoValidationResult {
    data class Valid(val promo: PromotionEntity) : PromoValidationResult()
    data class Invalid(val reason: String) : PromoValidationResult()
}
