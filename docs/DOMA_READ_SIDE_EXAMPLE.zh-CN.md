# Doma 读侧示例

[English](DOMA_READ_SIDE_EXAMPLE.md)

这个模板建议继续让 Hibernate/Panache 承担写侧，Doma 作为 **读侧实现选项** 引入。这样最符合现在的
CQRS-lite 方向：REST 契约和应用查询对象不变，查询实现可以替换。

Quarkus 侧已经有现成集成：

- https://quarkus.io/extensions/io.quarkiverse.doma/quarkus-doma/
- https://docs.quarkiverse.io/quarkus-doma/dev/index.html

按 Quarkus 扩展页显示，到了 **2026-02-01**，最新版本是 `1.0.8`，状态是 `preview`。

建议放置方式：

- `application/readmodel` 放返回给调用方的投影
- `application/port/driven/*ReadRepository` 放查询边界
- `infrastructure/persistence/doma` 放 DAO 和 SQL 文件

示例代码见 [docs/examples/doma](examples/doma) 目录。这些文件是文档级样例，默认不接入运行时。
