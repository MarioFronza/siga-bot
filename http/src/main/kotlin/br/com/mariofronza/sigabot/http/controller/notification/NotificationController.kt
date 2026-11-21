package br.com.mariofronza.sigabot.http.controller.notification

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.http.controller.NotificationResponse
import br.com.mariofronza.sigabot.http.controller.SentNotificationResponse
import br.com.mariofronza.sigabot.http.security.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.notification.BroadcastNotification
import br.com.mariofronza.sigabot.usecases.notification.ListNotifications
import br.com.mariofronza.sigabot.usecases.notification.NotifyStudent
import br.com.mariofronza.sigabot.usecases.shared.PageRequest
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.PageImpl
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
    private val listNotifications: ListNotifications,
    private val broadcastNotification: BroadcastNotification,
    private val notifyStudent: NotifyStudent,
) {

    @GetMapping("/me/notifications")
    fun list(
        @AuthenticationPrincipal principal: UserDetailsImpl,
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
    ): PagedModel<NotificationResponse> {
        val page = listNotifications(principal.toUser().id, PageRequest(pageable.pageNumber, pageable.pageSize))
        return PagedModel(PageImpl(page.items.map(NotificationResponse::from), pageable, page.totalItems))
    }

    @PostMapping("/notifications")
    @ResponseStatus(ACCEPTED)
    fun broadcast(@Valid @RequestBody request: NotificationRequest) {
        broadcastNotification(request.message)
    }

    @PostMapping("/users/{id}/notifications")
    @ResponseStatus(CREATED)
    fun notifyUser(@PathVariable id: UUID, @Valid @RequestBody request: NotificationRequest): SentNotificationResponse =
        SentNotificationResponse.from(notifyStudent(UserId(id), request.message))
}
