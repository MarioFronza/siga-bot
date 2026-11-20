package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.Cpf
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.EmailAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.entity.user.UserNotFoundException
import br.com.mariofronza.sigabot.usecases.credential.gateway.PasswordHasher
import br.com.mariofronza.sigabot.usecases.credential.gateway.SecretCipher
import br.com.mariofronza.sigabot.usecases.institution.gateway.InstitutionInteractor
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.UserOutput
import br.com.mariofronza.sigabot.usecases.user.gateway.AcademicAccountInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.springframework.stereotype.Service

@Service
class UserService(
    val userInteractor: UserInteractor,
    val institutionInteractor: InstitutionInteractor,
    val academicAccountInteractor: AcademicAccountInteractor,
    val passwordHasher: PasswordHasher,
    val secretCipher: SecretCipher,
) {

    fun findAllActiveUsers(): List<User> = userInteractor.findAllActive()

    fun findById(id: UserId): User = userInteractor.findById(id) ?: throw UserNotFoundException(id)

    fun createUser(request: CreateUserInput): UserOutput {
        val email = Email.of(request.email)
        val loginDigest = secretCipher.digest(Cpf.of(request.sigaCredential.cpf).value)
        val institution = InstitutionCode.UDESC

        if (userInteractor.findByEmail(email) != null) {
            throw EmailAlreadyRegisteredException()
        }

        checkNotNull(institutionInteractor.findByCode(institution)) { "Institution $institution is not seeded" }

        if (academicAccountInteractor.existsByLoginDigest(institution, loginDigest)) {
            throw AcademicAccountAlreadyRegisteredException()
        }

        val account = AcademicAccount(
            institution = institution,
            loginCiphertext = secretCipher.encrypt(request.sigaCredential.cpf),
            loginDigest = loginDigest,
            passwordCiphertext = secretCipher.encrypt(request.sigaCredential.password),
        )
        val user = User.registerStudent(email, passwordHasher.hash(request.password), account)
        userInteractor.save(user)

        return UserOutput.from(user)
    }
}
