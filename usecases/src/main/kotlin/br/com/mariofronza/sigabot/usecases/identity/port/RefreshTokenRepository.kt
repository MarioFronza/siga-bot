package br.com.mariofronza.sigabot.usecases.identity.port

import br.com.mariofronza.sigabot.entity.session.RefreshToken
import java.time.Instant
import java.util.UUID

interface RefreshTokenRepository {

    fun save(token: RefreshToken)

    fun findByHash(tokenHash: ByteArray): RefreshToken?

    fun revokeFamily(familyId: UUID, now: Instant)
}
