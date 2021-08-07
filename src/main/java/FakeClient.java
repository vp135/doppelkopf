import base.BaseCard;
import base.Configuration;
import base.IInputputHandler;
import base.Statics;
import base.doko.DokoCards;
import base.doko.messages.MessageGameSelected;
import base.messages.MessageCards;
import base.messages.Message;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FakeClient implements IInputputHandler {

    private final ComClient comClient;
    private final Configuration config;
    private final JPanel panel;
    private JPanel trumpfPanel;
    private JPanel kreuzPanel;
    private JPanel pikPanel;
    private JPanel herzPanel;
    private JPanel karoPanel;
    private MessageGameSelected.GAMES selectedGame = MessageGameSelected.GAMES.NORMAL;



    public FakeClient(Configuration config) {
        this.config = config;
        this.comClient = new ComClient(config.connection.server,config.connection.port, this,"FAKE");
        this.comClient.start();
        JFrame frame = new JFrame();
        panel = new JPanel(new GridLayout(1, 5));
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

    }

    @Override
    public void handleInput(Message message) {
        switch (message.getCommand()){
            case MessageCards.COMMAND:
                handleMessageCard(message);

                break;
            case MessageGameSelected.COMMAND:

                break;
        }
    }

    private void handleMessageCard(Message message) {
        MessageCards messageCards = new MessageCards(message);
        List<BaseCard> hand = messageCards.getCards();
        HashMap<String,List<BaseCard>> buckets = DevideCards(hand);
        panel.removeAll();
        trumpfPanel = new JPanel();
        kreuzPanel = new JPanel();
        pikPanel = new JPanel();
        herzPanel= new JPanel();
        karoPanel= new JPanel();
        panel.add(trumpfPanel);
        panel.add(kreuzPanel);
        panel.add(pikPanel);
        panel.add(herzPanel);
        panel.add(karoPanel);
        trumpfPanel.setLayout(new GridLayout(buckets.get(Statics.TRUMPF).size(),1));
        trumpfPanel.setBackground(Color.RED);
        kreuzPanel.setLayout(new GridLayout(buckets.get(Statics.KREUZ).size(),1));
        kreuzPanel.setBackground(Color.GREEN);
        pikPanel.setLayout(new GridLayout(buckets.get(Statics.PIK).size(),1));
        pikPanel.setBackground(Color.BLUE);
        herzPanel.setLayout(new GridLayout(buckets.get(Statics.HERZ).size(),1));
        herzPanel.setBackground(Color.MAGENTA);
        karoPanel.setLayout(new GridLayout(buckets.get(Statics.KARO).size(),1));
        karoPanel.setBackground(Color.YELLOW);
        buckets.get(Statics.TRUMPF).forEach(card ->  trumpfPanel.add(new JLabel(card.toString())));
        buckets.get(Statics.KREUZ).forEach(card ->  kreuzPanel.add(new JLabel(card.toString())));
        buckets.get(Statics.PIK).forEach(card ->  pikPanel.add(new JLabel(card.toString())));
        buckets.get(Statics.HERZ).forEach(card ->  herzPanel.add(new JLabel(card.toString())));
        buckets.get(Statics.KARO).forEach(card ->  karoPanel.add(new JLabel(card.toString())));
        panel.revalidate();
        panel.repaint();
    }

    private HashMap<String,List<BaseCard>> DevideCards(List<BaseCard> list){
        list.forEach(card -> card.trump = DokoCards.isTrumpf(card, selectedGame));
        HashMap<String,List<BaseCard>> buckets = new HashMap<>();
        buckets.put(Statics.TRUMPF,list.stream().filter(card -> card.trump).collect(Collectors.toList()));
        buckets.get(Statics.TRUMPF).forEach(list::remove);

        buckets.put(Statics.KREUZ,list.stream().filter(card -> card.suit.equals(Statics.KREUZ)).collect(Collectors.toList()));
        buckets.get(Statics.KREUZ).forEach(list::remove);


        buckets.put(Statics.PIK,list.stream().filter(card -> card.suit.equals(Statics.PIK)).collect(Collectors.toList()));
        buckets.get(Statics.PIK).forEach(list::remove);


        buckets.put(Statics.HERZ,list.stream().filter(card -> card.suit.equals(Statics.HERZ)).collect(Collectors.toList()));
        buckets.get(Statics.HERZ).forEach(list::remove);


        buckets.put(Statics.KARO,list.stream().filter(card -> card.suit.equals(Statics.KARO)).collect(Collectors.toList()));
        buckets.get(Statics.KARO).forEach(list::remove);


        return buckets;
    }
}
