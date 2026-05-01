import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/data/repository/UserRepositoryImpl.kt", "r") as f:
    content = f.read()

# Add import
content = content.replace("import kotlinx.coroutines.flow.flow", "import kotlinx.coroutines.flow.flow\nimport kotlinx.coroutines.flow.emitAll")

with open("app/src/main/java/dev/sonle/androidbasearchitecture/data/repository/UserRepositoryImpl.kt", "w") as f:
    f.write(content)
