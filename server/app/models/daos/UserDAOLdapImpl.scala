package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.User
import services.LdapClient

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
class UserDAOLdapImpl @Inject() (ldapClient: LdapClient) extends UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    find(loginInfo.providerKey)
  }

  /**
   * Finds a user by its user ID.
   *
   * @param uid The uid of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(uid: String): Future[Option[User]] = {
    Future.successful(ldapClient.getUser(uid))
  }

  /**
   * Saves a user.
   *
   * The current LDAP implementation does not allow to add new users! Not implemented yet.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User] = {
    throw new NotImplementedError()
  }
}
