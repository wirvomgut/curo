package controllers

import java.nio.charset.StandardCharsets

import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants
import org.apache.directory.api.ldap.model.password.PasswordUtil

/**
  * @author julianliebl 
  * @since 27.10.16
  */
class PasswordHasherMD5 extends PasswordHasher{
  override def id: String = "MD5"

  override def hash(plainPassword: String): PasswordInfo = {
    PasswordInfo(hasher = id, password = plainPassword)
  }

  //TODO: Here might be an oversight. It seems to be to complicated.
  override def matches(passwordInfo: PasswordInfo, suppliedPassword: String): Boolean = {
    val suppliedStoragePassword =  PasswordUtil.createStoragePassword(
      suppliedPassword,
      LdapSecurityConstants.getAlgorithm(passwordInfo.hasher)
    )

    val split = new String(PasswordUtil.splitCredentials(suppliedStoragePassword).getPassword,StandardCharsets.UTF_8)
    val test = passwordInfo.password

    split.equals(test)
  }
}
