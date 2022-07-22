package org.example.it;

import io.restassured.RestAssured;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;

@ExtendWith(ArquillianExtension.class)
public class DeploySimplePrimefacesTest {
    
    private final static Logger logger = Logger.getLogger(DeploySimplePrimefacesTest.class.getName());
    
    @Deployment
    public static WebArchive createDeployment() throws Exception {
        logger.info("Creating deployment");
        
        WebArchive war = ShrinkWrap.create(WebArchive.class);
    
        Path helloXhtml = Path.of("src/main/webapp/hello-pf.xhtml");
        war.addAsWebResource(helloXhtml.toFile());
        
        Files.list(Path.of("src/main/webapp/WEB-INF")).forEach(
            path -> war.addAsWebInfResource(path.toFile())
        );
    
        // Import Primefaces from Maven
        // Taken from https://cassiomolin.com/2015/06/07/adding-maven-dependencies-to-arquillian-test/
        File[] files = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importCompileAndRuntimeDependencies()
            .resolve("org.primefaces:primefaces:jar:jakarta:11.0.0")
            .withTransitivity()
            .asFile();
        war.addAsLibraries(files);
        
        return war;
    }
    
    @ArquillianResource
    public static URL deploymentUrl;
    
    @Test
    @RunAsClient
    void accessHelloXhtml() {
        logger.info("Hello Primefaces");
        RestAssured.baseURI = deploymentUrl.toString();
        given().when().get("/hello-pf.xhtml").then().assertThat().statusCode(HttpServletResponse.SC_OK);
    }

}
