import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Strings {

    public final static List<String> NORMALES_SPIEL = new ArrayList<>(Arrays.asList(
            "normales Spiel",
            "keine Vorbehalte"
    ));

    public final static List<String> SOLO = new ArrayList<>(Arrays.asList(
            "%s spielt ein %ssolo"
    ));

    public final static List<String> ARMUT_GIVE = new ArrayList<>(Arrays.asList(
            "%s hat nur %d Trumpf"
    ));

    public final static List<String> KOENIGE = new ArrayList<>(Arrays.asList(
            "%s hat mehr als 5 Könige",
            "%s hat zu viele Könige"
    ));

    public final static List<String> ARMUT_REFUSE = new ArrayList<>(Arrays.asList(
            "%s lehnt die Armut ab",
            "%s nimmt die Armut nicht mit",
            "%s mag arme Menschen nicht"
    ));

    public final static List<String> ARMUT_ACCEPT = new ArrayList<>(Arrays.asList(
            "%s nimmt die Armut auf",
            "%s ist reich"
    ));

    public final static List<String> ARMUT_RETURN = new ArrayList<>(Arrays.asList(
            "%s bekommt %s Trumpf zurück",
            "%s erhält %s Trumpf zurück",
            "An %s gehen %s Trumpf zurück"
    ));

    public final static List<String> NEU_GEBEN = new ArrayList<>(Arrays.asList(
            "Es wird neu gegeben",
            "Es wird nochmal gegeben",
            "Karten werden neu verteilt"
    ));



    public static String getString(List<String> list, Object... args ){
        Random r = new Random(System.currentTimeMillis());
        return String.format(list.get(r.nextInt(list.size())),args);
    }
}
