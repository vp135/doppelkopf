import base.Player;
import base.skat.Stich;
import base.skat.messages.GameSelected;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SkatEndDialog {

    private JPanel panel;
    private JButton buttonOK;

    private List<Stich> stichList;
    private List<Player> players;
    private int rePoints = 0;
    private int kontraPoints= 0;
    private final String reString1;
    private final String kontraString1;
    private String player1String;
    private String player2String;
    private String player3String;
    private int remaining = 120;
    private final GameSelected.GAMES game;

    public SkatEndDialog(GameSelected.GAMES game, List<Player> players, List<Stich> stichList, int skatPoints){
        this.game = game;
        this.players = players;
        this.stichList = stichList;
        calcPoints();
        StringBuilder[] playerBuilder = new StringBuilder[players.size()];
        for(int i = 0; i<players.size(); i++){
            playerBuilder[i] = new StringBuilder();
        }

        stichList.forEach(stich -> {
            players.forEach(player -> {
                if(player.getNumber()==stich.getWinner()){
                    player.addPoints(stich.getPoints());
                    playerBuilder[stich.getWinner()].append(stich.getPoints()).append("<br>");
                }
                else {
                    playerBuilder[player.getNumber()].append("<br>");
                }
            });
            remaining -= stich.getPoints();
        });

        if(game==GameSelected.GAMES.Ramsch){
            players.forEach(player -> {
                if(player.getNumber()==stichList.get(stichList.size()-1).getWinner()){
                    player.addPoints(skatPoints);
                    remaining -= skatPoints;
                    playerBuilder[player.getNumber()].append(skatPoints).append("(Skat)");
                }
                else {
                    playerBuilder[player.getNumber()].append("<br>");
                }
            });
        }
        else{
            players.forEach(player -> {
                if(player.isRe()){
                    remaining-=skatPoints;
                    player.addPoints(skatPoints);
                    playerBuilder[player.getNumber()].append(skatPoints).append("(Skat)");
                }
                else{
                    playerBuilder[player.getNumber()].append("<br>");
                }
            });
        }

        for (int i = 0; i < players.size(); i++) {
            playerBuilder[i].append("</html>");
        }

        if(game== GameSelected.GAMES.Ramsch){
            reString1 = "";
            kontraString1 ="";
        }else{
            rePoints += skatPoints;
            StringBuilder builder1 = new StringBuilder();
            StringBuilder builder2 = new StringBuilder();
            builder1.append("<html>Re(").append(rePoints).append(")<br><hr>");
            builder2.append("<html>Kontra(").append(kontraPoints).append(")<br><hr>");
            players.forEach(player -> {
                if(!player.isSpectator()) {
                    if (player.isRe()) {
                        builder1.append(player.getName()).append("(").append(player.getPoints()).append(")").append("<br><br>");
                    } else {
                        builder2.append(player.getName()).append("(").append(player.getPoints()).append(")").append("<br>");
                    }
                }
            });
            builder1.append("</html>");
            builder2.append("</html>");
            reString1 = builder1.toString();
            kontraString1 = builder2.toString();
        }
        int i = 0;
        for(Player p:players){
            if(!p.isSpectator()) {
                if (i == 0) {
                    player1String = "<html>" + players.get(i).getName() + "(" + players.get(i).getPoints() + ")<br><hr>"
                            + playerBuilder[p.getNumber()].toString();
                } else if (i == 1) {
                    player2String = "<html>" + players.get(i).getName() + "(" + players.get(i).getPoints() + ")<br><hr>"
                            + playerBuilder[p.getNumber()].toString();
                } else if (i == 2) {
                    player3String = "<html>" + players.get(i).getName() + "(" + players.get(i).getPoints() + ")<br><hr>"
                            + playerBuilder[p.getNumber()].toString();
                }
            }
            i++;
        }
    }

    public String getPlayer1String() {
        return player1String;
    }

    public String getPlayer2String() {
        return player2String;
    }

    public String getPlayer3String() {
        return player3String;
    }

    public String getReString1() {
        return reString1;
    }

    public String getKontraString1() {
        return kontraString1;
    }

    public int getRemaining() {
        return remaining;
    }

    public SkatEndDialog(GameSelected.GAMES game, String re1, String kontra1,
                         String player1String, String player2String, String player3String,
                         int remaining) {

        this.game = game;
        this.reString1 = re1;
        this.kontraString1 = kontra1;
        this.player1String = player1String;
        this.player2String = player2String;
        this.player3String = player3String;
        this.remaining = remaining;
        createPanel();
    }

    private void calcPoints(){
        for (Stich s : stichList) {
            if(players.get(s.getWinner()).isRe()){
                rePoints+=s.calculatePoints();
            }
            else{
                kontraPoints+=s.calculatePoints();
            }
        }
    }


    public void createPanel(){
        panel = new JPanel(new BorderLayout());
        if(!reString1.equals("")||!kontraString1.equals("")) {
            JPanel panelTop = new JPanel(new GridLayout(1, 2));
            panelTop.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
            panelTop.add(new JLabel(reString1));
            panelTop.add(new JLabel(kontraString1));
            panel.add(panelTop, BorderLayout.NORTH);
        }

        JPanel panelCenter = new JPanel((new GridLayout(1,3)));
        panelCenter.setBorder(BorderFactory.createLineBorder(Color.GRAY,3));
        panelCenter.add(new JLabel(player1String,JLabel.CENTER));
        panelCenter.add(new JLabel(player2String,JLabel.CENTER));
        panelCenter.add(new JLabel(player3String,JLabel.CENTER));

        JPanel bottomPanel;
        if(remaining>0) {
            bottomPanel= new JPanel(new GridLayout(2,1));
            bottomPanel.add(new JLabel(String.format("Rest: %s", remaining)));
        }
        else{
            bottomPanel = new JPanel(new GridLayout(1,1));
        }
        buttonOK = new JButton("OK");
        bottomPanel.add(buttonOK);

        panel.add(panelCenter, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
    }

    public void showDialog(JFrame frame){
        if(panel!=null) {
            JDialog d = new JDialog(frame);
            buttonOK.addActionListener(e -> d.dispose());
            d.setModal(true);
            d.setTitle("Ergebnis - " + game.name());
            d.getContentPane().add(panel);
            d.pack();
            d.setLocationRelativeTo(frame);
            d.setVisible(true);
        }
    }
}
