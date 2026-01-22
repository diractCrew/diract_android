package com.baek.diract.data.datasource.remote

import com.baek.diract.data.remote.dto.MembersDto
import com.baek.diract.data.remote.dto.TeamspaceDto
import com.baek.diract.data.remote.dto.UsersDto

interface TeamspaceRemoteDataSource {

    suspend fun getTeamspace(teamspaceId: String): TeamspaceDto

    suspend fun renameTeamspace(teamspaceId: String, newName: String)

    suspend fun deleteTeamspace(teamspaceId: String)

    suspend fun transferOwnership(teamspaceId: String, newOwnerId: String)

    suspend fun getMembers(teamspaceId: String): List<MembersDto>

    suspend fun kickMember(teamspaceId: String, targetUserId: String)

    suspend fun leaveTeamspace(teamspaceId: String, userId: String)

    suspend fun warnMember(teamspaceId: String, targetUserId: String, reason: String?)

    suspend fun getUsers(userIds: List<String>): List<UsersDto>
}
