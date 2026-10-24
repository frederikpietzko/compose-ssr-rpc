package dev.frederikpietzko.example.rpc.plumbing

import kotlinx.rpc.annotations.Rpc
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanNameGenerator
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.util.ClassUtils
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KrpcService

class RpcBeanScanner(registry: BeanDefinitionRegistry) : ClassPathBeanDefinitionScanner(registry) {
    init {
        addIncludeFilter(AnnotationTypeFilter(KrpcService::class.java))
    }
}

class RpcServiceRegistrar : ImportBeanDefinitionRegistrar {
    companion object {
        private val logger = LoggerFactory.getLogger(RpcServiceRegistrar::class.java)
    }

    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
        importBeanNameGenerator: BeanNameGenerator
    ) {
        val attributes = AnnotationAttributes.fromMap(
            importingClassMetadata.getAnnotationAttributes(EnableRpc::class.java.name)
        )
        if (attributes == null) return

        var basePackages = when (val rawPackages = attributes["basePackages"]) {
            is Array<*> -> rawPackages.map { it.toString() }.toTypedArray()
            is Iterable<*> -> rawPackages.map { it.toString() }.toTypedArray()
            else -> emptyArray()
        }
        if (basePackages.isEmpty()) {
            basePackages = arrayOf(ClassUtils.getPackageName(importingClassMetadata.className))
        }

        val scanner = RpcBeanScanner(registry)
        val rpcServicesCount = scanner.scan(*basePackages)
        logger.info("Registered $rpcServicesCount RPC services")
    }

}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(RpcServiceRegistrar::class)
annotation class EnableRpc(
    val basePackages: Array<String> = []
)

data class RpcBeanDefinitions<@Rpc Service : Any>(
    val rpcServices: MutableList<Pair<KClass<Service>, Service>> = mutableListOf()
)

@Configuration
class RpcBeanDefinitionsConfiguration<@Rpc Service : Any> {
    @Bean
    fun rpcBeanDefinitions(context: ApplicationContext): RpcBeanDefinitions<Service> {
        val beanList = context.getBeansWithAnnotation(Rpc::class.java)
        return RpcBeanDefinitions(
            beanList.map { it.value::class.superclasses.first() to it.value } as MutableList<Pair<KClass<Service>, Service>>
        )
    }
}