with open("app/src/main/java/dev/sonle/androidbasearchitecture/data/repository/UserRepositoryImpl.kt", "r") as f:
    content = f.read()

content = content.replace("kotlinx.coroutines.flow.emitAll(", "emitAll(")

with open("app/src/main/java/dev/sonle/androidbasearchitecture/data/repository/UserRepositoryImpl.kt", "w") as f:
    f.write(content)
