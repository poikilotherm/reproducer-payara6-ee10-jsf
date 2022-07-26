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
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;

@ExtendWith(ArquillianExtension.class)
public class DeployPrettyfacesTest {
    
    private final static Logger logger = Logger.getLogger(DeployPrettyfacesTest.class.getName());
    
    @Deployment
    public static WebArchive createDeployment() throws Exception {
        logger.info("Creating deployment");
        
        WebArchive war = ShrinkWrap.create(WebArchive.class);
    
        Path helloXhtml = Path.of("src/main/webapp/hello.xhtml");
        war.addAsWebResource(helloXhtml.toFile());
        
        Files.list(Path.of("src/main/webapp/WEB-INF")).forEach(
            path -> war.addAsWebInfResource(path.toFile())
        );
    
        // Import Prettyfaces Servlet
        // Taken from https://cassiomolin.com/2015/06/07/adding-maven-dependencies-to-arquillian-test/
        File[] files = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importDependencies(ScopeType.COMPILE)
            .resolve("org.ocpsoft.rewrite:rewrite-servlet")
            .withTransitivity()
            .asFile();
        war.addAsLibraries(files);
    
        Arrays.stream(files).forEach(f -> System.out.println(f.getAbsolutePath()));
    
        // Import Prettyfaces Config
        files = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .resolve("org.ocpsoft.rewrite:rewrite-config-prettyfaces")
            .withTransitivity()
            .asFile();
        war.addAsLibraries(files);
    
        Arrays.stream(files).forEach(f -> System.out.println(f.getAbsolutePath()));
        
        return war;
    }
    
    @ArquillianResource
    public static URL deploymentUrl;
    
    @Test
    @RunAsClient
    void accessHelloXhtml() {
        logger.info("Hello JSF");
        RestAssured.baseURI = deploymentUrl.toString();
        given().when().get("/hello.xhtml").then().assertThat().statusCode(HttpServletResponse.SC_OK);
    }

}
