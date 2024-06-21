package com.teamsparta.jobtopia.domain.post.service

import com.teamsparta.jobtopia.domain.post.dto.GetPostResponse
import com.teamsparta.jobtopia.domain.post.dto.PostRequest
import com.teamsparta.jobtopia.domain.post.dto.PostResponse
import com.teamsparta.jobtopia.domain.post.dto.PostSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.web.multipart.MultipartFile


interface PostService {

    fun getPostList(pageable: Pageable): Page<GetPostResponse>
    fun getPostById(postId: Long):GetPostResponse
    fun createPost(postRequest: PostRequest,authentication: Authentication, files: MultipartFile?):PostResponse
    fun updatePost(postId: Long,postRequest: PostRequest,authentication: Authentication, image: MultipartFile?):GetPostResponse
    fun deletePost(postId: Long,authentication: Authentication)
    fun postLikeReaction(postId: Long, userId: Long)
    fun postDisLikeReaction(postId: Long, userId: Long)
    fun getFollowingUserPostList(pageable: Pageable, userId: Long): Page<GetPostResponse>
    fun getPostListByKeyword(pageable: Pageable, postSearchRequest: PostSearchRequest?): Page<GetPostResponse>
}