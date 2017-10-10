package controllers

import java.nio.charset.StandardCharsets

import com.mohiva.play.silhouette.api.util.{ PasswordHasher, PasswordInfo }
import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants
import org.apache.directory.api.ldap.model.password.PasswordUtil

/**
 * @author julianliebl
 * @since 27.10.16
 */
class PasswordHasherSHA256 extends PasswordHasher {
  override def id: String = "SHA-256"

  override def hash(plainPassword: String): PasswordInfo = {
    PasswordInfo(hasher = id, password = plainPassword)
  }

  //TODO: Here might be an oversight. It seems to be to complicated.
  override def matches(passwordInfo: PasswordInfo, suppliedPassword: String): Boolean = {
    val suppliedStoragePW = PasswordUtil.createStoragePassword(
      suppliedPassword,
      LdapSecurityConstants.getAlgorithm(passwordInfo.hasher)
    )

    val suppliedPW = new String(PasswordUtil.splitCredentials(suppliedStoragePW).getPassword, StandardCharsets.UTF_8)
    val storedPW = passwordInfo.password

    suppliedPW.equals(storedPW)
  }
}
