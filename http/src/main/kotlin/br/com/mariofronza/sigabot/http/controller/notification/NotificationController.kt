package br.com.mariofronza.sigabot.http.controller.notification

import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.notification.NotificationService
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationInput
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationOutput
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/{version}", version = "v1")
class NotificationController(
    private val notificationService: NotificationService,
) {

    @GetMapping("/me/notifications")
    fun list(
        @AuthenticationPrincipal principal: UserDetailsImpl,
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
    ): PagedModel<NotificationOutput> = PagedModel(notificationService.listNotifications(principal.toUser(), pageable))

    @PostMapping("/notifications")
    @ResponseStatus(ACCEPTED)
    fun broadcast(@Valid @RequestBody input: NotificationInput) {
        notificationService.createNotificationToAll(input.message)
    }

    @PostMapping("/users/{id}/notifications")
    @ResponseStatus(CREATED)
    fun notifyUser(@PathVariable id: UUID, @Valid @RequestBody input: NotificationInput): NotificationOutput =
        notificationService.createNotificationToUser(id, input.message)
}
