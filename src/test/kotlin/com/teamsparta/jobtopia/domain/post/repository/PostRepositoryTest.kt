package com.teamsparta.jobtopia.domain.post.repository

import com.teamsparta.jobtopia.domain.post.dto.PostSearchRequest
import com.teamsparta.jobtopia.domain.post.model.Post
import com.teamsparta.jobtopia.domain.users.model.Profile
import com.teamsparta.jobtopia.domain.users.model.Users
import com.teamsparta.jobtopia.domain.users.repository.UserRepository
import com.teamsparta.jobtopia.infra.querydsl.QueryDslSupport
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

}