package givers.form

import givers.form.Mapping.ErrorSpec
import givers.form.helpers.{BaseSpec, Tuples}
import play.api.libs.json.*
import utest.Tests
import utest.*

import scala.util.{Failure, Success}


object MappingsSpec extends BaseSpec {
  val tests = Tests {
    test("boolean") {
      test("binds/unbinds boolean") {
        assert(Mappings.boolean.bind(JsDefined(JsBoolean(true)), BindContext.empty) == Success(true))
        assert(Mappings.boolean.unbind(true, UnbindContext.empty) == JsBoolean(true))
        assert(Mappings.boolean.bind(JsDefined(JsBoolean(false)), BindContext.empty) == Success(false))
        assert(Mappings.boolean.unbind(false, UnbindContext.empty) == JsBoolean(false))
      }

      test("binds string") {
        assert(Mappings.boolean.bind(JsDefined(JsString("true")), BindContext.empty) == Success(true))
        assert(Mappings.boolean.bind(JsDefined(JsString("false")), BindContext.empty) == Success(false))
      }

      test("binds missing") {
        assert(Mappings.boolean(translateAbsenceToFalse = true).bind(JsDefined(JsNull), BindContext.empty) == Success(false))
        assert(Mappings.boolean(translateAbsenceToFalse = true).bind(JsUndefined(""), BindContext.empty) == Success(false))
      }

      test("binds invalid") {
        assert(Mappings.boolean.bind(JsUndefined(""), BindContext.empty) == Failure(Mapping.error("error.required")))
        assert(Failure(Mapping.error("error.boolean")) == Mappings.boolean.bind(JsDefined(JsString("random")), BindContext.empty))
        assert(Failure(Mapping.error("error.boolean")) == Mappings.boolean.bind(JsDefined(JsNumber(100L)), BindContext.empty))
      }

      test("all errors") {
        assert(Mappings.boolean.getAllErrors() == Set(ErrorSpec("error.boolean"), ErrorSpec("error.required")))
      }
    }

    test("email") {
      test("binds/unbind string") {
        assert(Mappings.email.bind(JsDefined(JsString(" a@b ")), BindContext.empty) == Success("a@b"))
      }

      test("binds invalid") {
        assert(Mappings.email.bind(JsDefined(JsString(" ab ")), BindContext.empty) == Failure(Mapping.error("error.email")))
        assert(Mappings.email.bind(JsDefined(JsString("")), BindContext.empty) == Failure(Mapping.error("error.email")))
      }

      test("all errors") {
        assert(Mappings.email.getAllErrors() == Set(ErrorSpec("error.email")))
      }
    }

    test("text (not trimmed, allows empty)") {
      test("binds/unbinds string") {
        assert(Mappings.text(trim = false).bind(JsDefined(JsString("")), BindContext.empty) == Success(""))
        assert(Mappings.text(trim = false).bind(JsDefined(JsString(" txt ")), BindContext.empty) == Success(" txt "))
        assert(Mappings.text(trim = false).unbind(" txt ", UnbindContext.empty) == JsString(" txt "))
      }

      test("binds coerce") {
        assert(Mappings.text().bind(JsDefined(JsBoolean(true)), BindContext.empty) == Success("true"))
        assert(Mappings.text().bind(JsDefined(JsNumber(100L)), BindContext.empty) == Success("100"))
        assert(Mappings.text().bind(JsDefined(JsArray(Seq(JsNumber(100L)))), BindContext.empty) == Success("[100]"))
        assert(Mappings.text().bind(JsDefined(Json.obj("a" -> 123)), BindContext.empty) == Success("""{"a":123}"""))
      }

      test("binds invalid") {
        assert(Mappings.text(coerceToString = false).bind(JsDefined(JsBoolean(true)), BindContext.empty) == Failure(Mapping.error("error.invalid")))
        assert(Mappings.text(coerceToString = false).bind(JsDefined(JsNumber(100L)), BindContext.empty) == Failure(Mapping.error("error.invalid")))
        assert(Mappings.text(coerceToString = false).bind(JsDefined(JsArray(Seq(JsNumber(100L)))), BindContext.empty) == Failure(Mapping.error("error.invalid")))
        assert(Mappings.text(coerceToString = false).bind(JsDefined(Json.obj("a" -> 123)), BindContext.empty) == Failure(Mapping.error("error.invalid")))
      }

      test("all errors") {
        assert(Mappings.text().getAllErrors() == Set.empty)
        assert(Mappings.text(coerceToString = false).getAllErrors() == Set(ErrorSpec("error.invalid")))
      }
    }

    test("text (trimmed, limit length)") {
      test("binds/unbinds string") {
        assert(Mappings.text(maxLength = 3).bind(JsDefined(JsString(" txt ")), BindContext.empty) == Success("txt"))
        assert(Mappings.text(maxLength = 3).unbind(" txt ", UnbindContext.empty) == JsString(" txt "))
      }

      test("binds invalid") {
        assert(Mappings.text(maxLength = 3).bind(JsDefined(JsString("aaaa")), BindContext.empty) == Failure(Mapping.error("error.maxLength", 3)))
      }

      test("all errors") - {
        assert(Mappings.text(maxLength = 3).getAllErrors() == Set(ErrorSpec("error.maxLength", 1)))
      }
    }

    test("text (trimmed, forbids empty)") {
      test("binds/unbinds string") {
        assert(Mappings.text(allowEmpty = false).bind(JsDefined(JsString(" txt ")), BindContext.empty) == Success("txt"))
        assert(Mappings.text(allowEmpty = false).unbind(" txt ", UnbindContext.empty) == JsString(" txt "))
      }

      test("binds empty") {
        assert(Mappings.text(allowEmpty = false).bind(JsDefined(JsString("  ")), BindContext.empty) == Failure(Mapping.error("error.required")))
      }

      test("all errors") {
        assert(Mappings.text(allowEmpty = false).getAllErrors() == Set(ErrorSpec("error.required")))
      }
    }

    test("long") {
      test("binds/unbinds number") {
        assert(Mappings.longNumber(min = 99, max = 101).bind(JsDefined(JsNumber(100)), BindContext.empty) == Success(100L))
        assert(Mappings.longNumber().bind(JsDefined(JsNumber(100)), BindContext.empty) == Success(100L))
        assert(Mappings.longNumber().unbind(100L, UnbindContext.empty) == JsNumber(BigDecimal(100L)))
      }

      test("binds string") {
        assert(Mappings.longNumber().bind(JsDefined(JsString("100")), BindContext.empty) == Success(100L))
        assert(Mappings.longNumber().bind(JsDefined(JsString("-100")), BindContext.empty) == Success(-100L))
      }

      test("validates") {
        assert(Mappings.longNumber(min = 10L).bind(JsDefined(JsNumber(9L)), BindContext.empty) == Failure(Mapping.error("error.min", 10L)))
        assert(Mappings.longNumber(max = 10L).bind(JsDefined(JsNumber(11L)), BindContext.empty) == Failure(Mapping.error("error.max", 10L)))
      }

      test("all errors") {
        assert(Mappings.longNumber().getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required")))
        assert(Mappings.longNumber(min = 10L).getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required"), ErrorSpec("error.min", 1)))
        assert(Mappings.longNumber(max = 10L).getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required"), ErrorSpec("error.max", 1)))
      }

      test("binds invalid") {
        assert(Mappings.longNumber().bind(JsDefined(JsBoolean(true)), BindContext.empty) == Failure(Mapping.error("error.number")))
        assert(Mappings.longNumber().bind(JsDefined(JsString("aaa")), BindContext.empty) == Failure(Mapping.error("error.number")))
      }

      test("all errors") {
        assert(Mappings.longNumber().getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required")))
        assert(Mappings.longNumber(min = 10L).getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required"), ErrorSpec("error.min", 1)))
        assert(Mappings.longNumber(max = 10L).getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required"), ErrorSpec("error.max", 1)))
      }
    }

    test("int") {
      test("binds/unbinds number") {
        assert(Mappings.number(min = 99, max = 101).bind(JsDefined(JsNumber(100)), BindContext.empty) == Success(100))
        assert(Mappings.number().bind(JsDefined(JsNumber(100)), BindContext.empty) == Success(100))
        assert(Mappings.number().unbind(100, UnbindContext.empty) == JsNumber(100))
      }

      test("binds string") {
        assert(Mappings.number().bind(JsDefined(JsString("100")), BindContext.empty) == Success(100))
        assert(Mappings.number().bind(JsDefined(JsString("-100")), BindContext.empty) == Success(-100))
      }

      test("validates") {
        assert(Mappings.number(min = 10).bind(JsDefined(JsNumber(9)), BindContext.empty) == Failure(Mapping.error("error.min", 10)))
        assert(Mappings.number(max = 10).bind(JsDefined(JsNumber(11)), BindContext.empty) == Failure(Mapping.error("error.max", 10)))
      }

      test("binds invalid") {
        assert(Mappings.number().bind(JsDefined(JsBoolean(true)), BindContext.empty) == Failure(Mapping.error("error.number")))
        assert(Mappings.number().bind(JsDefined(JsString("aaa")), BindContext.empty) == Failure(Mapping.error("error.number")))
      }

      test("all errors") {
        assert(Mappings.number().getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required")))
        assert(Mappings.number(min = 10).getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required"), ErrorSpec("error.min", 1)))
        assert(Mappings.number(max = 10).getAllErrors() == Set(ErrorSpec("error.number"), ErrorSpec("error.required"), ErrorSpec("error.max", 1)))
      }
    }

    test("optional") {
      test("binds/unbinds empty") {
        assert(Mappings.opt(Mappings.boolean).bind(JsUndefined(""), BindContext.empty) == Success(None))
        assert(Mappings.opt(Mappings.boolean).bind(JsDefined(JsNull), BindContext.empty) == Success(None))
        assert(Mappings.opt(Mappings.boolean).unbind(None, UnbindContext.empty) == JsNull)
      }

      test("binds an empty string") {
        // In Play's Form, an empty string is converted back to None.
        assert(Mappings.opt(Mappings.text, translateEmptyStringToNone = true).bind(JsDefined(JsString("")), BindContext.empty) == Success(None))
        // For the normal mode, we don't do that.
        assert(Mappings.opt(Mappings.text).bind(JsDefined(JsString("")), BindContext.empty) == Success(Some("")))
      }

      test("binds/unbinds value") {
        assert(Mappings.opt(Mappings.boolean).bind(JsDefined(JsBoolean(true)), BindContext.empty) == Success(Some(true)))
        assert(Mappings.opt(Mappings.boolean).unbind(Some(true), UnbindContext.empty) == JsBoolean(true))
      }
    }

    test("seq") {
      test("binds empty") {
        assert(Mappings.seq(Mappings.boolean, nonEmpty = true).bind(JsDefined(JsArray()), BindContext.empty) == Failure(Mapping.error("error.required")))
        assert(Mappings.seq(Mappings.boolean).bind(JsDefined(JsNull), BindContext.empty) == Failure(Mapping.error("error.required")))
        assert(Mappings.seq(Mappings.boolean).bind(JsUndefined(""), BindContext.empty) == Failure(Mapping.error("error.required")))
      }

      test("binds invalid element") {
        val result = Mappings.seq(Mappings.boolean).bind(
          value = JsDefined(
            JsArray(Seq(
              JsString("invalid"),
              JsString("true"),
              JsNumber(2),
              JsString("false")
            ))
          ),
          context = BindContext.empty
        )
        val expected = Failure(
          new ValidationException(Seq(
            new ValidationMessage("item.error.boolean", 1),
            new ValidationMessage("item.error.boolean", 3)
          ))
        )
        assert(result == expected)
      }

      test("binds/unbinds empty array") {
        assert(Mappings.seq(Mappings.boolean, translateNoneToEmpty = true).bind(JsDefined(JsNull), BindContext.empty) == Success(Seq.empty))
        assert(Mappings.seq(Mappings.boolean, translateNoneToEmpty = true).bind(JsUndefined(""), BindContext.empty) == Success(Seq.empty))
        assert(Mappings.seq(Mappings.boolean).bind(JsDefined(JsArray()), BindContext.empty) == Success(Seq.empty))
        assert(Mappings.seq(Mappings.boolean).unbind(Seq.empty, UnbindContext.empty) == JsArray())
      }

      test("binds/unbinds value") {
        assert(Mappings.seq(Mappings.boolean, nonEmpty = true).bind(JsDefined(JsArray(Seq(JsBoolean(true)))), BindContext.empty) == Success(Seq(true)))
        assert(Mappings.seq(Mappings.boolean).bind(JsDefined(JsArray(Seq(JsBoolean(true), JsBoolean(false)))), BindContext.empty) == Success(Seq(true, false)))
        assert(Mappings.seq(Mappings.boolean).unbind(Seq(true, false), UnbindContext.empty) == JsArray(Seq(JsBoolean(true), JsBoolean(false))))
      }

      test("all errors") {
        assert(Mappings.seq(Mappings.number(min = 10)).getAllErrors() == Set(
          ErrorSpec("error.required"),
          ErrorSpec("error.invalid"),
          ErrorSpec("item.error.required", 1),
          ErrorSpec("item.error.number", 1),
          ErrorSpec("item.error.min", 2),
        ))
      }
    }

    test("obj") {
      test("binds invalid") {
        case class Simple(b: Seq[Boolean])
        case class Complex(a: Seq[Simple])
        val m = Mappings.obj(
          Complex.apply,
          Tuples.to[Complex] andThen (_.map(_._1)),
          "a" -> Mappings.seq(Mappings.obj(
              Simple.apply,
              Tuples.to[Simple] andThen (_.map(_._1)),
              "b" -> Mappings.seq(Mappings.boolean)
            )
          )
        )
        val params = Json.obj(
          "a" -> Seq(Json.obj("b" -> Seq("invalid")), Json.obj("b" -> Seq("invalid", "invalid"))),
        )
        val expected = Failure(
          new ValidationException(Seq(
            new ValidationMessage("a.item.b.item.error.boolean", 1, 1),
            new ValidationMessage("a.item.b.item.error.boolean", 2, 1),
            new ValidationMessage("a.item.b.item.error.boolean", 2, 2)
          ))
        )
        assert(m.bind(JsDefined(params), BindContext.empty) == expected)
      }

      test("binds/unbinds single") {
        // A case class with one value is a special case because its unapply function doesn't return a tuple, so we explicitly test it.
        case class Obj(a: String)
        val m = Mappings.obj(
          Obj.apply,
          Tuples.to[Obj].andThen(_.map(_._1)),
          "a" -> Mappings.text()
        )
        val params = Json.obj("a" -> "value")
        val expected = Obj("value")
        assert(m.bind(JsDefined(params), BindContext.empty) == Success(expected))
        assert(m.unbind(expected, UnbindContext.empty) == params)
      }

      test("binds/unbinds simple") {
        case class Obj(a: String, n: Long)
        val m = Mappings.obj(
          Obj.apply,
          Tuples.to[Obj],
          "a" -> Mappings.text(),
          "n" -> Mappings.longNumber()
        )
        val params = Json.obj("a" -> "value", "n" -> 100L)
        val expected = Obj("value", 100)
        assert(m.bind(JsDefined(params), BindContext.empty) == Success(expected))
        assert(m.unbind(expected, UnbindContext.empty) == params)
      }

      test("binds/unbinds complex") {
        case class Simple(b: Boolean)
        case class Complex(a: Seq[Simple], nOpt: Option[Long], s: Simple)
        val m = Mappings.obj(
          Complex.apply,
          Tuples.to[Complex],
          "a" -> Mappings.seq(Mappings.obj(
            Simple.apply,
            Tuples.to[Simple].andThen(_.map(_._1)),
            "b" -> Mappings.boolean
          )),
          "n" -> Mappings.opt(Mappings.longNumber),
          "s" -> Mappings.obj(
            Simple.apply,
            Tuples.to[Simple].andThen(_.map(_._1)),
            "bb" -> Mappings.boolean
          )
        )
        val params = Json.obj(
          "a" -> Seq(Json.obj("b" -> false), Json.obj("b" -> true)),
          "n" -> 100L,
          "s" -> Json.obj("bb" -> false)
        )
        val expected = Complex(
          a = Seq(Simple(false), Simple(true)),
          nOpt = Some(100L),
          s = Simple(false)
        )
        assert(m.bind(JsDefined(params), BindContext.empty) == Success(expected))
        assert(m.unbind(expected, UnbindContext.empty) == params)
      }
    }
  }
}
