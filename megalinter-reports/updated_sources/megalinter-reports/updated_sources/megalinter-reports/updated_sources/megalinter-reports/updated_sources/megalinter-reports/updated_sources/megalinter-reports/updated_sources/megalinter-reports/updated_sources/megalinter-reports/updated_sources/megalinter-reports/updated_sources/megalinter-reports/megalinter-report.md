## ✅⚠️[MegaLinter](https://megalinter.io/9.0.1) analysis: Success with warnings



| Descriptor  |                                               Linter                                                |Files|Fixed|Errors|Warnings|Elapsed time|
|-------------|-----------------------------------------------------------------------------------------------------|----:|----:|-----:|-------:|-----------:|
|✅ DOCKERFILE|[hadolint](https://megalinter.io/9.0.1/descriptors/dockerfile_hadolint)                              |   25|     |     0|       0|       0.84s|
|✅ JAVA      |[checkstyle](https://megalinter.io/9.0.1/descriptors/java_checkstyle)                                |   92|     |     0|       0|       2.88s|
|✅ JAVA      |[pmd](https://megalinter.io/9.0.1/descriptors/java_pmd)                                              |   92|     |     0|       0|        4.7s|
|✅ JSON      |[prettier](https://megalinter.io/9.0.1/descriptors/json_prettier)                                    |   43|    1|     0|       0|       0.53s|
|✅ JSON      |[v8r](https://megalinter.io/9.0.1/descriptors/json_v8r)                                              |   43|     |     0|       0|      12.17s|
|⚠️ MARKDOWN  |[markdownlint](https://megalinter.io/9.0.1/descriptors/markdown_markdownlint)                        |   34|   10|    92|       0|       2.13s|
|✅ MARKDOWN  |[markdown-table-formatter](https://megalinter.io/9.0.1/descriptors/markdown_markdown_table_formatter)|   34|   10|     0|       0|       0.32s|
|✅ REPOSITORY|[gitleaks](https://megalinter.io/9.0.1/descriptors/repository_gitleaks)                              |  yes|     |    no|      no|       1.08s|
|✅ REPOSITORY|[git_diff](https://megalinter.io/9.0.1/descriptors/repository_git_diff)                              |  yes|     |    no|      no|       0.03s|
|✅ REPOSITORY|[secretlint](https://megalinter.io/9.0.1/descriptors/repository_secretlint)                          |  yes|     |    no|      no|       3.02s|
|✅ REPOSITORY|[syft](https://megalinter.io/9.0.1/descriptors/repository_syft)                                      |  yes|     |    no|      no|       1.66s|
|✅ REPOSITORY|[trufflehog](https://megalinter.io/9.0.1/descriptors/repository_trufflehog)                          |  yes|     |    no|      no|       2.46s|
|✅ XML       |[xmllint](https://megalinter.io/9.0.1/descriptors/xml_xmllint)                                       |   39|    0|     0|       0|        5.3s|
|✅ YAML      |[prettier](https://megalinter.io/9.0.1/descriptors/yaml_prettier)                                    |   18|    0|     0|       0|       0.92s|
|✅ YAML      |[v8r](https://megalinter.io/9.0.1/descriptors/yaml_v8r)                                              |   18|     |     0|       0|       4.69s|
|✅ YAML      |[yamllint](https://megalinter.io/9.0.1/descriptors/yaml_yamllint)                                    |   18|     |     0|       0|        1.3s|

## Detailed Issues

<details>
<summary>⚠️ MARKDOWN / markdownlint - 92 errors</summary>

```
AvroToJson/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
AvroToJson/README.md:223 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
DockerComposeExamples/RunMultipleFlinkJobs/README.md:37:401 MD013/line-length Line length [Expected: 400; Actual: 451]
EnrichStream/README.md:76 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
EnrichStream/README.md:83 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
EnrichStream/README.md:134 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
ExternalLookup/README.md:3:197 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
ExternalLookup/README.md:76 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
ExternalLookup/README.md:82 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
ExternalLookup/README.md:133 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
FlinkStates/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
FlinkStates/README.md:76 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
FlinkStates/README.md:82 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
FlinkStates/README.md:133 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
JsonToAvro/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
JsonToAvro/README.md:141 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
JsonToAvro/README.md:151 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
KeySerializationSchema/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
KeySerializationSchema/README.md:97 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
KeySerializationSchema/README.md:106 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
KeySerializationSchema/README.md:170 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/EnrichStream/README.md:76 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/EnrichStream/README.md:83 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/EnrichStream/README.md:134 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
megalinter-reports/updated_sources/ExternalLookup/README.md:3:197 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
megalinter-reports/updated_sources/ExternalLookup/README.md:76 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/ExternalLookup/README.md:82 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/ExternalLookup/README.md:133 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
megalinter-reports/updated_sources/FlinkStates/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
megalinter-reports/updated_sources/FlinkStates/README.md:76 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/FlinkStates/README.md:82 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/FlinkStates/README.md:133 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
megalinter-reports/updated_sources/JsonToAvro/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
megalinter-reports/updated_sources/JsonToAvro/README.md:141 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/JsonToAvro/README.md:151 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/KeySerializationSchema/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
megalinter-reports/updated_sources/KeySerializationSchema/README.md:97 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/KeySerializationSchema/README.md:106 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/KeySerializationSchema/README.md:170 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:1 MD041/first-line-heading/first-line-h1 First line in a file should be a top-level heading [Context: "## ✅⚠️[MegaLinter](https://meg..."]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:29 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:85:401 MD013/line-length Line length [Expected: 400; Actual: 458]
megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/updated_sources/megalinter-reports/megalinter-report.md:86:401 MD013/line-length Line length [Expected: 400; Actual: 418]
megalinter-reports/updated_sources/TransformAndStore/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
megalinter-reports/updated_sources/TransformAndStore/README.md:98 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/TransformAndStore/README.md:109 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
megalinter-reports/updated_sources/TransformAndStore/README.md:176 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
MultipleSideOutput/README.md:3:197 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
Observability/README.md:3:227 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
SerializationErrorCatch/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
SerializationErrorSideOutput/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
TransformAndStore/README.md:3:196 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
TransformAndStore/README.md:98 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
TransformAndStore/README.md:109 MD040/fenced-code-language Fenced code blocks should have a language specified [Context: "```"]
TransformAndStore/README.md:176 MD025/single-title/single-h1 Multiple top-level headings in the same document [Context: "Implementing a new Flink job"]
TumblingWindow/README.md:3:197 MD059/descriptive-link-text Link text should be descriptive [Context: "[here]"]
```

</details>

See detailed reports in MegaLinter artifacts


Your project could benefit from a custom flavor, which would allow you to run only the linters you need, and thus improve runtime performances. (Skip this info by defining `FLAVOR_SUGGESTIONS: false`)

  - Documentation: [Custom Flavors](https://megalinter.io/9.0.1/custom-flavors/)
  - Command: `npx mega-linter-runner@9.0.1 --custom-flavor-setup --custom-flavor-linters DOCKERFILE_HADOLINT,JAVA_CHECKSTYLE,JAVA_PMD,JSON_V8R,JSON_PRETTIER,MARKDOWN_MARKDOWNLINT,MARKDOWN_MARKDOWN_TABLE_FORMATTER,REPOSITORY_GIT_DIFF,REPOSITORY_GITLEAKS,REPOSITORY_SECRETLINT,REPOSITORY_SYFT,REPOSITORY_TRUFFLEHOG,XML_XMLLINT,YAML_PRETTIER,YAML_YAMLLINT,YAML_V8R`

[![MegaLinter is graciously provided by OX Security](https://raw.githubusercontent.com/oxsecurity/megalinter/main/docs/assets/images/ox-banner.png)](https://www.ox.security/?ref=megalinter)