package dev.frederikpietzko.example

import org.springframework.stereotype.Component

@Component
class DefaultSiteStateProvider : SiteStateProvider {
    override fun provide() = State("Press Me!")
}
