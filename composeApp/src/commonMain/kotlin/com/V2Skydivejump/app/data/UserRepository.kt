package com.V2Skydivejump.app.data

import com.V2Skydivejump.app.database.entities.*
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import com.V2Skydivejump.app.utils.MediaUploadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

sealed class SessionStatus {
    object Authenticated : SessionStatus()
    object Unauthenticated : SessionStatus()
    object Loading : SessionStatus()
}

class UserRepository(private val scope: CoroutineScope) {
    private val auth: Auth = SupabaseManager.client.auth
    private val db: Postgrest = SupabaseManager.client.postgrest
    private val storage: Storage = SupabaseManager.client.storage

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _privacySettings = MutableStateFlow<UserPrivacySettingsEntity?>(null)
    val privacySettings: StateFlow<UserPrivacySettingsEntity?> = _privacySettings.asStateFlow()

    private val _sessionStatus = MutableStateFlow<SessionStatus>(SessionStatus.Loading)
    val sessionStatus: StateFlow<SessionStatus> = _sessionStatus.asStateFlow()

    private val _userMemberships = MutableStateFlow<List<DzStaffMembershipEntity>>(emptyList())
    val userMemberships: StateFlow<List<DzStaffMembershipEntity>> = _userMemberships.asStateFlow()

    init {
        scope.launch {
            auth.sessionStatus.collect { status ->
                if (status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
                    _sessionStatus.value = SessionStatus.Authenticated
                    fetchProfile()
                    fetchUserMemberships()
                } else {
                    _sessionStatus.value = SessionStatus.Unauthenticated
                    _currentUser.value = null
                    _userMemberships.value = emptyList()
                }
            }
        }
    }

