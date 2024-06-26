package com.teamsparta.jobtopia.domain.post.repository

import com.teamsparta.jobtopia.domain.post.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime


interface  PostRepository: JpaRepository<Post, Long>, QueryDslPostRepository {
    fun findByIsDeletedFalse(pageable: Pageable): Page<Post>

    @Query("SELECT p FROM Post p WHERE p.isDeleted = true AND p.deletedAt < :threshold")
    fun findOldSoftDeletedPosts(threshold: LocalDateTime): List<Post>
}
