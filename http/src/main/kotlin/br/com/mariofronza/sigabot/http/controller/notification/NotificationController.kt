package br.com.mariofronza.sigabot.http.controller.notification

import br.com.mariofronza.sigabot.usecases.notification.NotificationService
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationInput
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationOutput
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {

    @PostMapping("/all")
    fun createToAll(@RequestBody notificationInput: NotificationInput) {
        notificationService.createNotificationToAll(notificationInput.description)
    }

    @PostMapping("/user/{id}")
    fun createToUser(@PathVariable id: String, @RequestBody notificationInput: NotificationInput) {
        notificationService.createNotificationToUser(id, notificationInput)
    }

    @GetMapping
    fun list(): List<NotificationOutput> {
        return notificationService.listNotifications()
    }
}
