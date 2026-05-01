with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

# Remove the bad lines
content = content.replace("\n\n# Serialization\nkotlinx-serialization-json = { group = \"org.jetbrains.kotlinx\", name = \"kotlinx-serialization-json\", version = \"1.7.3\" }\n", "")

# Add it back into the [libraries] section
# Find the end of [libraries] block (which is before [plugins])
import re
content = re.sub(r"(\[plugins\])", r"kotlinx-serialization-json = { group = \"org.jetbrains.kotlinx\", name = \"kotlinx-serialization-json\", version = \"1.7.3\" }\n\n\1", content)

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)
