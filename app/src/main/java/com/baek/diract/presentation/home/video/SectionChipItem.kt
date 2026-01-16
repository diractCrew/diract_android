package com.baek.diract.presentation.home.video

sealed class SectionChipItem {
    data object AddSection : SectionChipItem()

    data class SectionUi(
        val id: String,
        val name: String,
        val isSelected: Boolean = false
    ) : SectionChipItem()
}
