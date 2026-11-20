package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.Cpf
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.EmailAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.identity.port.InstitutionRepository
import br.com.mariofronza.sigabot.usecases.identity.port.PasswordHasher
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository

class RegisterStudent(
    private val users: UserRepository,
    private val institutions: InstitutionRepository,
    private val passwordHasher: PasswordHasher,
    private val secretCipher: SecretCipher,
) {

    data class Command(val email: String, val password: String, val cpf: String, val sigaPassword: String)

    operator fun invoke(command: Command): User {
        val email = Email.of(command.email)
        val loginDigest = secretCipher.digest(Cpf.of(command.cpf).value)
        val institution = InstitutionCode.UDESC

        if (users.findByEmail(email) != null) throw EmailAlreadyRegisteredException()
        checkNotNull(institutions.findByCode(institution)) { "Institution $institution is not seeded" }
        if (users.existsByLoginDigest(institution, loginDigest)) throw AcademicAccountAlreadyRegisteredException()

        val account = AcademicAccount(
            institution = institution,
            loginCiphertext = secretCipher.encrypt(command.cpf),
            loginDigest = loginDigest,
            passwordCiphertext = secretCipher.encrypt(command.sigaPassword),
        )
        return users.save(User.registerStudent(email, passwordHasher.hash(command.password), account))
    }
}
