with open("app/src/main/java/dev/sonle/androidbasearchitecture/core/di/AppModule.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import dev.sonle.androidbasearchitecture.core.ai.GeminiNanoManager
import dev.sonle.androidbasearchitecture.core.ai.OnDeviceAiManager
import dev.sonle.androidbasearchitecture.domain.usecase.ai.SummarizeUserUseCase
import org.koin.android.ext.koin.androidContext
"""

content = content.replace("import org.koin.dsl.module", imports + "import org.koin.dsl.module")

# Add DI configurations
di_config = """
    // AI
    single<OnDeviceAiManager> { GeminiNanoManager(androidContext()) }
    factoryOf(::SummarizeUserUseCase)

    // ViewModels"""

content = content.replace("    // ViewModels", di_config)

with open("app/src/main/java/dev/sonle/androidbasearchitecture/core/di/AppModule.kt", "w") as f:
    f.write(content)

