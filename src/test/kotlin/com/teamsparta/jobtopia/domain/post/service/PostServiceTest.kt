package com.teamsparta.jobtopia.domain.post.service

import com.teamsparta.jobtopia.domain.common.exception.ModelNotFoundException
import com.teamsparta.jobtopia.domain.follow.repository.FollowRepository
import com.teamsparta.jobtopia.domain.post.dto.PostRequest
import com.teamsparta.jobtopia.domain.post.dto.PostResponse
import com.teamsparta.jobtopia.domain.post.model.Post
import com.teamsparta.jobtopia.domain.post.repository.PostRepository
import com.teamsparta.jobtopia.domain.reaction.service.ReactionService
import com.teamsparta.jobtopia.domain.users.model.Profile
import com.teamsparta.jobtopia.domain.users.model.Users
import com.teamsparta.jobtopia.domain.users.repository.UserRepository
import com.teamsparta.jobtopia.infra.s3.service.S3Service
import com.teamsparta.jobtopia.infra.security.UserPrincipal
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import java.time.LocalDateTime
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostServiceTest {
    val postRepository = mockk<PostRepository>()
    val userRepository = mockk<UserRepository>()
    val reactionService = mockk<ReactionService>()
    val followRepository = mockk<FollowRepository>()
    val s3Service = mockk<S3Service>()
    val postService = PostServiceImpl(postRepository, userRepository, reactionService, followRepository, s3Service)

    @Test
    fun `should throw ModelNotFoundException when requesting a deleted post`() {
        val postId = 53L
        every { postRepository.findById(postId) } returns Optional.of(
            Post(
                title = "title1",
                content = "content1",
                users = Users(
                    userName = "user",
                    password = "password",
                    profile = Profile(nickname = "nickname", description = "description")
                ),
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                isDeleted = true,
                files = ""
            ).apply { id = postId }
        )

        shouldThrow<ModelNotFoundException> {
            postService.getPostById(postId)
        }
    }

    @Test
    fun `should throw ModelNotFoundException when requesting a non-existent post`() {
        val postId = 1L
        every { postRepository.findByIdOrNull(postId) } returns null

        shouldThrow<ModelNotFoundException> {
            postService.getPostById(postId)
        }
    }

    @Test
    fun `should return PostResponse when creating a new post with valid PostRequest`() {
        val postRequest = PostRequest("New Title", "New Content")
        val authentication = mockk<Authentication>()
        val userPrincipal = UserPrincipal(
            id = 1L,
            userName = "user",
            roles = setOf("USER")
        )
        val profile = Profile(nickname = "nickname", description = "description")
        val user = Users(userName = "user", password = "password", profile = profile)
        user.id = 1L

        every { authentication.principal } returns userPrincipal
        every { userRepository.findByIdOrNull(userPrincipal.id) } returns user

        val postSlot = slot<Post>()
        every { postRepository.save(capture(postSlot)) } answers {
            postSlot.captured.apply { id = 1L }
        }


        val postResponse = PostResponse(1L, "New Title", "New Content", null, LocalDateTime.now(), false)

        val result = postService.createPost(postRequest, authentication, null)
        result.id shouldBe postResponse.id
        result.title shouldBe postResponse.title
        result.content shouldBe postResponse.content
        result.file shouldBe postResponse.file
        result.isDeleted shouldBe postResponse.isDeleted
    }
}