package com.teamsparta.jobtopia.domain.post.controller

import com.google.gson.GsonBuilder
import com.teamsparta.jobtopia.domain.comment.dto.CommentDTO
import com.teamsparta.jobtopia.domain.post.dto.GetPostResponse
import com.teamsparta.jobtopia.domain.post.service.PostService
import com.teamsparta.jobtopia.infra.security.jwt.JwtPlugin
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import java.time.LocalDateTime
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockKExtension::class)
class PostControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jwtPlugin: JwtPlugin
) : DescribeSpec({

    val logger = LoggerFactory.getLogger(PostControllerTest::class.java)

    extension(SpringExtension)

    afterContainer {
        clearAllMocks()
    }

    val postService = mockk<PostService>()

    val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        .create()

    fun generateRandomLocalDateTime(): LocalDateTime {
        val randomYear = Random.nextInt(2000, 2030)
        val randomMonth = Random.nextInt(1, 12)
        val randomDay = Random.nextInt(1, 28)
        val randomHour = Random.nextInt(0, 23)
        val randomMinute = Random.nextInt(0, 59)
        val randomSecond = Random.nextInt(0, 59)
        return LocalDateTime.of(randomYear, randomMonth, randomDay, randomHour, randomMinute, randomSecond)
    }

    describe("GET /api/v1/posts/{postId}는") {
        context("존재하는 id를 요청을 보낼 때") {
            it("상태 코드 200을 응답해야 한다.") {
                val postId = 79L
                val createdAt = generateRandomLocalDateTime()
                val updatedAt = generateRandomLocalDateTime()

                val postResponse = GetPostResponse(
                    id = postId,
                    title = "Title",
                    content = "Content",
                    file = "",
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    isDeleted = false,
                    like = 10,
                    dislike = 1,
                    comments = listOf(CommentDTO(id = 1L, content = "content", like = 1, dislike = 1))
                )
                every { postService.getPostById(postId) } returns postResponse

                val jwtToken = jwtPlugin.generateAccessToken(
                    subject = postId.toString(),
                    userName = "admin",
                    role = "USER"
                )

                logger.info("JWT Token: $jwtToken")

                val result = mockMvc.perform(
                    get("/api/v1/posts/$postId")
                        .header("Authorization", "Bearer $jwtToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andReturn()

                logger.info("Response status: ${result.response.status}")
                logger.info("Response content: ${result.response.contentAsString}")

                result.response.status shouldBe 200

                val responseDto = gson.fromJson(
                    result.response.contentAsString,
                    GetPostResponse::class.java
                )
                responseDto.id shouldBe postId
            }
        }
    }

    describe("GET /api/v1/posts") {
        context("페이지 가능한 게시물 목록을 요청할 때") {
            it("상태 코드 200을 응답해야 한다.") {
                val pageable = PageRequest.of(0, 9)
                val postList = listOf(
                    GetPostResponse(
                        1L,
                        "Title1",
                        "Content1",
                        "",
                        generateRandomLocalDateTime(),
                        generateRandomLocalDateTime(),
                        false,
                        10,
                        1,
                        listOf()
                    ),
                    GetPostResponse(
                        2L,
                        "Title2",
                        "Content2",
                        "",
                        generateRandomLocalDateTime(),
                        generateRandomLocalDateTime(),
                        false,
                        20,
                        2,
                        listOf()
                    )
                )
//                val page = PageImpl(postList, pageable, postList.size.toLong())
//                every { postService.getPostList(pageable) } returns page

                val jwtToken = jwtPlugin.generateAccessToken(
                    subject = 12L.toString(),
                    userName = "admin",
                    role = "USER"
                )


                val result = mockMvc.perform(
                    get("/api/v1/posts")
                        .header("Authorization", "Bearer $jwtToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andReturn()

                logger.info("Response status: ${result.response.status}")
                logger.info("Response content: ${result.response.contentAsString}")

                result.response.status shouldBe 200

                val responseDto = gson.fromJson(
                    result.response.contentAsString,
                    Map::class.java
                )
                val content = responseDto["content"] as List<*>
                println("컨텐트 : ${content}")
                content.size shouldBe 10
            }
        }
    }
})