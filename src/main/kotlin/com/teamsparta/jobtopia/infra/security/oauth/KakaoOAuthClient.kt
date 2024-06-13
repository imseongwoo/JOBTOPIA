package com.teamsparta.jobtopia.infra.security.oauth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body


@Component
class KakaoOAuthClient(
    @Value("\${oauth2.kakao.client_id}") val clientId : String,
    @Value("\${oauth2.kakao.redirect_url}") val redirectUrl : String,
    @Value("\${oauth2.kakao.auth_server_base_url}") val authServerBaseUrl : String,
    @Value("\${oauth2.kakao.resource_server_base_url}") val resourceServerBaseUrl : String,
    private val restClient: RestClient
): OAuthClient {
    override fun getLoginPageUrl(): String {
        return StringBuilder(authServerBaseUrl)
            .append("/oauth/authorize")
            .append("?client_id=").append(clientId)
            .append("&redirect_uri=").append(redirectUrl)
            .append("&response_type=").append("code")
            .toString()
    }

    override fun getAccessToken(code: String): String {
        val requestData = mutableMapOf(
            "grant_type" to "authorization_code",
            "client_id" to clientId,
            "redirect_uri" to redirectUrl,
            "code" to code
        )
        return restClient.post()
            .uri("$authServerBaseUrl/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(LinkedMultiValueMap<String,String>().apply { this.setAll(requestData) })
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, response ->
                throw RuntimeException("${response.statusCode} kakao AccessToken 조회 실패")
            }
            .body<KakaoTokenResponse>()
            ?.accessToken
            ?:throw RuntimeException("AccessToken 조회 실패")
    }

    override fun getUserInfo(accessToken: String): OAuthUserInfoResponse {
        return restClient.get()
            .uri("$resourceServerBaseUrl/v2/user/me")
            .header("Authorization", "Bearar $accessToken")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, response ->
                throw RuntimeException("${response.statusCode} kakao user 조회 실패")
            }
            .body<KakaoOAuthUserInfo>()
            ?:throw RuntimeException("kakao user 조회 실패")


    }
}