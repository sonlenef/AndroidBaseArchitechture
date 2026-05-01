import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/login/LoginScreen.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel.onEmailChange", "viewModel.onAction(LoginAction.EmailChanged")
content = content.replace("viewModel.onPasswordChange", "viewModel.onAction(LoginAction.PasswordChanged")
content = content.replace("viewModel.onLoginClick()", "viewModel.onAction(LoginAction.LoginClicked)")

# The above replacement for EmailChanged and PasswordChanged will result in 
# viewModel.onAction(LoginAction.EmailChanged(it)
# Need to close the bracket manually in regex
content = re.sub(r"viewModel\.onAction\(LoginAction\.EmailChanged\((.*?)\)", r"viewModel.onAction(LoginAction.EmailChanged(\1))", content)
content = re.sub(r"viewModel\.onAction\(LoginAction\.PasswordChanged\((.*?)\)", r"viewModel.onAction(LoginAction.PasswordChanged(\1))", content)

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/login/LoginScreen.kt", "w") as f:
    f.write(content)

