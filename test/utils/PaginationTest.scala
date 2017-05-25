package utils

import models.common.Pagination
import org.scalatest.FunSuite

class PaginationTest extends FunSuite {

  test("pagination page test") {

    assert(Pagination.currentPage(9,9)   == 1)
    assert(Pagination.currentPage(9,2)   == 1)
    assert(Pagination.currentPage(9,0)   == 0)
    assert(Pagination.currentPage(9,10)  == 2)
  }

}
