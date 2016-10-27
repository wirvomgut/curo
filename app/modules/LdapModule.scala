package modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import services.LdapClient

/**
  * @author julianliebl 
  * @since 21.10.16
  */
class LdapModule extends AbstractModule with ScalaModule{
  override def configure(): Unit = {
    bind[LdapClient].asEagerSingleton()
  }
}
