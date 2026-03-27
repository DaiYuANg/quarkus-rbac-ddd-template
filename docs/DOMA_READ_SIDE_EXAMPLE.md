# Doma Read-Side Example

[中文](DOMA_READ_SIDE_EXAMPLE.zh-CN.md)

This repository keeps Hibernate/Panache on the write side and treats Doma as a **read-side option**.
That matches the CQRS-lite direction: resource contracts and application query objects stay stable
while the query implementation can switch.

## Quarkus integration

Quarkus already has a Doma integration via Quarkiverse:

- Quarkus extension page: https://quarkus.io/extensions/io.quarkiverse.doma/quarkus-doma/
- Quarkiverse docs: https://docs.quarkiverse.io/quarkus-doma/dev/index.html

As of **February 1, 2026**, the latest extension release listed by the Quarkus registry is
`1.0.8`, with status `preview`.

## Suggested placement

Keep the Doma implementation on the **read side** only:

- `application/readmodel` for the projection returned to callers
- `application/port/driven/*ReadRepository` for the query boundary
- `infrastructure/persistence/doma` for DAO + SQL files

## Example layout

See the sample files under [docs/examples/doma](examples/doma):

- [ExampleOrderSummaryRow.java](examples/doma/ExampleOrderSummaryRow.java)
- [ExampleOrderDomaDao.java](examples/doma/ExampleOrderDomaDao.java)
- [selectByBuyer.sql](examples/doma/selectByBuyer.sql)

These files are intentionally documentation examples, not active runtime wiring. When you decide to
enable Doma in a real context, add the Quarkiverse extension to the target app/module and wire the
DAO behind an existing `*ReadRepository` port.
