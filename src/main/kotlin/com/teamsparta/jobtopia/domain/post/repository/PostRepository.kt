package com.teamsparta.jobtopia.domain.post.repository

import com.teamsparta.jobtopia.domain.post.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository


interface  PostRepository: JpaRepository<Post, Long>, QueryDslPostRepository {
    fun findByIsDeletedFalse(pageable: Pageable): Page<Post>
}
