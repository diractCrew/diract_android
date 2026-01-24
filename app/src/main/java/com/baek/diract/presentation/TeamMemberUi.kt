package com.baek.diract.presentation

data class TeamMemberUi(
    val id: String,
    val name: String,
    val isLeader: Boolean = false
)