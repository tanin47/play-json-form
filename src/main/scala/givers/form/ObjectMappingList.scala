package givers.form

import givers.form.Mapping.Field
import play.api.libs.json.JsValue

import scala.reflect.ClassTag
import scala.util.Try

class ObjectMappingList[T](val fields: Seq[Field[_]])(implicit classTag: ClassTag[T]) extends ObjectMapping[T] {

  def apply(values: Seq[_]): T = {
    val runtimeClass = classTag.runtimeClass
    if (!runtimeClass.isMemberClass) {
      throw new IllegalArgumentException("CaseClassFactory only applies to case classes!")
    }

    val constructor = runtimeClass.getConstructors.head
    constructor.newInstance(values.map(_.asInstanceOf[Object]): _*).asInstanceOf[T]
  }

  def unapply(value: T): Seq[_] = {
    val runtimeClass = classTag.runtimeClass
    val methods = runtimeClass.getDeclaredMethods
      .filter(_.getParameterCount == 0)
      .sortBy(_.getName)

    methods.map(_.invoke(value)).toSeq
  }

  def bind(value: JsValue, context: BindContext): Try[T] = {
    ObjectMapping.bind(value, fields, context).map(items => apply(items))
  }

  def unbind(value: T, context: UnbindContext): JsValue = {
    val values = unapply(value)
    ObjectMapping.unbind(values, fields, context)
  }
}