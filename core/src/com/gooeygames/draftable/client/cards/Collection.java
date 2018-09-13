package com.gooeygames.draftable.client.cards;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Collection {
    private Map<CardColor, List<Card>> collection;
    private List<Card> cardList;

    public Collection(){
        collection = new LinkedHashMap<>();
        for (CardColor value : CardColor.values()){
            collection.put(value, new ArrayList<>());
        }
        cardList = new ArrayList<>();
    }

    public Map<CardColor, List<Card>> get(){
        return collection;
    }

    public void add(Card card){
        Map<String, Integer> symbols = getSymbols(card.mana_cost);
        card.convertedManaCost = getConvertedManaCost(symbols);
        card.color = getColor(symbols);

        collection.get(card.color).add(card);
        Collections.sort(collection.get(card.color), new SortByCMC());

        cardList.add(card);
    }

    private Map<String, Integer> getSymbols(String manacost){
        Map<String, Integer> symbolMap = new HashMap<>();

        manacost = manacost
                .replaceAll("\\{", "")
                .replaceAll("\\}", "")
                .replaceAll("/", "")
                .replaceAll("P", "");

        for (int i = 0; i < manacost.length(); i++){
            char c = manacost.charAt(i);
            if (symbolMap.containsKey(String.valueOf(c))){
                symbolMap.put(String.valueOf(c), symbolMap.get(String.valueOf(c)) + 1);
            }else{
                symbolMap.put(String.valueOf(c), 1);
            }
        }
        return symbolMap;
    }

    private int getConvertedManaCost(Map<String, Integer> symbolMap){
        int total = 0;
        for (String key : symbolMap.keySet()){
            if (StringUtils.isNumeric(key)){
                total += Integer.parseInt(key);
            }else{
                if (key.equals("X")) continue;
                total += symbolMap.get(key);
            }
        }
        return total;
    }

    private CardColor getColor(Map<String, Integer> symbolMap){
        boolean hasCost = hasCost(symbolMap);
        String dominantSymbol = getDominantSymbol(symbolMap);

        if (!hasCost && dominantSymbol.isEmpty()){
            return CardColor.LAND;
        }else if (dominantSymbol.length() == 1){
            switch (dominantSymbol.toUpperCase()){
                case "W": return CardColor.WHITE;
                case "U": return CardColor.BLUE;
                case "B": return CardColor.BLACK;
                case "R": return CardColor.RED;
                case "G": return CardColor.GREEN;
            }
        }else if (dominantSymbol.length() > 1){
            return CardColor.MULTICOLOR;
        }

        return CardColor.COLORLESS;
    }

    private String getDominantSymbol(Map<String, Integer> symbolMap){
        StringBuilder symbol = new StringBuilder();
        for (String key : symbolMap.keySet()) {
            if (StringUtils.isNumeric(key)) continue;
            if (key.equals("X")) continue;
            symbol.append(key);
        }
        return symbol.toString();
    }

    private boolean hasCost(Map<String, Integer> symbolMap){
        for (String key : symbolMap.keySet()) {
            if (StringUtils.isNumeric(key) || key.equals("X")){
                return true;
            }
        }
        return false;
    }
}

class SortByCMC implements Comparator<Card>{
    public int compare(Card a, Card b){
        return a.convertedManaCost - b.convertedManaCost;
    }
}