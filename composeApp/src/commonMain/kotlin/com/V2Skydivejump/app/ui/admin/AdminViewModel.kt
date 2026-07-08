package com.V2Skydivejump.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.entities.*
import com.V2Skydivejump.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUiState(
    val unverifiedUsers: List<UserEntity> = emptyList(),
    val pendingBadges: List<UserBadgeEntity> = emptyList(),
    val allUsers: List<UserEntity> = emptyList(),
    val systemMetrics: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val appConfig: AppConfig = AppConfig()
)

data class AppConfig(
    val primaryColor: String = "#1976D2",
    val welcomeText: String = "Welcome to Jump Logbook",
    val convenienceFeeType: String = "FIXED", // "FIXED" or "PERCENTAGE"
    val convenienceFeeRate: Double = 1.0 // Default $1.00
)

class AdminViewModel(
    private val database: AppDatabase,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val users = adminRepository.getUnverifiedUsers()
                val badges = adminRepository.getPendingBadges()
                val allUsers = adminRepository.getAllUsers()
                val metrics = adminRepository.getSystemMetrics()
                _uiState.update { it.copy(
                    unverifiedUsers = users,
                    pendingBadges = badges,
                    allUsers = allUsers,
                    systemMetrics = metrics,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun verifyUser(userId: String) {
        viewModelScope.launch {
            try {
                adminRepository.verifyUser(userId)
                refreshData()
            } catch (e: Exception) { }
        }
    }

    fun verifyBadge(badgeId: Long) {
        viewModelScope.launch {
            try {
                adminRepository.verifyBadge(badgeId)
                refreshData()
            } catch (e: Exception) { }
        }
    }

    fun addAdmin(email: String) {
        viewModelScope.launch {
            try {
                adminRepository.promoteToAdmin(email)
                refreshData()
            } catch (e: Exception) { }
        }
    }

    fun updateConfig(config: AppConfig) {
        _uiState.update { it.copy(appConfig = config) }
    }
}
