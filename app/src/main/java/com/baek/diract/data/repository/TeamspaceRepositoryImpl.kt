package com.baek.diract.data.repository

import com.baek.diract.data.datasource.remote.TeamspaceRemoteDataSource
import com.baek.diract.data.mapper.toMemberSummary
import com.baek.diract.data.mapper.toSummaryDomain
import com.baek.diract.domain.common.DataResult
import com.baek.diract.domain.model.MemberSummary
import com.baek.diract.domain.model.TeamspaceSummary
import com.baek.diract.domain.repository.TeamspaceRepository
import javax.inject.Inject

class TeamspaceRepositoryImpl @Inject constructor(
    private val remote: TeamspaceRemoteDataSource
) : TeamspaceRepository {

    override suspend fun getTeamspace(teamspaceId: String): DataResult<TeamspaceSummary> =
        runCatching {
            val dto = remote.getTeamspace(teamspaceId)
            dto.toSummaryDomain()
        }.fold(
            onSuccess = { DataResult.Success(it) },
            onFailure = { DataResult.Error(it) }
        )

    override suspend fun renameTeamspace(teamspaceId: String, newName: String): DataResult<Unit> =
        runCatching { remote.renameTeamspace(teamspaceId, newName) }
            .fold({ DataResult.Success(Unit) }, { DataResult.Error(it) })

    override suspend fun deleteTeamspace(teamspaceId: String): DataResult<Unit> =
        runCatching { remote.deleteTeamspace(teamspaceId) }
            .fold({ DataResult.Success(Unit) }, { DataResult.Error(it) })

    override suspend fun transferOwnership(teamspaceId: String, newOwnerId: String): DataResult<Unit> =
        runCatching { remote.transferOwnership(teamspaceId, newOwnerId) }
            .fold({ DataResult.Success(Unit) }, { DataResult.Error(it) })

    override suspend fun getMembers(teamspaceId: String): DataResult<List<MemberSummary>> =
        runCatching {
            val members = remote.getMembers(teamspaceId)              // List<MembersDto>
            val userIds = members.map { it.user_id }.distinct()
            val users = remote.getUsers(userIds)                      // List<UsersDto>

            val userMap = users.associateBy { it.user_id }

            members.mapNotNull { m ->
                val u = userMap[m.user_id] ?: return@mapNotNull null
                m.toMemberSummary(u)
            }
        }.fold(
            onSuccess = { DataResult.Success(it) },
            onFailure = { DataResult.Error(it) }
        )


    override suspend fun kickMember(teamspaceId: String, targetUserId: String): DataResult<Unit> =
        runCatching { remote.kickMember(teamspaceId, targetUserId) }
            .fold({ DataResult.Success(Unit) }, { DataResult.Error(it) })

    override suspend fun leaveTeamspace(teamspaceId: String, userId: String): DataResult<Unit> =
        runCatching { remote.leaveTeamspace(teamspaceId, userId) }
            .fold({ DataResult.Success(Unit) }, { DataResult.Error(it) })

    override suspend fun warnMember(teamspaceId: String, targetUserId: String, reason: String?): DataResult<Unit> =
        runCatching { remote.warnMember(teamspaceId, targetUserId, reason) }
            .fold({ DataResult.Success(Unit) }, { DataResult.Error(it) })
}