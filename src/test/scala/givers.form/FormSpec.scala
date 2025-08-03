package givers.form

import givers.form.Mapping.ErrorSpec
import givers.form.helpers.{BaseSpec, Tuples}
import play.api.libs.json.*
import play.api.test.FakeRequest
import utest.{Tests, *}

import scala.util.{Failure, Success}


object FormSpec extends BaseSpec {

  case class TestObj(a: String, b: Int)
  val form = Form(
    "validation.form",
    TestObj.apply,
    Tuples.to[TestObj],
    "a" -> Mappings.text(allowEmpty = false),
    "b" -> Mappings.number()
  )
  val obj = TestObj("test", 123)
  val json = Json.obj("a" -> "test", "b" -> 123)

  val tests = Tests {
    test("Form") {
      test("bindFromRequest") {
        test("binds JsValue") {
          val req = FakeRequest("POST", "/").withBody(json)
          assert(form.bindFromRequest()(req) == Success(obj))
        }

        test("binds AnyContent") {
          val req = FakeRequest("POST", "/").withJsonBody(json)
          assert(form.bindFromRequest()(req) == Success(obj))
        }

        test("binds invalid") {
          test("textBody") {
            val req = FakeRequest("POST", "/").withTextBody("random")
            val ex = assertThrows[Exception](form.bindFromRequest()(req))
            assert(ex.getMessage == "Unable to parse body as json.")
          }

          test("invalidJson") {
            val req = FakeRequest("POST", "/").withBody(JsString("random"))
            assert(form.bindFromRequest()(req) == Failure(new ValidationException(Seq(
              new ValidationMessage("validation.form.a.error.required"),
              new ValidationMessage("validation.form.b.error.required")
            ))))
          }
        }
      }

      test("binds and fills") {
        assert(form.fill(obj).toJson == json)
        assert(form.bind(json) == Success(obj))
      }

      test("all errors") {
        assert(form.getAllErrors() == Set(
          ErrorSpec("validation.form.a.error.required"),
          ErrorSpec("validation.form.b.error.required"),
          ErrorSpec("validation.form.b.error.number"),
        ))
      }
    }
  }
}
