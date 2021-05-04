import java.util.HashMap;

public class DokoConfig {

    public HashMap<String,Boolean> sonderpunkte;
    public HashMap<String,Object>  regeln;


    public DokoConfig() {
        sonderpunkte = new HashMap<>();
        sonderpunkte.put("doppelkopf",true);
        sonderpunkte.put("karlchen",true);
        sonderpunkte.put("herzstich",true);
        sonderpunkte.put("fuchsGefangen",true);

        regeln = new HashMap<>();
        regeln.put("zweiteHerz10",true);
        regeln.put("schwein",true);
    }

}
