package dev.frederikpietzko.example.spring.plumbing

import dev.frederikpietzko.example.spring.plumbing.generated.generatedPages
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jetbrains.compose.web.renderHydratedDocument
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanNameGenerator
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata
import org.springframework.http.MediaType
import org.springframework.web.HttpRequestHandler

class PageHandler(
    private val page: PageDefinition<Any?>,
    private val beanFactory: BeanFactory,
) : HttpRequestHandler {
    override fun handleRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {

        val provider = page.stateProvider.objectInstance ?: beanFactory.getBean(page.stateProvider.java)
        val state = provider.provide()
        response.contentType = MediaType.TEXT_HTML_VALUE
        response.writer.write(renderHydratedDocument { page.render(state) })
    }

}

class PageRouteRegistrar : ImportBeanDefinitionRegistrar {
    companion object {
        private val logger = LoggerFactory.getLogger(PageRouteRegistrar::class.java)
    }

    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
        importBeanNameGenerator: BeanNameGenerator
    ) {
        generatedPages.forEach { page ->
            val definition = BeanDefinitionBuilder.genericBeanDefinition(PageHandler::class.java) {
                PageHandler(page as PageDefinition<Any?>, registry as BeanFactory)
            }.beanDefinition
            registry.registerBeanDefinition(page.path, definition)
        }
        logger.info("Registered ${generatedPages.size} page handlers")
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(PageRouteRegistrar::class)
annotation class EnablePages