    suspend fun fetchUserMemberships() {
        val user = auth.currentUserOrNull() ?: return
        try {
            val memberships = db.from("dz_staff_memberships").select {
                filter { eq("user_id", user.id); eq("is_active", true) }
            }.decodeList<DzStaffMembershipEntity>()
            _userMemberships.value = memberships
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun signUp(email: String, password: String, name: String, role: String?, license: String?) {
        auth.signUpWith(io.github.jan.supabase.auth.providers.builtin.Email) {
            this.email = email
            this.password = password
        }
        val user = auth.currentUserOrNull()
        if (user != null) {
            createInitialProfile(user.id, name, role ?: "JUMPER", email, license)
        }
    }

    private suspend fun createInitialProfile(userId: String, name: String, role: String, email: String, licenseOrDzLocation: String?) {
        val isDzo = role == "DZ_OPERATOR"
        val profile = UserEntity(
            userId = userId,
            name = name,
            role = role,
            email = email,
            licenseNumber = if (isDzo) "" else licenseOrDzLocation.orEmpty(),
            dzName = if (isDzo) name else null,
            dzLocation = if (isDzo) licenseOrDzLocation else null,
            dzEmail = if (isDzo) email else null,
            walletBalance = 0.0
        )
        db.from("users").upsert(profile)
        
        // Sync to Directory if DZO
        if (isDzo) {
            val dz = DropzoneEntity(
                id = userId,
                ownerId = userId,
                dzName = name,
                location = licenseOrDzLocation?.takeIf { it.isNotBlank() } ?: "New Registration",
                city = "",
                province = "",
                country = "N/A",
                facilities = "Awaiting Setup",
                aircraftFleet = "Awaiting Setup",
                businessHour = "Not Set",
                website = "",
                contactNumbers = "",
                emailAddress = email
            )
            db.from("dropzones").upsert(dz)
        }

        _currentUser.value = profile
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(io.github.jan.supabase.auth.providers.builtin.Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun fetchProfile() {
        val user = auth.currentUserOrNull() ?: return
        try {
            val profile = db.from("users").select(Columns.ALL) {
                filter { eq("user_id", user.id) }
            }.decodeSingleOrNull<UserEntity>()
            
            println("UserRepository DIAGNOSTIC: Profile fetched. Role=${profile?.role}")
            
            _currentUser.value = profile
            fetchPrivacySettings()
        } catch (e: Exception) {
            println("UserRepository ERROR during profile fetch: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun updateProfile(user: UserEntity) {
        try {
            val authUserId = auth.currentUserOrNull()?.id ?: user.userId
            val persistedUser = user.copy(userId = authUserId)
            db.from("users").upsert(persistedUser)
            
            // Sync to Directory if DZO
            if (persistedUser.role == "DZ_OPERATOR") {
                val dz = DropzoneEntity(
                    id = persistedUser.userId,
                    ownerId = persistedUser.userId,
                    dzName = persistedUser.dzName ?: persistedUser.name,
                    location = persistedUser.dzStreet ?: persistedUser.dzLocation ?: "Set in Profile",
                    city = persistedUser.dzCity ?: "",
                    province = persistedUser.dzProvince ?: "",
                    country = persistedUser.dzCountry ?: persistedUser.country ?: "N/A",
                    facilities = "Integrated DZ", 
                    aircraftFleet = "Active Fleet", 
                    businessHour = "${persistedUser.operatingDays ?: "Mon-Sun"}: ${persistedUser.operatingHours ?: "08:00-18:00"}",
                    website = persistedUser.dzWebsite ?: "",
                    contactNumbers = persistedUser.dzMobileNumber ?: persistedUser.mobileNumber ?: "",
                    emailAddress = persistedUser.dzEmail ?: persistedUser.email ?: ""
                )
                println("UserRepository: Syncing professional details to Directory for ${persistedUser.dzName}")
                db.from("dropzones").upsert(dz)
            }

            // Optimization: Only update flow if data actually changed to avoid UI jitters
            if (_currentUser.value != persistedUser) {
                _currentUser.value = persistedUser
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun uploadProfileMedia(type: String, localUri: String): String {
        val user = auth.currentUserOrNull() ?: run {
            println("Media Upload Error: No authenticated user found")
            throw IllegalStateException("No authenticated user found")
        }

        try {
            val bucketName = if (type == "inventory" || type == "facility") "inventory-media" else "profile-media"
            val uploaded = MediaUploadService.uploadLocalUri(
                localUri = localUri,
                bucketName = bucketName,
                entityType = type,
                entityId = user.id,
                mediaKind = "image"
            )
            val publicUrl = uploaded.publicUrl
            
            if (type == "profile" || type == "background") {
                val updatedUser = if (type == "profile") {
                    _currentUser.value?.copy(profilePictureUrl = publicUrl)
                } else {
                    _currentUser.value?.copy(backgroundPictureUrl = publicUrl)
                }
                
                if (updatedUser != null) {
                    println("Media Upload: Updating user profile with new URL...")
                    updateProfile(updatedUser)
                    println("Media Upload: Profile updated successfully.")
                }
            }
            
            println("Media Upload SUCCESS: Successfully uploaded and mapped $type")
            return publicUrl
        } catch (e: Exception) {
            println("Media Upload GENERAL ERROR: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchPrivacySettings() {
        val user = auth.currentUserOrNull() ?: return
        try {
            val settings = db.from("user_privacy_settings").select(Columns.ALL) {
                filter { eq("user_id", user.id) }
            }.decodeSingleOrNull<UserPrivacySettingsEntity>()
            _privacySettings.value = settings ?: UserPrivacySettingsEntity(user.id)
        } catch (e: Exception) {
            _privacySettings.value = UserPrivacySettingsEntity(user.id)
        }
    }

    suspend fun updatePrivacySettings(settings: UserPrivacySettingsEntity) {
        db.from("user_privacy_settings").upsert(settings)
        _privacySettings.value = settings
    }

    suspend fun toggleFollow(targetUserId: String) {
        val user = auth.currentUserOrNull() ?: return
        try {
            val existing = db.from("user_follows").select {
                filter {
                    eq("follower_id", user.id)
                    eq("followed_id", targetUserId)
                }
            }.decodeList<UserFollowEntity>()

            if (existing.isEmpty()) {
                db.from("user_follows").insert(UserFollowEntity(user.id, targetUserId, com.V2Skydivejump.app.TimeUtils.nowEpochMillis()))
            } else {
                db.from("user_follows").delete {
                    filter {
                        eq("follower_id", user.id)
                        eq("followed_id", targetUserId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getStaffForDz(dzId: String): List<UserEntity> {
        return try {
            val memberships = db.from("dz_staff_memberships").select {
                filter { eq("dz_id", dzId) }
            }.decodeList<DzStaffMembershipEntity>()
            
            val staffIds = memberships.map { it.userId }
            if (staffIds.isEmpty()) return emptyList()
            
            db.from("users").select {
                filter {
                    // postgrest.in_("user_id", staffIds)
                }
            }.decodeList<UserEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addDzStaffMembership(membership: DzStaffMembershipEntity) {
        db.from("dz_staff_memberships").upsert(membership)
    }

    suspend fun addDzFacility(facility: DzFacilityEntity) {
        db.from("dz_facilities").upsert(facility)
    }

    suspend fun updateDzFacility(facility: DzFacilityEntity) {
        db.from("dz_facilities").update(facility) {
            filter {
                eq("id", facility.id)
                eq("dz_id", facility.dzId)
            }
        }
    }

    suspend fun deleteDzFacility(id: Long) {
        db.from("dz_facilities").delete {
            filter { eq("id", id) }
        }
    }

    suspend fun addDzInventory(item: DzInventoryEntity) {
        db.from("dz_inventory").upsert(item)
    }

    suspend fun updateDzInventory(item: DzInventoryEntity) {
        db.from("dz_inventory").update(item) {
            filter {
                eq("id", item.id)
                eq("dz_id", item.dzId)
            }
        }
    }

    suspend fun deleteDzInventory(id: Long) {
        db.from("dz_inventory").delete {
            filter { eq("id", id) }
        }
    }

    suspend fun addDzRating(rating: DzRatingEntity) {
        db.from("dz_ratings").upsert(rating)
    }

    suspend fun upsertWaiver(waiver: DzWaiverEntity) {
        try {
            db.from("dz_waivers").upsert(waiver)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun signWaiver(signature: JumperWaiverSignatureEntity) {
        try {
            db.from("jumper_waiver_signatures").insert(signature)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun isFollowing(targetUserId: String): Boolean {
        val user = auth.currentUserOrNull() ?: return false
        return try {
            val res = db.from("user_follows").select {
                filter {
                    eq("follower_id", user.id)
                    eq("followed_id", targetUserId)
                }
            }.decodeList<UserFollowEntity>()
            res.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllRegisteredUsers(): List<UserEntity> {
        return try {
            db.from("users").select(Columns.ALL).decodeList<UserEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllDzInventory(): List<DzInventoryEntity> {
        return try {
            db.from("dz_inventory").select(Columns.ALL).decodeList<DzInventoryEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLedger(): List<LedgerEntity> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return try {
            db.from("ledger").select {
                filter { eq("user_id", user.id) }
            }.decodeList<LedgerEntity>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getUserLoyalty(): UserLoyaltyEntity? {
        val user = auth.currentUserOrNull() ?: return null
        return try {
            db.from("user_loyalty").select {
                filter { eq("user_id", user.id) }
            }.decodeSingleOrNull<UserLoyaltyEntity>()
        } catch (e: Exception) { null }
    }

    suspend fun getUserFollows(): List<UserFollowEntity> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return try {
            db.from("user_follows").select {
                filter {
                    or {
                        eq("follower_id", user.id)
                        eq("followed_id", user.id)
                    }
                }
            }.decodeList<UserFollowEntity>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getStudentSkills(): List<StudentSkillEntity> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return try {
            db.from("student_skills").select {
                filter { eq("user_id", user.id) }
            }.decodeList<StudentSkillEntity>()
        } catch (e: Exception) { emptyList() }
    }
}
