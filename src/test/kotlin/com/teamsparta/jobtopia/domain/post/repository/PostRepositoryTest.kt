package com.teamsparta.jobtopia.domain.post.repository

import com.teamsparta.jobtopia.domain.post.dto.PostSearchRequest
import com.teamsparta.jobtopia.domain.post.model.Post
import com.teamsparta.jobtopia.domain.users.model.Profile
import com.teamsparta.jobtopia.domain.users.model.Users
import com.teamsparta.jobtopia.domain.users.repository.UserRepository
import com.teamsparta.jobtopia.infra.querydsl.QueryDslSupport
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(value = [QueryDslSupport::class])
@ActiveProfiles("test")
class PostRepositoryTest {
    @Autowired
    lateinit var postRepository: PostRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `Keyword에 의해 Post가 검색되는지 확인`() {
        // GIVEN
        val profile = Profile(nickname = "nickname", description = "description")
        val user = Users(userName = "user", password = "password", profile = profile)
        userRepository.save(user)

        val postList = listOf(
            Post(
                title = "title1",
                content = "content1",
                users = user,
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                files = ""
            ),
            Post(
                title = "title2",
                content = "content2",
                users = user,
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                files = ""
            ),
            Post(
                title = "title3",
                content = "content3",
                users = user,
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                files = ""
            ),
            Post(
                title = "title4",
                content = "content4",
                users = user,
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                files = ""
            )
        )

        postRepository.saveAllAndFlush(postList)

        val postSearchRequest = PostSearchRequest(title = "title", content = "content")
        val postSearchRequest2 = PostSearchRequest(title = "title1", content = "content1")
        val pageable = PageRequest.of(0, 9)
        // WHEN
        val result = postRepository.searchPostsByKeyword(postSearchRequest, pageable)
        val result2 = postRepository.searchPostsByKeyword(postSearchRequest2, pageable)
        // THEN
        result.content.size shouldBe 4
        result2.content.size shouldBe 1
    }

    @Test
    fun `팔로우하는 사용자의 게시물 가져오기`() {
        // GIVEN
        val profile1 = Profile(nickname = "nickname1", description = "description1")
        val user1 = Users(userName = "user1", password = "password1", profile = profile1)
        userRepository.save(user1)

        val profile2 = Profile(nickname = "nickname2", description = "description2")
        val user2 = Users(userName = "user2", password = "password2", profile = profile2)
        userRepository.save(user2)

        val postList = listOf(
            Post(
                title = "title1",
                content = "content1",
                users = user1,
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                files = ""
            ),
            Post(
                title = "title2",
                content = "content2",
                users = user1,
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                files = ""
            ),
            Post(
                title = "title3",
                content = "content3",
                users = user2,
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                files = ""
            ),
            Post(
                title = "title4",
                content = "content4",
                users = user2,
                updatedAt = LocalDateTime.now(),
                deletedAt = LocalDateTime.now(),
                files = ""
            )
        )
        postRepository.saveAll(postList)

        val userIds = listOf(user1.id!!, user2.id!!)
        val pageable = PageRequest.of(0, 10)

        // WHEN
        val result = postRepository.getFollowingUserPosts(userIds, pageable)

        // THEN
        result.content shouldContainExactly postList
    }

}