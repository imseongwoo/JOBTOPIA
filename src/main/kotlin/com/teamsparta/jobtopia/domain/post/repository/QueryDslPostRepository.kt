package com.teamsparta.jobtopia.domain.post.repository

import com.teamsparta.jobtopia.domain.post.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface QueryDslPostRepository {
    fun getFollowingUserPosts(userIds: List<Long>, pageable: Pageable): Page<Post>
}