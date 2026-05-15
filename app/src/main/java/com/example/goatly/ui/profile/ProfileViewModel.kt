package com.example.goatly.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.LocalProvider
import com.example.goatly.data.network.ChangePasswordRequest
import com.example.goatly.data.network.RetrofitClient
import com.example.goatly.data.network.TokenManager
import com.example.goatly.data.network.UpdateProfileRequest
import com.example.goatly.data.network.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class ProfileViewModel : ViewModel() {

    private val api = RetrofitClient.api
    private val userPrefs = LocalProvider.userPrefs

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // Sprint 4: Caching — Coil image cache — carnet upload state
    private val _carnetUploadSuccess = MutableStateFlow(false)
    val carnetUploadSuccess: StateFlow<Boolean> = _carnetUploadSuccess.asStateFlow()

    private val _carnetDeleteSuccess = MutableStateFlow(false)
    val carnetDeleteSuccess: StateFlow<Boolean> = _carnetDeleteSuccess.asStateFlow()

    fun seedFromAuth(name: String, email: String, department: String, language: String = "es", isDarkMode: Boolean = false) {
        _user.value = UserResponse(
            id = 0,
            name = name,
            email = email,
            department = department,
            role = "student",
            language = language,
            isDarkMode = isDarkMode
        )
    }

    fun loadProfile() {
        val token = TokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    api.getMe("Bearer $token")
                }
                _user.value = result
                _isOffline.value = false
                withContext(Dispatchers.IO) {
                    userPrefs.saveUserPreferences(
                        name = result.name,
                        email = result.email,
                        department = result.department,
                        language = result.language,
                        isDarkMode = result.isDarkMode
                    )
                }
            } catch (e: IOException) {
                // developer: separar IOException para EVC
                val hasLocal = withContext(Dispatchers.IO) { userPrefs.hasData() }
                _isOffline.value = true
                if (!hasLocal && _user.value == null) {
                    _error.value = "Sin conexión — no hay datos disponibles"
                }
            } catch (e: HttpException) {
                _isOffline.value = false
                _error.value = when (e.code()) {
                    401 -> "Sesión expirada — inicia sesión nuevamente"
                    else -> "Error al cargar el perfil"
                }
            } catch (_: Exception) {
                _isOffline.value = false
                _error.value = "Error al cargar el perfil"
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(name: String, department: String, language: String, onSuccess: () -> Unit) {
        val token = TokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentDarkMode = _user.value?.isDarkMode ?: false
                val result = withContext(Dispatchers.IO) {
                    api.updateProfile(
                        "Bearer $token",
                        UpdateProfileRequest(name.trim(), department, language, currentDarkMode)
                    )
                }
                _user.value = result
                _isOffline.value = false
                withContext(Dispatchers.IO) {
                    userPrefs.saveUserPreferences(
                        name = result.name,
                        email = result.email,
                        department = result.department,
                        language = result.language,
                        isDarkMode = result.isDarkMode
                    )
                }
                onSuccess()
            } catch (e: IOException) {
                _isOffline.value = true
                _error.value = "Sin conexión — cambios no guardados"
            } catch (e: HttpException) {
                _isOffline.value = false
                _error.value = when (e.code()) {
                    400 -> "Datos inválidos — verifica los campos"
                    401 -> "Sesión expirada — inicia sesión nuevamente"
                    else -> "Error al actualizar el perfil"
                }
            } catch (_: Exception) {
                _isOffline.value = false
                _error.value = "Error al actualizar el perfil"
            }
            _isLoading.value = false
        }
    }

    fun changePassword(currentPw: String, newPw: String, confirmPw: String, onSuccess: () -> Unit) {
        val token = TokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                withContext(Dispatchers.IO) {
                    api.changePassword(
                        "Bearer $token",
                        ChangePasswordRequest(currentPw, newPw, confirmPw)
                    )
                }
                _isOffline.value = false
                onSuccess()
            } catch (e: IOException) {
                _isOffline.value = true
                _error.value = "Sin conexión — cambio de contraseña no guardado"
            } catch (e: HttpException) {
                _isOffline.value = false
                _error.value = when (e.code()) {
                    400 -> "Contraseña actual incorrecta"
                    401 -> "Sesión expirada — inicia sesión nuevamente"
                    else -> "Error al cambiar la contraseña"
                }
            } catch (_: Exception) {
                _isOffline.value = false
                _error.value = "Error al cambiar la contraseña"
            }
            _isLoading.value = false
        }
    }

    // ============================================================
    // Sprint 4: Caching — Coil image cache — uploadCarnet
    // ============================================================
    fun uploadCarnet(context: Context, uri: Uri) {
        val token = TokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.readBytes()
                } ?: run {
                    _error.value = "No se pudo leer la imagen"
                    _isLoading.value = false
                    return@launch
                }

                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = if (mimeType.contains("png")) "png" else "jpg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", "carnet.$extension", requestBody)

                Log.d("GOATLY_COIL", "uploadCarnet — sending ${bytes.size} bytes to /users/me/carnet on Dispatchers.IO")

                val result = withContext(Dispatchers.IO) {
                    api.uploadCarnet("Bearer $token", part)
                }

                _user.value = result
                _carnetUploadSuccess.value = true
                Log.d("GOATLY_COIL", "uploadCarnet — success, Coil will cache image from URL: ${result.profilePicture}")

            } catch (e: Exception) {
                Log.e("GOATLY_COIL", "uploadCarnet — failed: ${e.message}")
                if (e is java.net.UnknownHostException || e is java.net.ConnectException || e is IOException) {
                    _error.value = "Sin conexión. Conéctate a internet para subir tu carnet."
                } else {
                    _error.value = "No se pudo subir el carnet. Intenta de nuevo."
                }
            }
            _isLoading.value = false
        }
    }

    // Sprint 4: Caching — delete carnet
    fun deleteCarnet() {
        val token = TokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("GOATLY_COIL", "deleteCarnet — removing carnet URL on Dispatchers.IO")
                val result = withContext(Dispatchers.IO) {
                    api.deleteCarnet("Bearer $token")
                }
                _user.value = result
                _carnetDeleteSuccess.value = true
                Log.d("GOATLY_COIL", "deleteCarnet — success, profile_picture cleared")
            } catch (e: Exception) {
                Log.e("GOATLY_COIL", "deleteCarnet — failed: ${e.message}")
                if (e is java.net.UnknownHostException || e is java.net.ConnectException || e is IOException) {
                    _error.value = "Sin conexión. Conéctate a internet para eliminar tu carnet."
                } else {
                    _error.value = "No se pudo eliminar el carnet. Intenta de nuevo."
                }
            }
            _isLoading.value = false
        }
    }
    // Sprint 4: Caching — END

    fun clearError() { _error.value = null }
    fun clearCarnetSuccess() { _carnetUploadSuccess.value = false }
    fun clearCarnetDeleteSuccess() { _carnetDeleteSuccess.value = false }

    fun clear() {
        _user.value = null
        _error.value = null
        _isOffline.value = false
    }
}