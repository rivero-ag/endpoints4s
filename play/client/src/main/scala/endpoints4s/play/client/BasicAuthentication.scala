package endpoints4s.play.client

import endpoints4s.{Tupler, algebra}
import endpoints4s.algebra.BasicAuthentication.Credentials
import endpoints4s.algebra.Documentation
import play.api.libs.ws.WSAuthScheme

/**
  * @group interpreters
  */
trait BasicAuthentication extends algebra.BasicAuthentication {
  self: EndpointsWithCustomErrors =>

  private[endpoints4s] def authenticatedRequest[U, E, H, UE, HCred, Out](
      method: Method,
      url: Url[U],
      entity: RequestEntity[E],
      headers: RequestHeaders[H],
      requestDocs: Documentation
  )(implicit
      tuplerUE: Tupler.Aux[U, E, UE],
      tuplerHCred: Tupler.Aux[H, Credentials, HCred],
      tuplerUEHCred: Tupler.Aux[UE, HCred, Out]
  ): Request[Out] = {
    val basicAuthenticationHeader: RequestHeaders[Credentials] =
      (credentials, request) => {
        request.withAuth(
          credentials.username,
          credentials.password,
          WSAuthScheme.BASIC
        )
      }
    request(
      method,
      url,
      entity,
      requestDocs,
      headers ++ basicAuthenticationHeader
    )
  }

}
