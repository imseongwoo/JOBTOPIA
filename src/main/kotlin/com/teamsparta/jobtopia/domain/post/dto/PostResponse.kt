package com.teamsparta.jobtopia.domain.post.dto

import java.time.LocalDateTime

data class PostResponse (
    val id : Long,
    val title : String,
    val content : String,
    val createdAt : LocalDateTime,
    val deleted : Boolean,
)