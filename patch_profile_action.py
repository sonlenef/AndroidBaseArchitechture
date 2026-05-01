with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileAction.kt", "r") as f:
    content = f.read()

content = content.replace("object NavigateBack : ProfileAction\n}", "object NavigateBack : ProfileAction\n    object SummarizeProfile : ProfileAction\n}")

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/profile/ProfileAction.kt", "w") as f:
    f.write(content)

