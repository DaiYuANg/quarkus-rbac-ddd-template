package com.github.DaiYuANg.modules.contracttests.commonmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PageQueryCompatibilityTest {

  @Test
  void legacyPageNumAndPageSizeStillDrivePaging() {
    var query = new ApiPageQuery();
    query.setPageNum(3);
    query.setPageSize(25);

    assertEquals(3, query.getPageNum());
    assertEquals(3, query.getPage());
    assertEquals(25, query.getPageSize());
    assertEquals(25, query.getSize());
    assertEquals(50, query.offset());
  }

  @Test
  void zeroBasedPageAliasMapsBackToToolkitPageRequestSemantics() {
    var query = new ApiPageQuery();
    query.setPage(2);
    query.setSize(20);

    assertEquals(3, query.getPageNum());
    assertEquals(3, query.getPage());
    assertEquals(20, query.getPageSize());
    assertEquals(20, query.getSize());
    assertEquals(40, query.offset());
    assertEquals(2, query.pageIndex());
  }
}
