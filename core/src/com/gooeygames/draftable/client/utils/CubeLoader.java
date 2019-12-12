package com.gooeygames.draftable.client.utils;

import com.gooeygames.draftable.client.cards.Card;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CubeLoader {
    public static String getCubeDataFromFile(String filepath){
        try {
            return "[" + new String(Files.readAllBytes(Paths.get(filepath))) + "]";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCubeDataFromScryfall(List<String> cardNames){
        RestClient client = new RestClient();
        for (String cardName : cardNames){
            try {
                client.get("https://api.scryfall.com/cards/named?exact=" + cardName);
                Card card =
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
