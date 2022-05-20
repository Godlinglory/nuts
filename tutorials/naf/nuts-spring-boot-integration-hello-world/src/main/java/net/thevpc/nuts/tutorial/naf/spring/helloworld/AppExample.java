package net.thevpc.nuts.tutorial.naf.spring.helloworld;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.lib.spring.boot.NutsSpringBootConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
public class AppExample implements NutsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppExample.class, args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsPrintStream out = context.getSession().out();
        out.println("Hello ##World##");
    }
}
