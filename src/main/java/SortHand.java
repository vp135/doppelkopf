import base.doko.Card;
import base.doko.messages.GameSelected;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SortHand {

    private static final String KREUZ = "Kreuz";
    private static final String PIK = "Pik";
    private static final String HERZ = "Herz";
    private static final String KARO = "Karo";

    private static final String ZEHN = "10";
    private static final String BUBE = "Bube";
    private static final String DAME = "Dame";
    private static final String KOENIG = "Koenig";
    private static final String ASS = "Ass";


    public static List<Card> sort(List<Card> list, String order, boolean schweinExists){
        switch (order){
            case GameSelected.NORMAL:{
                return sortNormal(list,schweinExists);
            }
            case GameSelected.DAMEN:{
                return sortDamenSolo(list);
            }
            case GameSelected.BUBEN:{
                return sortBubenSolo(list);
            }
            case GameSelected.BUBENDAMEN:{
                return sortBubenDamenSolo(list);
            }
            case GameSelected.FLEISCHLOS:{
                return sortFleischlos(list);
            }
            case GameSelected.ARMUT:{
                return sortArmut(list,schweinExists);
            }
            case GameSelected.KREUZ:{
                return sortKreuz(list);
            }
            case GameSelected.PIK:{
                return sortPik(list);
            }
            case GameSelected.HERZ:{
                return sortHerz(list);
            }
            case GameSelected.KARO:{
                return sortKaro(list,schweinExists);
            }
            default:{
                return list;
            }
        }
    }

    public static List<Card> sortNormal(List<Card> list_orig, boolean schweinExists){
        List<Card> list = new ArrayList<>(list_orig);
        List<Card> frc = new ArrayList<>();
        List<Card> fuchslist = list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList());
        if (fuchslist.size() > 1 || schweinExists) {
            fuchslist.forEach(p -> {
                frc.add(p);
                list.remove(p);
            });
        }
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        return frc;
    }

    public static List<Card> sortBubenSolo(List<Card> list_orig){
        List<Card> list = new ArrayList<>(list_orig);
        List<Card> frc = new ArrayList<>();

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });


        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });



        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        return frc;
    }

    public static List<Card> sortDamenSolo(List<Card> list_orig){
        List<Card> list = new ArrayList<>(list_orig);
        List<Card> frc = new ArrayList<>();

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });


        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });



        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        return frc;
    }

    public static List<Card> sortBubenDamenSolo(List<Card> list_orig){
        List<Card> list = new ArrayList<>(list_orig);
        List<Card> frc = new ArrayList<>();

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });


        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });



        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        return frc;
    }

    public static List<Card> sortFleischlos(List<Card> list_orig){
        List<Card> list = new ArrayList<>(list_orig);
        List<Card> frc = new ArrayList<>();

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        return frc;
    }

    public static List<Card> sortKreuz(List<Card> list_orig){
        List<Card> list = new ArrayList<>(list_orig);
        List<Card> frc = new ArrayList<>();

        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });



        return frc;
    }

    public static List<Card> sortPik(List<Card> list_orig){
        List<Card> list = new ArrayList<>(list_orig);
        List<Card> frc = new ArrayList<>();

        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });


        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        return frc;
    }

    public static List<Card> sortHerz(List<Card> list_orig){
        List<Card> list = new ArrayList<>(list_orig);
        List<Card> frc = new ArrayList<>();
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(DAME))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });

        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(BUBE))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(HERZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });



        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KREUZ) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(PIK) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ASS))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(ZEHN))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        list.stream().filter(p -> p.farbe.equals(KARO) && p.value.equals(KOENIG))
                .collect(Collectors.toList()).forEach(p -> {
            frc.add(p);
            list.remove(p);
        });
        return frc;
    }

    public static List<Card> sortKaro(List<Card> hand, boolean schweinExists) {
        return sortNormal(hand, schweinExists);
    }

    public static List<Card> sortArmut(List<Card> hand, boolean schweinExists) {
        return sortNormal(hand, schweinExists);
    }
}
