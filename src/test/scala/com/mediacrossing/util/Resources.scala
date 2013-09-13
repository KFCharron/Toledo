package com.mediacrossing.util

import scala.io.Source

trait Resources {

  def usingResource[R](resource: String)(f: Source => R): R = {
    val source = Source.fromInputStream(
      getClass
        .getClassLoader
        .getResourceAsStream(resource))

    try {
      f(source)
    } finally {
      source.close()
    }
  }
}
