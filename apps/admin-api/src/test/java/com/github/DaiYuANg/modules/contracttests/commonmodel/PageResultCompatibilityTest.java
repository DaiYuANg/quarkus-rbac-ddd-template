package com.github.DaiYuANg.modules.contracttests.commonmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultCompatibilityTest {

  @Test
  void exposesContractAliasesForFrontend() {
    var page = ApiPageResult.of(10L, 2, 25, List.of("a", "b"));

    assertEquals(List.of("a", "b"), page.items());
    assertEquals(2, page.page());
    assertEquals(25, page.pageSizeAlias());
  }
}
