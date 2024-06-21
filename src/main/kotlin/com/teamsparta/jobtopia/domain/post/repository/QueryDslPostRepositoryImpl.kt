package com.teamsparta.jobtopia.domain.post.repository

import com.teamsparta.jobtopia.domain.post.model.Post
import com.teamsparta.jobtopia.domain.post.model.QPost
import com.teamsparta.jobtopia.infra.querydsl.QueryDslSupport
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class QueryDslPostRepositoryImpl : QueryDslPostRepository, QueryDslSupport() {
    private val post = QPost.post

    override fun getFollowingUserPosts(userIds: List<Long>, pageable: Pageable): Page<Post> {
        val query = queryFactory.selectFrom(post)
            .where(post.users.id.`in`(userIds))
            .orderBy(post.users.id.asc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val contents = query.fetch()

        return PageImpl(contents)
    }

}