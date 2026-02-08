package org.saripalli.aigateway.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class WebController {

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }

    @GetMapping("/chat")
    fun chat(): String {
        return "chat"
    }

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        model.addAttribute("pageTitle", "Overview")
        model.addAttribute("currentPage", "overview")
        return "dashboard/index"
    }

    @GetMapping("/dashboard/projects")
    fun projects(model: Model): String {
        model.addAttribute("pageTitle", "Projects")
        model.addAttribute("currentPage", "projects")
        return "dashboard/projects"
    }

    @GetMapping("/dashboard/settings")
    fun settings(model: Model): String {
        model.addAttribute("pageTitle", "Settings")
        model.addAttribute("currentPage", "settings")
        return "dashboard/settings"
    }

    @GetMapping("/dashboard/projects/{projectId}/keys")
    fun projectKeys(@PathVariable projectId: UUID, model: Model): String {
        model.addAttribute("pageTitle", "API Keys")
        model.addAttribute("currentPage", "projects")
        model.addAttribute("projectId", projectId.toString())
        return "dashboard/keys"
    }
}
