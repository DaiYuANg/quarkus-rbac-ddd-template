package com.github.DaiYuANg.modules.contracttests.commonmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.DaiYuANg.modules.accesscontrol.query.UserPageQueryParams;
import org.junit.jupiter.api.Test;

class PageQueryCompatibilityTest {

  @Test
  void zeroBasedPageAndSizeDriveToolkitPaging() {
    var params = new UserPageQueryParams();
    params.setPage(2);
    params.setSize(25);
    var query = params.toQuery();

    assertEquals(3, query.getPage());
    assertEquals(25, query.getSize());
    assertEquals(50, query.offset());
  }

  @Test
  void zeroBasedPageAliasMapsBackToToolkitPageRequestSemantics() {
    var params = new UserPageQueryParams();
    params.setPage(2);
    params.setSize(20);
    var query = params.toQuery();

    assertEquals(3, query.getPage());
    assertEquals(20, query.getSize());
    assertEquals(40, query.offset());
    assertEquals(2, query.pageIndex());
  }
}
