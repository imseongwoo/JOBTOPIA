package com.teamsparta.jobtopia.domain.comment.controller

import com.teamsparta.jobtopia.domain.comment.dto.CommentDTO
import com.teamsparta.jobtopia.domain.comment.dto.CommentModifyDTO
import com.teamsparta.jobtopia.domain.comment.dto.CommentPostDTO
import com.teamsparta.jobtopia.domain.comment.service.CommentService
import com.teamsparta.jobtopia.domain.reaction.service.ReactionService
import com.teamsparta.jobtopia.infra.aop.LogExecutionTime
import com.teamsparta.jobtopia.infra.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
class CommentController(
    private val commentService: CommentService
) {
    @LogExecutionTime
    @GetMapping
    fun getCommentList(@PathVariable postId: Long): ResponseEntity<List<CommentDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(commentService.getCommentList(postId))
    }

    @GetMapping("/{commentId}")
    fun getComment(@PathVariable postId: Long, @PathVariable commentId: Long): ResponseEntity<CommentDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(commentService.getComment(postId, commentId))
    }

    @PostMapping
    fun postComment(
        @PathVariable postId: Long,
        @RequestBody commentPostDTO: CommentPostDTO,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<CommentDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(commentService.postComment(postId, commentPostDTO, principal.id))
    }

    @PutMapping("/{commentId}")
    fun modifyComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @RequestBody commentModifyDTO: CommentModifyDTO,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<CommentDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(commentService.modifyComment(postId, commentId, commentModifyDTO, principal.id))
    }

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Unit> {
        commentService.deleteComment(postId, commentId, principal.id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping("/{commentId}/like")
    fun commentLikeReaction(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Unit> {
        commentService.commentLikeReaction(postId, commentId, principal.id)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @PostMapping("/{commentId}/dislike")
    fun commentDisLikeReaction(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Unit> {
        commentService.commentLikeReaction(postId, commentId, principal.id)
        return ResponseEntity.status(HttpStatus.OK).build()
    }
}