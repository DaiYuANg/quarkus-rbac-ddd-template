package com.github.DaiYuANg.modules.example;

import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.modules.example.application.command.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.port.in.ExampleProductCatalogApi;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;
import com.github.DaiYuANg.modules.security.runtime.replay.ReplayProtected;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.toolkit4j.data.model.envelope.Result;

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
  public Result<String, ExampleProductView> create(@Valid CreateExampleProductCommand body) {
    return Results.ok(productCatalog.create(body));
  }

  @GET
  public Result<String, List<ExampleProductView>> list() {
    return Results.ok(productCatalog.listActive());
  }
}
