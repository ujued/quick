package net.apisp.quick.example.ioc;

import net.apisp.quick.ioc.annotation.Accept;
import net.apisp.quick.ioc.annotation.Factory;

@Factory
public class ObjectFactory {

    @Accept
    public Ujued ujued() {
        return new Ujued();
    }

    @Accept
    public James james() {
        return new James();
    }

    @Accept
    public Bob bob(Ujued ujued) {
        return new Bob(ujued);
    }
}
