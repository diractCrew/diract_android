package com.baek.diract.presentation.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("home_tips", Context.MODE_PRIVATE)

    val homeTipStep = MutableLiveData(prefs.getInt("step", 0))

    fun setHomeTipStep(step: Int) {
        prefs.edit().putInt("step", step).apply()
        homeTipStep.value = step
    }

    fun createTeamspace(name: String) {
        // TODO repository/usecase 연결
    }

    fun createSong(name: String) {
        // TODO repository/usecase 연결
    }
    fun createProject(name: String) {
        // TODO repository/usecase 연결
    }

    fun renameProject(projectId: String, newName: String) {
        // TODO repository/usecase 연결
    }

    fun deleteProject(projectId: String) {
        // TODO repository/usecase 연결
    }
}