package controllers

import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}

/**
  * @author julianliebl 
  * @since 27.10.16
  */
class PasswordHasherLdap extends PasswordHasher{
  override def id: String = "plain"

  override def hash(plainPassword: String): PasswordInfo = {
    PasswordInfo(hasher = id, password = plainPassword)
  }

  override def matches(passwordInfo: PasswordInfo, suppliedPassword: String): Boolean = {
    passwordInfo.password.equals(suppliedPassword)
  }
}
