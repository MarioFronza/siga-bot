package br.com.mariofronza.sigabot.entity.session

class InvalidRefreshTokenException(reason: String) : RuntimeException("Invalid refresh token: $reason")
