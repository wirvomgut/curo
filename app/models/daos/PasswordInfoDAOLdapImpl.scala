package models.daos

import java.sql.ResultSet
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import play.api.Play.current
import play.api.db.DB
import play.api.libs.concurrent.Execution.Implicits._
import services.LdapClient

import scala.concurrent.Future

/**
 * The DAO to store the password information.
 */
class PasswordInfoDAOLdapImpl @Inject() (ldapClient: LdapClient) extends DelegableAuthInfoDAO[PasswordInfo] {

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    Future.successful(ldapClient.getPasswordInfo(loginInfo.providerKey))
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    DB.withConnection(conn => {
      conn.createStatement().execute(
        s"""
        INSERT INTO authinfo (
          loginInfoId, loginInfoKey, hasher, password, salt
        ) values (
          '${loginInfo.providerID}', '${loginInfo.providerKey}', '${authInfo.hasher}', '${authInfo.password}',
          '${authInfo.salt}'
        )

      """.stripMargin)
    })
    Future.successful(authInfo)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    DB.withConnection(conn => {
      conn.createStatement().execute(
        s"""
        UPDATE authinfo
        SET hasher='${authInfo.hasher}', password='${authInfo.password}', salt='${authInfo.salt}'
        WHERE loginInfoId='${loginInfo.providerID}' AND loginInfoKey='${loginInfo.providerKey}'

      """.stripMargin)
    })
    Future.successful(authInfo)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    var passwordInfo: Option[PasswordInfo] = None

    Future.successful(())
  }
}
