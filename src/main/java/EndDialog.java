import base.Player;
import base.Stich;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EndDialog{

    private JPanel panel;
    private JButton buttonOK;

    private final String reString1;
    private List<Stich> stichList;
    private List<Player> players;
    private int rePoints = 0;
    private int kontraPoints= 0;
    private final String kontraString1;
    private final String reString2;
    private final String kontraString2;

    public EndDialog(List<Player> players, List<Stich> stichList){
        this.players = players;
        this.stichList = stichList;
        calcPoints();
        StringBuilder builderRe = new StringBuilder();
        StringBuilder builderKontra = new StringBuilder();
        builderRe.append("<html>");
        builderKontra.append("<html>");
        players.forEach(player -> player.setPoints(0));
        stichList.forEach(stich -> {
            players.get(stich.getWinner()).addPoints(stich.getPoints());
            //rest.set(rest.get() - stich.getPoints());
            if(players.get(stich.getWinner()).isRe()){
                builderRe.append(stich.getPoints()).append("(").append(players.get(stich.getWinner()).getName()).append(")");
            }
            else {
                builderKontra.append(stich.getPoints()).append("(").append(players.get(stich.getWinner()).getName()).append(")");
            }
            builderKontra.append("<br>");
            builderRe.append("<br>");
        });
        builderRe.append("</html>");
        builderKontra.append("</html>");

        StringBuilder builder1 = new StringBuilder();
        StringBuilder builder2 = new StringBuilder();
        builder1.append("<html>Re(").append(rePoints).append(")<br><hr>");
        builder2.append("<html>Kontra(").append(kontraPoints).append(")<br><hr>");
        players.forEach(player -> {
            if(!player.isSpectator()) {
                if (player.isRe()) {
                    builder1.append(player.getName()).append("(").append(player.getPoints()).append(")").append("<br>");
                } else {
                    builder2.append(player.getName()).append("(").append(player.getPoints()).append(")").append("<br>");
                }
            }
        });
        builder1.append("</html>");
        builder2.append("</html>");

        reString1 = builder1.toString();
        reString2 = builderRe.toString();
        kontraString1 = builder2.toString();
        kontraString2 = builderKontra.toString();

    }


    public String getReString1() {
        return reString1;
    }

    public String getKontraString1() {
        return kontraString1;
    }

    public String getReString2() {
        return reString2;
    }

    public String getKontraString2() {
        return kontraString2;
    }

    public EndDialog(String re1, String re2, String kontra1, String kontra2) {

        this.reString1 = re1;
        this.reString2 = re2;
        this.kontraString1 = kontra1;
        this.kontraString2 = kontra2;
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
        JPanel panelTop = new JPanel(new GridLayout(1,2));
        panelTop.setBorder(BorderFactory.createLineBorder(Color.GRAY,3));
        panelTop.add(new JLabel(reString1));
        panelTop.add(new JLabel(kontraString1));

        JPanel panelCenter = new JPanel((new GridLayout(1,2)));
        panelCenter.setBorder(BorderFactory.createLineBorder(Color.GRAY,3));
        panelCenter.add(new JLabel(reString2));
        panelCenter.add(new JLabel(kontraString2));

        panel.add(panelTop,BorderLayout.NORTH);
        panel.add(panelCenter, BorderLayout.CENTER);
        buttonOK = new JButton("OK");
        panel.add(buttonOK, BorderLayout.SOUTH);
    }

    public void showDialog(Main main){
        if(panel!=null) {
            JDialog d = new JDialog(main.mainFrame);
            //d.setSize(300,300);
            buttonOK.addActionListener(e -> d.dispose());
            d.setModal(true);
            d.setTitle("Ergebnis");
            d.getContentPane().add(panel);
            d.pack();
            d.setLocationRelativeTo(main.mainFrame);
            d.setVisible(true);
        }
    }
}
