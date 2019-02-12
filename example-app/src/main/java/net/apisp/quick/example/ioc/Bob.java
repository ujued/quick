package net.apisp.quick.example.ioc;

public class Bob implements NiceInfo {

    private Ujued ujued;

    public Bob(Ujued ujued) {
        this.ujued = ujued;
    }

    @Override
    public String get() {
        return "He is Ujued's nice friend! Abount Ujued? " + ujued.get();
    }
}
