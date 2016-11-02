package services

import java.nio.charset.StandardCharsets
import javax.inject._

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import models.User
import org.apache.directory.api.ldap.model.cursor.EntryCursor
import org.apache.directory.api.ldap.model.entry.{DefaultEntry, DefaultModification, ModificationOperation}
import org.apache.directory.api.ldap.model.message.SearchScope
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
  val host = configuration.getString("ldap.host").getOrElse("localhost")
  val port = configuration.getInt("ldap.port").getOrElse(389)
  val userBind = configuration.getString("ldap.user.bind").getOrElse("")
  val userBindPassword = sys.env.getOrElse("WVG_PROFILE_USER_BIND_PASSWORD", "")

  val groupUsers = configuration.getString("ldap.group.users").getOrElse("")


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

    val fetchedPassword = new String(cursor.get().get("userPassword").getBytes, StandardCharsets.UTF_8)


    val split = fetchedPassword.split("\\}", 2)
    val algo = split(0).replaceFirst("\\{", "")
    val hash = split(1)

    Option(
      PasswordInfo(
        hasher = algo,
        password = hash
      )
    )
  }

  //TODO: implement other hashing types like sha256 and md5
  def authUser(uid:String, password:String):Boolean = {
    val cursor:EntryCursor = connection
      .search( "uid=" + uid + "," + groupUsers , "(objectclass=person)", SearchScope.OBJECT)

    if(!cursor.next()){
      return false
    }

    val contents = cursor.get()
    val usernameOk = contents.get("uid").getString.equals(uid)

    val fetchedPassword = new String(contents.get("userPassword").getBytes, StandardCharsets.UTF_8)


    val split = fetchedPassword.split("\\}", 2)
    val algo = split(0).replaceFirst("\\{", "")
    val hash = split(1)

    if(algo.equals("plain")){
      usernameOk && password.equals(hash)
    }else{
      false
    }
  }

  //TODO: implement other hashing types like sha256 and md5
  def modifyPassword(uid:String, newPassword:String): Unit = {
    val mod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "userpassword", "{plain}" + newPassword)
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

  def addUser(givenName:String, lastname:String, password:String): Unit ={
    val uid = givenName.toLowerCase + lastname.toLowerCase

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
        "userpassword", "{plain}" + password
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