# Qute + Infinispan 动态查询示例

本示例演示如何使用 **Quarkus Qute** 模板引擎 + **Infinispan Ickle** 查询语言实现类似 MyBatis 的动态查询。

## 核心组件

| 组件 | 说明 |
|------|------|
| `Book` | 带 `@Indexed` 的可查询实体 |
| `BookSchema` | ProtoStream 序列化 schema |
| `templates/queries/book-search.txt` | Qute 模板，按参数条件生成 WHERE 子句 |
| `BookSearchParams` | 查询参数 DTO |
| `BookSearchService` | 模板渲染 + Infinispan 执行 |
| `BookQueryExampleResource` | REST 接口 |

## 使用方式

1. **启动应用**（需 Infinispan，Docker 环境下 Dev Services 会自动启动）：
   ```bash
   ./gradlew :apps:admin-api:quarkusDev
   ```

2. **注入示例数据**：
   ```bash
   curl -X POST http://localhost:8080/example/books/bootstrap
   ```

3. **动态查询**：
   ```bash
   # 无参数：返回全部
   curl "http://localhost:8080/example/books/search"

   # 关键词搜索
   curl "http://localhost:8080/example/books/search?keyword=Java"

   # 作者精确匹配
   curl "http://localhost:8080/example/books/search?author=Joshua%20Bloch"

   # 年份范围
   curl "http://localhost:8080/example/books/search?minYear=2020&maxYear=2025"
   ```

4. **查看生成的 Ickle 查询**：
   ```bash
   curl "http://localhost:8080/example/books/search/render?keyword=java&minYear=2020"
   ```

## Qute 模板示例

```txt
FROM example.query.Book b WHERE 1=1
{#if keyword != null && keyword.length > 0}
  AND (b.title LIKE :keyword OR b.description LIKE :keyword OR b.author LIKE :keyword)
{/if}
{#if author != null && author.length > 0}
  AND b.author = :author
{/if}
...
```

参数通过 `query.setParameter()` 绑定，避免注入风险。
