package dev.frederikpietzko.example.spring.plubming

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate

class PageProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val platforms: List<PlatformInfo>
) : SymbolProcessor {

    companion object {
        private const val PAGE_FQN = "dev.frederikpietzko.example.spring.plumbing.Page"
        private const val NO_STATE_FQN = "dev.frederikpietzko.example.spring.plumbing.NoState"
        private const val GENERATED_PACKAGE = "dev.frederikpietzko.example.spring.plumbing.generated"
    }


    private val pages = mutableListOf<KSFunctionDeclaration>()
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(PAGE_FQN)
        val (valid, deferred) = symbols.partition { it.validate() }
        valid.filterIsInstance<KSFunctionDeclaration>().forEach { fn ->
            if (fn.parentDeclaration != null) {
                logger.error("@Page must be top-level", fn)
                return@forEach
            }
            pages += fn
        }
        return deferred
    }

    override fun finish() {
        if (pages.isEmpty()) return

        val body = pages.joinToString(",\n") { fn ->
            val annotation = fn.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == PAGE_FQN
            }
            val path = annotation.arguments.first { it.name?.asString() == "path" }.value as String
            val provider = (annotation.arguments.firstOrNull { it.name?.asString() == "state" }?.value as? KSType)
                ?.declaration?.qualifiedName?.asString()
                ?: NO_STATE_FQN
            val function = fn.qualifiedName!!.asString()
            val stateType = fn.parameters.firstOrNull()?.type?.resolve()?.declaration?.qualifiedName?.asString()
            val render = if (stateType == null) "{ _: Any? -> $function() }"
            else "{ state: $stateType -> $function(state) }"
            "    PageDefinition(\"$path\", $provider::class, $render)"
        }

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, *pages.mapNotNull { it.containingFile }.toTypedArray()),
            packageName = GENERATED_PACKAGE,
            fileName = "GeneratedPages",
        )
        file.bufferedWriter().use {
            it.write(
                """
                |package $GENERATED_PACKAGE
                |
                |import dev.frederikpietzko.example.spring.plumbing.PageDefinition
                |
                |val generatedPages: List<PageDefinition<*>> = listOf(
                |$body
                |)
                |""".trimMargin()
            )
        }
    }
}

class PageProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        PageProcessor(environment.logger, environment.codeGenerator, environment.platforms)
}