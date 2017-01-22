package services

import java.nio.charset.StandardCharsets
import javax.inject._

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import models.User
import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants
import org.apache.directory.api.ldap.model.cursor.EntryCursor
import org.apache.directory.api.ldap.model.entry._
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.api.ldap.model.password.PasswordUtil
import org.apache.directory.ldap.client.api.LdapNetworkConnection
import play.api.inject.ApplicationLifecycle

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.Try

/**
  * @author julianliebl 
  * @since 21.10.16
  */
@Singleton
class LdapClient @Inject() (conf: play.api.Configuration, lifecycle: ApplicationLifecycle){

  val host:String = conf.getString("ldap.host").get
  val port:Int = conf.getInt("ldap.port").get
  val bindUserName = conf.getString("ldap.bind.user.name").get
  val bindUserPass = conf.getString("ldap.bind.user.pass").get

  val groupUsers = conf.getString("ldap.group.users").get


  def getUser(uid:String):Option[User] = {
    connection
      .search( "uid=" + uid + "," + groupUsers , "(objectclass=person)", SearchScope.OBJECT)
      .asScala
      .toSeq
      .headOption
      .flatMap(e => parseUser(e.getAttributes.asScala.toSeq))
  }

  def getUsers():Seq[User] = {
    connection
      .search(groupUsers , "(objectclass=person)", SearchScope.ONELEVEL)
      .asScala
      .toSeq
      .flatMap(e => parseUser(e.getAttributes.asScala.toSeq))
      .sortBy(_.loginInfo.providerKey)
  }

  private def parseUser(attributes: Seq[Attribute]): Option[User] ={
    Try(User(
      loginInfo = LoginInfo(providerID = "LDAP", providerKey = attributes.find(_.getUpId == "uid").map(_.getString).get),
      firstName = attributes.find(_.getUpId == "givenName").map(_.getString),
      lastName = attributes.find(_.getUpId == "sn").map(_.getString),
      fullName = attributes.find(_.getUpId == "cn").map(_.getString),
      email = attributes.find(_.getUpId == "mail").map(_.getString),
      phone = attributes.find(_.getUpId == "homePhone").map(_.getString),
      passwordHash = attributes.find(_.getUpId == "userPassword").map(a => new String(a.getBytes, StandardCharsets.UTF_8))
    )).toOption
  }

  def getPasswordInfo(uid:String):Option[PasswordInfo] = {
    val cursor:EntryCursor = connection
      .search( "uid=" + uid + "," + groupUsers , "(objectclass=person)", SearchScope.OBJECT)

    if(!cursor.next()){
      return None
    }

    val pass = cursor.get().asScala.find(a => a.getUpId.equals("userPassword")).map(a => a.getBytes)

    if(pass.isEmpty){
      return None
    }

    val cred = PasswordUtil.splitCredentials(pass.get)

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

  def modifyPassword(uid:String, pass:String, hash:LdapSecurityConstants = LdapSecurityConstants.HASH_METHOD_MD5): Unit = {
    val bytePassword = PasswordUtil.createStoragePassword(pass, hash)
    val mod = new DefaultModification(
      ModificationOperation.REPLACE_ATTRIBUTE,
      "userpassword", new String(bytePassword, StandardCharsets.UTF_8)
    )
    connection.modify("uid=" + uid + "," + groupUsers, mod)
  }

  def hasPassword(uid:String): Boolean = {
    val user = getUser(uid)
    user.isDefined && user.get.passwordHash.isDefined
  }

  def addMail(uid:String, mail:String): Unit ={
    addAttribute(uid, "mail", mail)
  }

  def modifyMail(uid:String, mail:String): Unit ={
    modifyAttribute(uid, "mail", mail)
  }

  def addPhone(uid:String, phone:String): Unit ={
    addAttribute(uid, "homePhone", phone)
  }

  def modifyPhone(uid:String, phone:String): Unit ={
    modifyAttribute(uid, "homePhone", phone)
  }

  def modifyAttribute(uid:String, key: String, value:String): Unit = {
    val mod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, key, value)
    connection.modify("uid=" + uid + "," + groupUsers, mod)
  }

  def addAttribute(uid:String, key:String, value:String): Unit ={
    val mod = new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, key, value )
    connection.modify("uid=" + uid + "," + groupUsers, mod)
  }

  def addUser(givenName:String, lastname:String, password:String, hash:LdapSecurityConstants = LdapSecurityConstants.HASH_METHOD_MD5): Unit ={
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

    ldap.bind(bindUserName, bindUserPass)

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
