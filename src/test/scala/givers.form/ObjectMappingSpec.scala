package givers.form

import givers.form.Mapping.ErrorSpec
import givers.form.helpers.{BaseSpec, Tuples}
import play.api.libs.json.*
import utest.{Tests, *}

import scala.util.{Failure, Success}


object ObjectMappingSpec extends BaseSpec {
  val tests = Tests {
    test("ObjectMapping") {
      test("converts") {
        case class TestObj(a: String, b: Int, c: NestedObj)
        case class NestedObj(num: Int)
        val mapping = Mappings.obj(
          TestObj.apply,
          Tuples.to[TestObj],
          "a" -> Mappings.text(allowEmpty = false),
          "b" -> Mappings.number(min = 10),
          "c" -> Mappings.obj(
            NestedObj.apply,
            Tuples.to[NestedObj] andThen {a => a.map(_._1)},
            "num" -> Mappings.number
          )
        )

        test("lists all errors") {
          println(mapping.getAllErrors() == Set(
            ErrorSpec("a.error.required"),
            ErrorSpec("b.error.required"),
            ErrorSpec("b.error.min", 1),
            ErrorSpec("b.error.number"),
            ErrorSpec("c.num.error.required"),
            ErrorSpec("c.num.error.number"),
          ))
        }

        test("succeeds") {
          val json = Json.obj("a" -> "value", "b" -> "11", "c" -> Json.obj("num" -> "12"))
          assert(mapping.bind(JsDefined(json), BindContext.empty) == Success(TestObj("value", 11, NestedObj(12))))
        }

        test("fails") {
          val expected = new ValidationException(Seq(
            new ValidationMessage("a.error.required"),
            new ValidationMessage("b.error.min", 10),
            new ValidationMessage("c.num.error.number")
          ))
          val json = Json.obj("a" -> "", "b" -> "9", "c" -> Json.obj("num" -> "nan"))
          assert(mapping.bind(JsDefined(json), BindContext.empty) == Failure(expected))
        }

        test("partially fails") {
          test - {
            val json = Json.obj("a" -> "", "b" -> "11", "c" -> Json.obj("num" -> "12"))
            val expected = new ValidationException(Seq(new ValidationMessage("a.error.required")))
            assert(mapping.bind(JsDefined(json), BindContext.empty) == Failure(expected))
          }

          test - {
            val json = Json.obj("a" -> "v", "b" -> "9", "c" -> Json.obj("num" -> "12"))
            val expected = new ValidationException(Seq(new ValidationMessage("b.error.min", 10)))
            assert(mapping.bind(JsDefined(json), BindContext.empty) == Failure(expected))
          }

          test - {
            val json = Json.obj("a" -> "v", "b" -> "19", "c" -> Json.obj("num" -> ""))
            val expected = new ValidationException(Seq(new ValidationMessage("c.num.error.number")))
            assert(mapping.bind(JsDefined(json), BindContext.empty) == Failure(expected))
          }
        }
      }
    }
  }
}
