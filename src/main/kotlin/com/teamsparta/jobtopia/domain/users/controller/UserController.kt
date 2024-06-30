package com.teamsparta.jobtopia.domain.users.controller

import com.teamsparta.jobtopia.domain.users.dto.*
import com.teamsparta.jobtopia.domain.users.service.UserService
import com.teamsparta.jobtopia.infra.aop.LogExecutionTime
import com.teamsparta.jobtopia.infra.security.UserPrincipal
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v1")
@RestController
class UserController(
    private val userService: UserService
) {
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody signUpRequest: SignUpRequest
    ): ResponseEntity<UserDto> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(userService.signUp(signUpRequest))
    }

    @LogExecutionTime
    @PostMapping("/login")
    fun signIn(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.login(loginRequest))

    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<Unit> {
        val token = request.getAttribute("accessToken") as String?
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.logout(token!!))
    }

    @PatchMapping("/users/profile")
    fun updateProfile(
        request: HttpServletRequest,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody profile: UserUpdateProfileDto
    ): ResponseEntity<UserDto> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.updateProfile(profile, principal.id))
    }

}