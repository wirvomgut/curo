package models.common

case class Pagination(limit: Int = 9, offset: Int = 0) {
  def pages(itemsTotal: Long): Int = {
    Pagination.pages(limit, itemsTotal)
  }

  def currentPage: Int = {
    Pagination.currentPage(limit, offset)
  }
}

object Pagination {
  def pages(itemsPerPage: Int, itemsTotal: Long): Int = {
    math.ceil(itemsTotal.toDouble / itemsPerPage).toInt
  }

  def currentPage(itemsPerPage: Int, offset: Int): Int = {
    math.ceil(offset.toDouble / itemsPerPage).toInt
  }
}
