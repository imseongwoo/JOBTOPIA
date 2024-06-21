package com.teamsparta.jobtopia.domain.post.service

import com.teamsparta.jobtopia.domain.comment.dto.CommentDTO
import com.teamsparta.jobtopia.domain.common.exception.ModelNotFoundException
import com.teamsparta.jobtopia.domain.follow.repository.FollowRepository
import com.teamsparta.jobtopia.domain.post.dto.GetPostResponse
import com.teamsparta.jobtopia.domain.post.dto.PostRequest
import com.teamsparta.jobtopia.domain.post.dto.PostResponse
import com.teamsparta.jobtopia.domain.post.model.Post
import com.teamsparta.jobtopia.domain.post.model.toResponse
import com.teamsparta.jobtopia.domain.post.repository.PostRepository
import com.teamsparta.jobtopia.domain.reaction.dto.ReactionResponse
import com.teamsparta.jobtopia.domain.reaction.model.ReactionType
import com.teamsparta.jobtopia.domain.reaction.service.ReactionService
import com.teamsparta.jobtopia.domain.users.repository.UserRepository
import com.teamsparta.jobtopia.infra.s3.service.S3Service
import com.teamsparta.jobtopia.infra.security.UserPrincipal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val reactionService: ReactionService,
    private val followRepository: FollowRepository,
    private val s3Service: S3Service
): PostService {

    override fun getPostList(pageable: Pageable): Page<GetPostResponse> {
        val post = postRepository.findByIsDeletedFalse(pageable)

        val postIds = post.map{ it.id!! }.toMutableList()
        val reactionList = mutableListOf<ReactionResponse>()

        postIds.forEach { id ->
            val reaction = reactionService.getCountReactions(id, null)
            reactionList.add(reaction)
        }

        val postToReaction = postIds.zip(reactionList).toMap()

        return post.map {
            val reaction = postToReaction[it.id]
            GetPostResponse.from(it, reaction!!, it.comments.map { comment -> CommentDTO.from(comment, reactionService.getCountReactions(null, comment.id)) })
        }

    }


    override fun getPostById(postId: Long): GetPostResponse {
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("삭제된 게시글입니다.", postId)
        if (post.isDeleted) throw ModelNotFoundException("삭제된 게시글입니다.", postId)

        val reaction = reactionService.getCountReactions(postId, null)

        return GetPostResponse.from(post, reaction, post.comments.map { comment -> CommentDTO.from(comment, reactionService.getCountReactions(null, comment.id)) })
    }


    @Transactional
    override fun createPost(postRequest: PostRequest,authentication: Authentication, files: MultipartFile?): PostResponse {
        val userPrincipal = authentication.principal as UserPrincipal
        val users = userRepository.findByIdOrNull(userPrincipal.id) ?: throw ModelNotFoundException("없는 user 입니다.", userPrincipal.id)

        val fileUrl = files?.let { s3Service.upload(it) }

        return postRepository.save(
            Post(
                title = postRequest.title,
                content = postRequest.content,
                files = fileUrl,
                users = users
            )
        ).toResponse()
    }


    @Transactional
    override fun updatePost(postId: Long, postRequest: PostRequest, authentication: Authentication, files: MultipartFile?): GetPostResponse {
        val userPrincipal = authentication.principal as UserPrincipal
        val users = userRepository.findByIdOrNull(userPrincipal.id) ?: throw ModelNotFoundException("없는 user 입니다.", userPrincipal.id)
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("삭제된 게시글입니다.", postId)

        if(users.id != post.users.id ) throw IllegalStateException("권한이 없습니다.")

        if (post.isDeleted) throw ModelNotFoundException("삭제된 게시글입니다.", postId)

        post.files?.let { s3Service.delete(it.split("m/")[1]) }
        val imageUrl = files?.let { s3Service.upload(it) }

        post.createPostRequest(postRequest, imageUrl)
        val reaction = reactionService.getCountReactions(post.id!!, null)
        return GetPostResponse.from(postRepository.save(post), reaction)
    }


    @Transactional
    override fun deletePost(postId: Long,authentication: Authentication) {
        val userPrincipal = authentication.principal as UserPrincipal
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("삭제된 게시글입니다.", postId)
        val users = userRepository.findByIdOrNull(userPrincipal.id) ?: throw ModelNotFoundException("없는 user 입니다.", userPrincipal.id)

        if(users.id != post.users.id ) throw IllegalStateException("권한이 없습니다.")

        if (post.isDeleted) throw ModelNotFoundException("삭제된 게시글입니다.", postId)

        post.softDeleted()
        reactionService.deleteReaction(post, null)
        post.files?.let { s3Service.delete(it.split("m/")[1]) }

        post.deletedAt = LocalDateTime.now()
    }

    override fun postLikeReaction(postId: Long, userId: Long) {
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("Post", postId)
        if (post.isDeleted) throw ModelNotFoundException("삭제된 게시글입니다.", postId)

        reactionService.updateReaction(userId, post, null, ReactionType.LIKE)
    }

    override fun postDisLikeReaction(postId: Long, userId: Long) {
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("Post", postId)
        if (post.isDeleted) throw ModelNotFoundException("삭제된 게시글입니다.", postId)

        reactionService.updateReaction(userId, post, null, ReactionType.DISLIKE)
    }

    override fun getFollowingUserPostList(pageable: Pageable, userId: Long): Page<GetPostResponse> {
        val followingResult = followRepository.findAllByUserId(userId)
        val result = postRepository.getFollowingUserPosts(followingResult.map { it.followingUserId }, pageable)

        return result.map { GetPostResponse.from(it) }
    }
}
