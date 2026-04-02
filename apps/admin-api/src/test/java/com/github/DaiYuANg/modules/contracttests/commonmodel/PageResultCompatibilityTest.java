package com.github.DaiYuANg.modules.contracttests.commonmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.toolkit4j.data.model.page.PageResult;

class PageResultCompatibilityTest {

  @Test
  void usesToolkitPageResultFields() {
    var page = new PageResult<>(List.of("a", "b"), 2, 25, 10L, 1L);

    assertEquals(List.of("a", "b"), page.getContent());
    assertEquals(2, page.getPage());
    assertEquals(25, page.getSize());
  }
}
