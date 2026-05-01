with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

import re
content = re.sub(r'kotlinx-serialization-json = \{ group = \\"org\.jetbrains\.kotlinx\\", name = \\"kotlinx-serialization-json\\", version = \\"1\.7\.3\\" \}', 'kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.7.3" }', content)

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)
