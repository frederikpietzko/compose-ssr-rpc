package dev.frederikpietzko.example

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.HydrationRoot
import org.jetbrains.compose.web.attributes.ScriptType
import org.jetbrains.compose.web.attributes.src
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderHydratedDocument
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Composable
fun Site() {
    Html(attrs = { lang("en") }) {
        Head {
            Meta { attr("charset", "UTF-8") }
            Meta {
                attr("name", "viewport")
                attr("content", "width=device-width, initial-scale=1")
            }
            Title { Text("Compose SSR with Hydration and kotlinx.rpc") }
        }
        Body {
            HydrationRoot(State("Press Me!"), Json::encodeToString) { initialState ->
                App(initialState.initialMessage)
            }
        }
        Script(attrs = {
            type(ScriptType.Module)
            src("/search-client.mjs")
        })
    }
}

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