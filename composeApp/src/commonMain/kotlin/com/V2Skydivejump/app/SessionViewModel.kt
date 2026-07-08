package com.V2Skydivejump.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import com.V2Skydivejump.app.data.UserRepository
import com.V2Skydivejump.app.database.entities.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val settings = Settings()

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = userRepository.currentUser
    val sessionStatus: StateFlow<com.V2Skydivejump.app.data.SessionStatus> = userRepository.sessionStatus
    val memberships: StateFlow<List<com.V2Skydivejump.app.database.entities.DzStaffMembershipEntity>> = userRepository.userMemberships

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                println("DIAGNOSTIC: SessionViewModel - CurrentUser updated: ${user?.userId}, raw role: ${user?.role}")
                if (user != null) {
                    _userRole.value = try {
                        val mappedRole = UserRole.valueOf(user.role.uppercase().replace(" ", "_"))
                        println("DIAGNOSTIC: SessionViewModel - Mapped role: $mappedRole")
                        mappedRole
                    } catch (e: Exception) {
                        println("DIAGNOSTIC ERROR: SessionViewModel - Role mapping failed for ${user.role}: ${e.message}")
                        UserRole.JUMPER
                    }
                } else {
                    _userRole.value = null
                }
            }
        }
        viewModelScope.launch {
            sessionStatus.collect { status ->
                println("SessionViewModel: SessionStatus updated: $status")
            }
        }
    }

    fun signUp(email: String, password: String, role: UserRole = UserRole.JUMPER, dzName: String? = null, dzLocation: String? = null) {
        viewModelScope.launch {
            _error.value = null
            try {
                val displayName = dzName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
                userRepository.signUp(email, password, displayName, role.name, dzLocation)
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message ?: "Sign up failed"
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _error.value = null
            try {
                userRepository.signIn(email, password)
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message ?: "Login failed"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun selectRole(role: UserRole) {
        _userRole.value = role
    }

    fun saveRememberedEmail(email: String) {
        settings.putString("remembered_email", email)
    }

    fun getRememberedEmail(): String {
        return settings.getString("remembered_email", "")
    }

    fun clearRememberedEmail() {
        settings.remove("remembered_email")
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.signOut()
            _userRole.value = null
        }
    }
}
