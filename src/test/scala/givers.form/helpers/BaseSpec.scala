package givers.form.helpers

import org.mockito.ArgumentMatcher
import org.mockito.internal.progress.ThreadSafeMockingProgress
import org.mockito.verification.VerificationMode
import utest.TestSuite

import scala.collection.mutable
import scala.reflect.ClassTag

private[form] abstract class BaseSpec extends TestSuite {
  def mock[T](implicit m: ClassTag[T]): T = org.mockito.Mockito.mock(m.runtimeClass.asInstanceOf[Class[T]])

  def any[T]() = org.mockito.ArgumentMatchers.any[T]()
  def argThat[T](fn: T => Boolean) = org.mockito.ArgumentMatchers.argThat[T](new ArgumentMatcher[T] {
    override def matches(argument: T) = fn(argument)
  })
  def varArgsThat[T](fn: Seq[T] => Boolean): T = {
    ThreadSafeMockingProgress.mockingProgress().getArgumentMatcherStorage.reportMatcher(new ArgumentMatcher[mutable.ArraySeq[T]] {
      override def matches(argument: mutable.ArraySeq[T]) = fn(argument.toSeq)
    })
    null.asInstanceOf[T]
  }

  def times(n: Int) = org.mockito.Mockito.times(n)

  def eq[T](v: T) = org.mockito.ArgumentMatchers.eq(v)

  def verify[T](mock: T) = org.mockito.Mockito.verify(mock)
  def verify[T](mock: T, mode: VerificationMode) = org.mockito.Mockito.verify(mock, mode)
  def verifyNoMoreInteractions(mocks: AnyRef*) = org.mockito.Mockito.verifyNoMoreInteractions(mocks:_*)
  def verifyZeroInteractions(mocks: AnyRef*) = org.mockito.Mockito.verifyNoInteractions(mocks: _*)
  def when[T](methodCall: T) = org.mockito.Mockito.when(methodCall)
  def doThrow(toBeThrown: Throwable*) = org.mockito.Mockito.doThrow(toBeThrown:_*)
}
