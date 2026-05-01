import re

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/login/LoginScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onEmailChange = viewModel::onEmailChange,", "onEmailChange = { viewModel.onAction(LoginAction.EmailChanged(it)) },")
content = content.replace("onPasswordChange = viewModel::onPasswordChange,", "onPasswordChange = { viewModel.onAction(LoginAction.PasswordChanged(it)) },")
content = content.replace("onLoginClick = viewModel::onLoginClick", "onLoginClick = { viewModel.onAction(LoginAction.LoginClicked) }")

with open("app/src/main/java/dev/sonle/androidbasearchitecture/presentation/features/login/LoginScreen.kt", "w") as f:
    f.write(content)
