package br.com.mariofronza.sigabot.usecases.academic.port

sealed class AcademicSystemException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class SigaCredentialRejectedException : AcademicSystemException("SIGA rejected the credential")

class SigaUnavailableException(message: String, cause: Throwable? = null) : AcademicSystemException(message, cause)

class SigaLayoutChangedException(message: String) : AcademicSystemException(message)

class SigaPeriodNotFoundException(period: String) : AcademicSystemException("SIGA has no period $period")
