# CI/CD 示例

[English](CI_CD_EXAMPLES.md)

这里故意只提供 **示例**，不把仓库绑定到某一种 CI 平台。你可以把同样的阶段翻译到
GitHub Actions、GitLab CI、Jenkins 或内部流水线。

## 建议的最小基线

1. 所有合并请求执行 `./gradlew assemble test --no-daemon`
2. 主分支在质量门禁稳定后执行 `./gradlew build --no-daemon`
3. 只为当前要发布的应用构建镜像：`admin-api`、`mobile-api` 或 `migrator`
4. 先运行 `migrator` 做 Flyway，再发布 API

GitHub Actions 示例见
[docs/examples/ci/github-actions-ci.yml](examples/ci/github-actions-ci.yml)。它只是翻译样例，不代表仓库强绑定 GitHub。
