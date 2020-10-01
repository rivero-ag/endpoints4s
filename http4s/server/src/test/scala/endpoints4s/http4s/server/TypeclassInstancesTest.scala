package endpoints4s.http4s.server

import cats._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import cats.implicits._
// import cats.implicits._

import cats.laws.discipline.MonadTests
// import cats.laws.discipline.FunctorTests

import org.scalatest.funsuite.AnyFunSuite
// import org.scalatest.funsuite.AnyFunSuite

import cats.Eq
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FunSuiteDiscipline
import cats.effect.IO

import cats.Eq
import cats.data.EitherT
import cats.instances.either.catsStdEqForEither
import cats.instances.int._
import cats.instances.option.catsKernelStdEqForOption
import cats.instances.string._
import cats.instances.unit._
import cats.laws.discipline.MonadErrorTests
import cats.laws.discipline.SemigroupalTests.Isomorphisms
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FunSuiteDiscipline
import cats.laws.discipline._
import cats.laws.discipline.eq._
import org.http4s.Request


object TypeclassInstancesTest extends AnyFunSuite with FunSuiteDiscipline with Configuration  {
  val server = new ServerInterpreterTest
  implicit val monadInstance = server.serverApi.requestEntityMonadError

  implicit def arbitraryResponse:Arbitrary[org.http4s.Response[IO]] = ???

  implicit val throwableEq: Eq[Throwable] = ???


  implicit def weDiregardRequestAnyways[A]: ExhaustiveCheck[org.http4s.Request[IO]] = new ExhaustiveCheck[org.http4s.Request[IO]] {
    def allValues: List[Request[IO]] = List(Request.apply[IO]())
  }

  implicit def yoloEq[A]:Eq[IO[A]] = new Eq[IO[A]] {
    def eqv(x: IO[A], y: IO[A]): Boolean = x.unsafeRunSync() === y.unsafeRunSync()
  }

  implicit def arbitraryRequestEntity[A](
    implicit arbitraryA: Arbitrary[A],
    arbitraryResponse: Arbitrary[org.http4s.Response[IO]],
    arbitraryThrowable: Arbitrary[Throwable],
  ):Arbitrary[server.serverApi.RequestEntity[A]] =Arbitrary( for {
    leftResp <- arbitraryResponse.arbitrary
    a <- arbitraryA.arbitrary
    effectErr <- arbitraryThrowable.arbitrary

    respA = IO.pure(a.asRight[org.http4s.Response[IO]])
    respLeft = IO.pure(leftResp.asLeft[A])
    respEffectErr = IO.raiseError[Either[org.http4s.Response[IO], A]](effectErr)
    resp <- Gen.oneOf(respA, respLeft, respEffectErr)
  } yield (_:org.http4s.Request[IO]) => resp)


  checkAll("Server RequestEntityMonad MonadLaws", MonadErrorTests[server.serverApi.RequestEntity, Throwable].monadError[Boolean, Boolean, Boolean])
}
