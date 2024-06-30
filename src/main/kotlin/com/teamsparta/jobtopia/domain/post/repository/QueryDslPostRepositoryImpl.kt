package com.teamsparta.jobtopia.domain.post.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.teamsparta.jobtopia.domain.post.dto.PostSearchRequest
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

    override fun searchPostsByKeyword(postSearchRequest: PostSearchRequest?, pageable: Pageable): Page<Post> {
        val query = queryFactory.selectFrom(post)
            .where(
                titleContains(postSearchRequest?.title),
                contentContains(postSearchRequest?.content)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(post.createdAt.desc())

        val contents = query.fetch()

        val total = queryFactory.select(post.count())
            .from(post)
            .where(
                titleContains(postSearchRequest?.title),
                contentContains(postSearchRequest?.content)
            )
            .fetchOne() ?: 0L

        return PageImpl(contents, pageable, total)
    }

    private fun titleContains(title: String?): BooleanExpression? {
        return if (title.isNullOrBlank()) null else post.title.containsIgnoreCase(title)
    }

    private fun contentContains(content: String?): BooleanExpression? {
        return if (content.isNullOrBlank()) null else post.content.containsIgnoreCase(content)
    }

}