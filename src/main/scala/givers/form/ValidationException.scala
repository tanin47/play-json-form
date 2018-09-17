package givers.form


class ValidationMessage(val key: String, val message: String, val args: Any*) {
  override def toString = {
    s"ValidationMessage($key, $message, ${args.mkString(", ")})"
  }

  def addPrefix(prefix: String): ValidationMessage = {
    new ValidationMessage(
      key = if (key.isEmpty) {
        prefix
      } else {
        s"$prefix.$key"
      },
      message = message,
      args:_*
    )
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[ValidationMessage]

  override def equals(other: Any): Boolean = other match {
    case that: ValidationMessage =>
      (that canEqual this) &&
        key == that.key &&
        message == that.message &&
        args == that.args
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(key, message, args)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

class ValidationException(val messages: Seq[ValidationMessage]) extends RuntimeException {
  override def toString = {
    s"ValidationException($messages)"
  }

  def addPrefix(key: String) =  new ValidationException(messages.map(_.addPrefix(key)))

  def canEqual(other: Any): Boolean = other.isInstanceOf[ValidationException]

  override def equals(other: Any): Boolean = other match {
    case that: ValidationException =>
      (that canEqual this) &&
        messages == that.messages
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(messages)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
