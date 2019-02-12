package net.apisp.quick.example;

import net.apisp.quick.core.Quick;
import net.apisp.quick.core.http.WebContext;
import net.apisp.quick.example.ioc.Bob;
import net.apisp.quick.example.ioc.James;
import net.apisp.quick.example.ioc.Ujued;

public class ExampleApplication {

    public static void main(String[] args) {
        Quick.boot(args)
                .mapping("GET /ujued", ExampleApplication::ujued)
                .mapping("GET /james", ExampleApplication::james)
                .mapping("GET /bob", (webContext) -> {
                    return webContext.singleton(Bob.class).get();
                })
                .mapping("GET /{name}",
                        (req, res) ->
                                res.body(("Who is " + req.variable("name", String.class) + "?").getBytes()));
    }

    private static String ujued(WebContext webContext) {
        return webContext.singleton(Ujued.class).get();
    }

    private static String james(WebContext webContext) {
        return webContext.singleton(James.class).get();
    }
}
