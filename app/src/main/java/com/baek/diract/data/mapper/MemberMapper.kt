package com.baek.diract.data.mapper

import com.baek.diract.data.remote.dto.MembersDto
import com.baek.diract.data.remote.dto.UsersDto
import com.baek.diract.domain.model.MemberSummary

fun MembersDto.toMemberSummary(user: UsersDto): MemberSummary = MemberSummary(
    userId = user_id,
    name = user.name,
    email = user.email,
    joinedAtEpochSeconds = joined_at?.seconds
)
