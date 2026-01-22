package com.baek.diract.domain.repository

import com.baek.diract.domain.common.DataResult
import com.baek.diract.domain.model.TeamspaceSummary
import com.baek.diract.domain.model.MemberSummary

interface TeamspaceRepository {

    // 팀스페이스 기본 정보
    suspend fun getTeamspace(teamspaceId: String): DataResult<TeamspaceSummary>

    // 팀스페이스 이름 수정
    suspend fun renameTeamspace(teamspaceId: String, newName: String): DataResult<Unit>

    // 팀스페이스 삭제 (보통 owner만)
    suspend fun deleteTeamspace(teamspaceId: String): DataResult<Unit>

    // 팀스페이스 나가기 (멤버가 나감)
    suspend fun leaveTeamspace(teamspaceId: String, userId: String): DataResult<Unit>

    // 멤버 목록 (MembersDto + UsersDto 조합해서 UI에 쓰기 좋게)
    suspend fun getMembers(teamspaceId: String): DataResult<List<MemberSummary>>

    // 팀원 추방 (owner/관리자 권한)
    suspend fun kickMember(teamspaceId: String, targetUserId: String): DataResult<Unit>

    // 팀장(소유자) 권한 넘기기
    suspend fun transferOwnership(teamspaceId: String, newOwnerId: String): DataResult<Unit>

    // 팀원 경고 (정책에 따라: warning_count 증가 or warning 기록 추가)
    suspend fun warnMember(teamspaceId: String, targetUserId: String, reason: String?): DataResult<Unit>
}
