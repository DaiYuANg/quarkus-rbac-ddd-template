package com.github.DaiYuANg.modules.example.infrastructure.persistence.doma;

import java.time.Instant;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;

@Entity(immutable = true)
public record ExampleOrderSummaryRow(
    @Id Long id, String buyerUsername, String status, long totalMinor, Instant createAt) {}
