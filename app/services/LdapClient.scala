package services

import java.nio.charset.StandardCharsets
import javax.inject._

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import models.User
import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants
import org.apache.directory.api.ldap.model.cursor.EntryCursor
import org.apache.directory.api.ldap.model.entry.{DefaultEntry, DefaultModification, ModificationOperation}
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.api.ldap.model.password.PasswordUtil
import org.apache.directory.ldap.client.api.LdapNetworkConnection
import play.api.inject.ApplicationLifecycle

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * @author julianliebl 
  * @since 21.10.16
  */
@Singleton
class LdapClient @Inject() (configuration: play.api.Configuration, lifecycle: ApplicationLifecycle){
  val host:String = sys.env.getOrElse("WVG_PROFILE_LDAP_HOST", "localhost")
  val port:Int = sys.env.getOrElse("WVG_PROFILE_LDAP_PORT", "389").toInt
  val userBind = sys.env.getOrElse("WVG_PROFILE_LDAP_USER_BIND", "")
  val userBindPassword = sys.env.getOrElse("WVG_PROFILE_LDAP_USER_BIND_PASSWORD", "")

  val groupUsers = sys.env.getOrElse("WVG_PROFILE_LDAP_USERS_GROUP", "")


  def getUser(uid:String):Option[User] = {
    val cursor:EntryCursor = connection
      .search( "uid=" + uid + "," + groupUsers , "(objectclass=person)", SearchScope.OBJECT)

    if(!cursor.next()){
      return None
    }

    val contents = cursor.get().asScala

    Option(User(
      loginInfo = LoginInfo(providerID = "LDAP", providerKey = uid),
      firstName = contents.find(a => a.getUpId.equals("givenName")).map(a => a.getString),
      lastName  = contents.find(a => a.getUpId.equals("sn")).map(a => a.getString),
      fullName  = contents.find(a => a.getUpId.equals("cn")).map(a => a.getString),
      email = contents.find(a => a.getUpId.equals("mail")).map(a => a.getString)
    ))
  }

  def getPasswordInfo(uid:String):Option[PasswordInfo] = {
    val cursor:EntryCursor = connection
      .search( "uid=" + uid + "," + groupUsers , "(objectclass=person)", SearchScope.OBJECT)

    if(!cursor.next()){
      return None
    }

    val pass = cursor.get().get("userPassword").getBytes
    val cred = PasswordUtil.splitCredentials(pass)

    Option(
      PasswordInfo(
        hasher = cred.getAlgorithm.getName,
        password = new String(cred.getPassword, StandardCharsets.UTF_8)
      )
    )
  }

  def authUser(uid:String, password:String):Boolean = {
    val cursor:EntryCursor = connection
      .search( "uid=" + uid + "," + groupUsers , "(objectclass=person)", SearchScope.OBJECT)

    if(!cursor.next()){
      return false
    }

    val contents = cursor.get()

    PasswordUtil.compareCredentials(password.getBytes(StandardCharsets.UTF_8), contents.get("userPassword").getBytes)
  }

  def modifyPassword(uid:String, pass:String, hash:LdapSecurityConstants = LdapSecurityConstants.HASH_METHOD_SHA256): Unit = {
    val bytePassword = PasswordUtil.createStoragePassword(pass, hash)
    val mod = new DefaultModification(
      ModificationOperation.REPLACE_ATTRIBUTE,
      "userpassword", new String(bytePassword, StandardCharsets.UTF_8)
    )
    connection.modify("uid=" + uid + "," + groupUsers, mod)
  }

  def modifyEmail(uid:String, newEmail:String): Unit = {
    val mod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "mail", newEmail)
    connection.modify("uid=" + uid + "," + groupUsers, mod)
  }

  def addMail(uid:String, mail:String): Unit ={
    addAttribute(uid, "mail", mail)
  }

  def addAttribute(uid:String, key:String, value:String): Unit ={
    val mod = new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, key, value )
    connection.modify("uid=" + uid + "," + groupUsers, mod)
  }

  def addUser(givenName:String, lastname:String, password:String, hash:LdapSecurityConstants = LdapSecurityConstants.HASH_METHOD_SHA256): Unit ={
    val uid = givenName.toLowerCase + lastname.toLowerCase
    val bytePassword = PasswordUtil.createStoragePassword(password, hash)

    connection.add(
      new DefaultEntry(
        "uid=" + uid + "," + groupUsers,    // The Dn
        "objectclass: top",
        "objectclass: person",
        "objectclass: inetOrgPerson",
        "objectclass: organizationalPerson",
        "cn", givenName + " " + lastname,
        "sn", lastname,
        "givenname", givenName,
        "uid", uid,
        "userpassword", new String(bytePassword, StandardCharsets.UTF_8)
      ) )
  }

  def addGroup(dn:String, groupName:String) = {
    connection.add(
      new DefaultEntry(
        "ou=" + groupName + "," + dn,    // The Dn
        "objectclass: organizationalUnit",
        "objectclass: top",
        "ou: " + groupName
      ) )
  }

  def connection = {
    val ldap = new LdapNetworkConnection(host, port)

    ldap.bind(userBind, userBindPassword)

    ldap
  }

  lifecycle.addStopHook { () =>
    Future.successful(() => {
      if(connection.isConnected){
        connection.unBind()
      }
      connection.close()
    })
  }
}
