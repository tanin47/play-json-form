package givers.form

import givers.form.Mapping.Field
import play.api.libs.json.JsValue

import scala.reflect.{ClassTag, classTag}
import scala.reflect.runtime.universe.{MethodSymbol, TypeTag, runtimeMirror, termNames, typeOf}
import scala.util.Try

class ObjectMappingList[T: TypeTag: ClassTag](val fields: Seq[Field[_]]) extends ObjectMapping[T] {

  def apply(values: Seq[_]): T = {
    // Due to Play's auto-reload. We'll have to re-fetch the class loader every time.
    // This is because Play's auto-reload replace the class loader with a new one every time the code is changed.
    val rm = runtimeMirror(classTag[T].runtimeClass.getClassLoader)
    val tpe = typeOf[T]
    val classSymbol = tpe.typeSymbol.asClass

    if (!(tpe <:< typeOf[Product] && classSymbol.isCaseClass))
      throw new IllegalArgumentException(
        "CaseClassFactory only applies to case classes!"
      )

    val classMirror = rm.reflectClass(classSymbol)

    val constructorSymbol = tpe.decl(termNames.CONSTRUCTOR)

    val defaultConstructor = if (constructorSymbol.isMethod) {
      constructorSymbol.asMethod
    }
    else {
      val ctors = constructorSymbol.asTerm.alternatives
      ctors.map { _.asMethod }.find { _.isPrimaryConstructor }.get
    }

    val constructorMethod = classMirror.reflectConstructor(defaultConstructor)

    constructorMethod.apply(values: _*).asInstanceOf[T]
  }

  def unapply(value: T): Seq[_] = {
    // Due to Play's auto-reload. We'll have to re-fetch the class loader every time.
    // This is because Play's auto-reload replace the class loader with a new one every time the code is changed.
    val rm = runtimeMirror(classTag[T].runtimeClass.getClassLoader)
    val instance = rm.reflect(value)
    val fields = typeOf[T].members.sorted.collect { case m: (MethodSymbol @unchecked) if m.isCaseAccessor => m }.toList
    fields.map { f => instance.reflectMethod(f).apply() }
  }

  def bind(value: JsValue, context: BindContext): Try[T] = {
    ObjectMapping.bind(value, fields, context).map(items => apply(items))
  }

  def unbind(value: T, context: UnbindContext): JsValue = {
    val values = unapply(value)
    ObjectMapping.unbind(values, fields, context)
  }
}

