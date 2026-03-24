package com.github.DaiYuANg.modules.example;

import com.github.DaiYuANg.common.model.Result;
import com.github.DaiYuANg.modules.example.application.port.in.ExampleOrderPlacementApi;
import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderView;
import com.github.DaiYuANg.modules.example.application.dto.PlaceExampleOrderCommand;
import com.github.DaiYuANg.modules.security.runtime.replay.ReplayProtected;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** DDD sample: place order as the current user; buyer must exist in {@code sys_user}. */
@Path("/api/v1/example/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@ReplayProtected
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExampleOrderResource {

  private final ExampleOrderPlacementApi orderPlacement;

  @POST
  public Result<ExampleOrderView> place(@Valid PlaceExampleOrderCommand body) {
    return Result.ok(orderPlacement.placeOrder(body));
  }

  @GET
  @Path("/mine")
  public Result<List<ExampleOrderView>> mine() {
    return Result.ok(orderPlacement.myOrders());
  }
}
