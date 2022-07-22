package org.example.it;

import io.restassured.RestAssured;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;

@ExtendWith(ArquillianExtension.class)
public class DeployGFWebJSFTest {
    
    private final static Logger logger = Logger.getLogger(DeployGFWebJSFTest.class.getName());
    
    @Deployment
    public static WebArchive createDeployment() throws Exception {
        logger.info("Creating deployment");
        
        WebArchive war = ShrinkWrap.create(WebArchive.class);
    
        Path helloXhtml = Path.of("src/main/webapp/hello.xhtml");
        war.addAsWebResource(helloXhtml.toFile());
        
        Files.list(Path.of("src/main/webapp/WEB-INF")).forEach(
            path -> war.addAsWebInfResource(path.toFile())
        );
        war.addAsWebInfResource(Path.of("src/main/resources/glassfish-web.xml").toFile());
        
        return war;
    }
    
    @ArquillianResource
    public static URL deploymentUrl;
    
    @Test
    @RunAsClient
    void accessHelloXhtml() {
        logger.info("Hello JSF + glassfish-web.xml");
        RestAssured.baseURI = deploymentUrl.toString();
        given().when().get("/hello.xhtml").then().assertThat().statusCode(HttpServletResponse.SC_OK);
    }

}
