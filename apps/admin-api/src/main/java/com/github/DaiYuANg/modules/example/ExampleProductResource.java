package com.github.DaiYuANg.modules.example;

import com.github.DaiYuANg.common.model.Result;
import com.github.DaiYuANg.modules.example.application.dto.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.dto.ExampleProductView;
import com.github.DaiYuANg.modules.example.application.port.in.ExampleProductCatalogApi;
import com.github.DaiYuANg.modules.security.runtime.replay.ReplayProtected;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** DDD sample: driving adapter for the example catalog. Wire your own permissions in production. */
@Path("/api/v1/example/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@ReplayProtected
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExampleProductResource {

  private final ExampleProductCatalogApi productCatalog;

  @POST
  public Result<ExampleProductView> create(@Valid CreateExampleProductCommand body) {
    return Result.ok(productCatalog.create(body));
  }

  @GET
  public Result<List<ExampleProductView>> list() {
    return Result.ok(productCatalog.listActive());
  }
}
