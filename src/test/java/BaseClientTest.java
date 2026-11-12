import base.Statics;
import base.config.Configuration;
import base.game.Card;
import base.game.doko.DokoCards;
import base.game.doko.SortHand;
import base.messages.doko.MessageGameSelected;
import org.junit.Test;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseClientTest {

    @Test
    public void normalGameMarksExpectedTrumpCards() {
        Card karoAss = new Card(Statics.ASS, Statics.KARO, 0, 11);
        Card heartTen = new Card(Statics.ZEHN, Statics.HERZ, 1, 10);
        Card crossAss = new Card(Statics.ASS, Statics.KREUZ, 2, 11);

        assertTrue(DokoCards.isTrumpf(karoAss, MessageGameSelected.GAMES.NORMAL));
        assertTrue(DokoCards.isTrumpf(heartTen, MessageGameSelected.GAMES.NORMAL));
        assertFalse(DokoCards.isTrumpf(crossAss, MessageGameSelected.GAMES.NORMAL));
    }

    @Test
    public void bubenSoloOnlyMarksBubenAsTrump() {
        Card bubeCross = new Card(Statics.BUBE, Statics.KREUZ, 0, 2);
        Card damePik = new Card(Statics.DAME, Statics.PIK, 1, 3);

        assertTrue(DokoCards.isTrumpf(bubeCross, MessageGameSelected.GAMES.BUBEN));
        assertFalse(DokoCards.isTrumpf(damePik, MessageGameSelected.GAMES.BUBEN));
    }

    @Test
    public void deckContainsFortyCardsAcrossTwentyUniqueSuitRankCombinations() {
        List<String> uniqueIds = DokoCards.ALL_CARDS.stream()
                .map(Card::toTrimedString)
                .distinct()
                .collect(Collectors.toList());

        assertEquals(40, DokoCards.ALL_CARDS.size());
        assertEquals(20, uniqueIds.size());
    }

    @Test
    public void sortNormalPlacesStrongTrumpCardsFirst() {
        List<Card> hand = Arrays.asList(
                new Card(Statics.KOENIG, Statics.KREUZ, 0, 4),
                new Card(Statics.ASS, Statics.KARO, 1, 11),
                new Card(Statics.DAME, Statics.PIK, 2, 3),
                new Card(Statics.ZEHN, Statics.HERZ, 3, 10),
                new Card(Statics.SIEBEN, Statics.KREUZ, 4, 0)
        );

        List<Card> sorted = SortHand.sortNormal(hand, false);

        assertEquals(Statics.HERZ + Statics.ZEHN, sorted.get(0).toTrimedString());
        assertEquals(Statics.PIK + Statics.DAME, sorted.get(1).toTrimedString());
        assertEquals(Statics.KARO + Statics.ASS, sorted.get(2).toTrimedString());
    }

    @Test
    public void dokoClientBuildsGameSelectionButtons() {
        DokoClient client = new DokoClient(null, Arrays.asList("Alice", "Bob", "Carol", "Dave"), new Configuration());

        client.createGameSpecificButtons();

        assertEquals(12, client.buttonList.size());
        assertTrue(client.buttonList.stream().anyMatch(button -> button.getText().equals("Gesund")));
        assertTrue(client.buttonList.stream().anyMatch(button -> button.getText().equals("Buben-Damen")));
    }

    @Test
    public void dokoClientClickingSortNormalSelectsNormalGame() throws Exception {
        DokoClient client = new DokoClient(null, Arrays.asList("Alice", "Bob", "Carol", "Dave"), new Configuration());
        client.createGameSpecificButtons();
        client.panel = new JPanel();
        client.hand = Arrays.asList(
                new Card(Statics.BUBE, Statics.KREUZ, 0, 2),
                new Card(Statics.DAME, Statics.PIK, 1, 3),
                new Card(Statics.ZEHN, Statics.HERZ, 2, 10),
                new Card(Statics.ASS, Statics.KARO, 3, 11),
                new Card(Statics.SIEBEN, Statics.KREUZ, 4, 0)
        );

        findButtonByText(client.buttonList, "Gesund").doClick();

        assertEquals(MessageGameSelected.GAMES.NORMAL, readField(client, "selectedGame"));
    }

    @Test
    public void skatClientBuildsGameSelectionButtons() {
        SkatClient client = new SkatClient(null, Arrays.asList("Alice", "Bob", "Carol", "Dave"), new Configuration());

        client.createGameSpecificButtons();

        assertEquals(8, client.buttonList.size());
        assertTrue(client.buttonList.stream().anyMatch(button -> button.getText().equals("Karo")));
        assertTrue(client.buttonList.stream().anyMatch(button -> button.getText().equals("Null")));
        assertTrue(client.buttonList.stream().anyMatch(button -> button.getText().equals("weg")));
    }

    @Test
    public void skatClientClickingSortKaroSelectsKaroGame() throws Exception {
        SkatClient client = new SkatClient(null, Arrays.asList("Alice", "Bob", "Carol", "Dave"), new Configuration());
        client.createGameSpecificButtons();
        client.panel = new JPanel();
        client.hand = Arrays.asList(
                new Card(Statics.SIEBEN, Statics.KARO, 0, 0),
                new Card(Statics.ASS, Statics.PIK, 1, 11),
                new Card(Statics.KOENIG, Statics.HERZ, 2, 4),
                new Card(Statics.DAME, Statics.KREUZ, 3, 3)
        );

        findButtonByText(client.buttonList, "Karo").doClick();

        assertEquals(base.messages.skat.MessageGameSelected.GAMES.Karo, readField(client, "sortGame"));
    }

    private JButton findButtonByText(List<JButton> buttons, String text) {
        return buttons.stream()
                .filter(button -> button.getText().equals(text))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No button named '" + text + "' found"));
    }

    private Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}