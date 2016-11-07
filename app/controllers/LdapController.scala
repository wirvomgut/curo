package controllers

import java.nio.charset.StandardCharsets
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.{CredentialsProvider, SocialProviderRegistry}
import forms.{EmailForm, PasswordForm}
import models.User
import models.services.UserService
import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants
import org.apache.directory.api.ldap.model.password.PasswordUtil
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import services.LdapClient

import scala.concurrent.Future
import scala.language.postfixOps

/**
  * The credentials auth controller.
  *
  * @param messagesApi The Play messages API.
  * @param env The Silhouette environment.
  * @param userService The user service implementation.
  * @param authInfoRepository The auth info repository implementation.
  * @param credentialsProvider The credentials provider.
  * @param socialProviderRegistry The social provider registry.
  * @param configuration The Play configuration.
  * @param clock The clock instance.
  */
class LdapController @Inject()(
                                            val ldapClient: LdapClient,
                                            val messagesApi: MessagesApi,
                                            val env: Environment[User, CookieAuthenticator],
                                            userService: UserService,
                                            authInfoRepository: AuthInfoRepository,
                                            credentialsProvider: CredentialsProvider,
                                            socialProviderRegistry: SocialProviderRegistry,
                                            configuration: Configuration,
                                            clock: Clock)
  extends Silhouette[User, CookieAuthenticator] {

  /**
    * Changes the users password if form data is correct.
    *
    * @return The result to display.
    */
  def password = SecuredAction.async { implicit request =>
    PasswordForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.password(request.identity, form))),
      data => {
        val passwordOld = data.passwordOld
        val passwordNew = data.passwordNew
        val passwordConfirm = data.passwordConfirm
        val uid = request.identity.loginInfo.providerKey

        if(!ldapClient.authUser(uid, passwordOld)){
          Future.successful(
            Redirect(routes.ApplicationController.password()).flashing("error" -> Messages("password.old.error"))
          )
        }else{
          if(passwordNew.equals(passwordConfirm) && !passwordNew.isEmpty){
            ldapClient.modifyPassword(uid, passwordNew)

            Future.successful(
              Redirect(routes.ApplicationController.password()).flashing("success" -> Messages("password.save.success"))
            )
          }else{
            Future.successful(
              Redirect(routes.ApplicationController.password()).flashing("error" -> Messages("password.new.error"))
            )
          }
        }
      }
    )
  }

  /**
    * Changes the users email if form data is correct.
    *
    * @return The result to display.
    */
  def email = SecuredAction.async { implicit request =>
    EmailForm.form.bindFromRequest.fold(
      form => Future.successful(
        BadRequest(views.html.email(request.identity, form))
      ),
      data => {
        ldapClient.modifyEmail(request.identity.loginInfo.providerKey, data.emailNew)

        Future.successful(
          Redirect(routes.ApplicationController.email()).flashing("success" -> Messages("email.save.success"))
        )
      }
    )
  }
}