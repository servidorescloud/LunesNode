package io.lunes.lang

import io.lunes.lang.directives.Directive

trait ExprCompiler extends Versioned {
  def compile(input: String,
              directives: List[Directive]): Either[String, version.ExprT]
}
