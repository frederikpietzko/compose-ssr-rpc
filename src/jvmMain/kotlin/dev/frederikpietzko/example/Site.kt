package dev.frederikpietzko.example

import org.jetbrains.compose.web.renderHydratedDocument
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody


@Controller
class SiteController {
    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun site() = ResponseEntity
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(
            renderHydratedDocument { Site() }
        )
}