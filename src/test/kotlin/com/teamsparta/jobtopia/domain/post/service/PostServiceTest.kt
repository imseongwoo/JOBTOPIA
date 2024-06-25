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
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import java.time.LocalDateTime

@SpringBootTest
@ExtendWith(MockKExtension::class)
class PostServiceTest : BehaviorSpec({
    extension(SpringExtension)

    afterContainer {
        clearAllMocks()
    }
    val postRepository = mockk<PostRepository>()
    val userRepository = mockk<UserRepository>()
    val reactionService = mockk<ReactionService>()
    val followRepository = mockk<FollowRepository>()
    val s3Service = mockk<S3Service>()

    val postService = PostServiceImpl(postRepository, userRepository, reactionService, followRepository, s3Service)

    Given("Post가 삭제되었을 때") {
        When("특정 Post를 요청하면") {
            Then("ModelNotFoundException이 발생해야 한다.") {
                val postId = 53L
                every { postRepository.findByIdOrNull(postId)?.isDeleted } returns true

                shouldThrow<ModelNotFoundException> {
                    postService.getPostById(postId)
                }
            }
        }
    }

    Given("Post가 존재하지 않을 때") {
        When("특정 Post를 요청하면") {
            Then("ModelNotFoundException이 발생해야 한다.") {
                val postId = 1L
                every { postRepository.findByIdOrNull(any()) } returns null

                shouldThrow<ModelNotFoundException> {
                    postService.getPostById(postId)
                }
            }
        }
    }

    Given("유효한 PostRequest가 주어졌을 때") {
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

        When("새로운 Post를 생성하면") {
            Then("PostResponse가 반환되어야 한다.") {
                val post = Post(
                    title = postRequest.title,
                    content = postRequest.content,
                    files = null,
                    users = user
                ).apply { id = 1L }

                val postResponse = PostResponse(1L, "New Title", "New Content", null, LocalDateTime.now(), false)

                every { postRepository.save(any<Post>()) } returns post

                val result = postService.createPost(postRequest, authentication, null)
                result shouldBe postResponse
            }
        }
    }

})