package controllers

import org.apache.directory.api.ldap.model.cursor.EntryCursor
import org.apache.directory.api.ldap.model.message.SearchScope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification
import services.LdapClient

/**
  * @author julianliebl 
  * @since 20.10.16
  */
class LdapClientISpec extends PlaySpecification {
  sequential

  lazy val application = new GuiceApplicationBuilder().build()
  lazy val client = application.injector.instanceOf(classOf[LdapClient])
  lazy val connection = client.connection


  "Ldap client" should {
    "be able to connect to ldap server" in {
      connection.isConnected must be equalTo true
      connection.isAuthenticated must be equalTo true
    }
    "be able to add a group" in {
      connection.exists("ou=users,dc=test,dc=com") must be equalTo false

      client.addGroup("dc=test,dc=com", "users")

      connection.exists("ou=users,dc=test,dc=com") must be equalTo true
    }
    "be able to add users" in {
      connection.exists("uid=given1last1,ou=users,dc=test,dc=com") must be equalTo false
      connection.exists("uid=given2last2,ou=users,dc=test,dc=com") must be equalTo false

      client.addUser("Given1", "Last1", "pw1")
      client.addUser("Given2", "Last2", "pw2")
      client.addUser("Julian", "Liebl", "test")

      connection.exists("uid=given1last1,ou=users,dc=test,dc=com") must be equalTo true
      connection.exists("uid=given2last2,ou=users,dc=test,dc=com") must be equalTo true
    }
    "search for users" in {
      val cursor:EntryCursor = connection.search( "ou=users,dc=test,dc=com", "(objectclass=person)", SearchScope.ONELEVEL )

      cursor.next() must be equalTo true
      cursor.get().getDn === "uid=given1last1,ou=users,dc=test,dc=com"
      cursor.next() must be equalTo true
      cursor.get().getDn ==="uid=given2last2,ou=users,dc=test,dc=com"
      cursor.next() must be equalTo false

      cursor.close()
      ok
    }
    "validate a user auth" in {
      client.authUser("given1last1", "pw1") must be equalTo true
      client.authUser("given1last1", "pw2") must be equalTo false
      client.authUser("given2last2", "pw1") must be equalTo false
      client.authUser("given2last2", "pw2") must be equalTo true
    }
    "change a users password" in {
      client.authUser("given2last2", "pw2") must be equalTo true
      client.authUser("given2last2", "pw2new") must be equalTo false

      client.modifyPassword("given2last2", "pw2new")

      client.authUser("given2last2", "pw2") must be equalTo false
      client.authUser("given2last2", "pw2new") must be equalTo true
    }
    "add a mail attribute" in {
      client.addMail("given1last1", "given1@last1.com")

      val cursor:EntryCursor = connection.search( "uid=given1last1,ou=users,dc=test,dc=com", "(objectclass=person)", SearchScope.OBJECT )
      cursor.next() must be equalTo true
      cursor.get().get("mail").getString === "given1@last1.com"
      cursor.close()

      ok
    }
    "cleanup and close the connection" in {
      connection.delete("uid=given1last1,ou=users,dc=test,dc=com")
      connection.exists("uid=given1last1,ou=users,dc=test,dc=com") must be equalTo false

      connection.delete("uid=given2last2,ou=users,dc=test,dc=com")
      connection.exists("uid=given2last2,ou=users,dc=test,dc=com") must be equalTo false

      connection.delete("ou=users,dc=test,dc=com")
      connection.exists("ou=users,dc=test,dc=com") must be equalTo false
    }

  }

}
