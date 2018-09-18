package com.gooeygames.draftable.client.utils;

import com.gooeygames.draftable.client.cards.Card;
import com.gooeygames.draftable.client.jsonobjects.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerProxy {

    private static RestClient restClient = new RestClient();

    public static int createDraft(String serverUrl, String cubeData, String draftType, String cardsPerPlayer) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("cube", cubeData);
        params.put("draftType", draftType);
        params.put("cardsPerPlayer", cardsPerPlayer);
        String response = restClient.post(serverUrl + "/draft/create/", params);
        return new GsonBuilder().create().fromJson(response, Draft.class).draftId;
    }

    public static void startDraft(String serverUrl, String draftId) throws Exception {
        restClient.post(serverUrl + "/draft/" + draftId + "/start");
    }

    public static Drafter joinDraft(String serverUrl, String draftId) throws Exception {
        String response = restClient.post(serverUrl + "/draft/" + draftId + "/join");
        return new GsonBuilder().create().fromJson(response, Drafter.class);
    }

    public static boolean isStarted(String serverUrl, String draftId) throws Exception {
        String response = restClient.get(serverUrl + "/draft/" + draftId + "/started");
        return new GsonBuilder().create().fromJson(response, Started.class).Started;
    }

    public static List<Card> getCube(String serverUrl, String draftId) throws Exception {
        String response = restClient.get(serverUrl + "/draft/" + draftId + "/cube");
        return toCardList(response);
    }

    public static List<Card> getPile(String serverUrl, String draftId, int pileId) throws Exception {
        String response = restClient.get(serverUrl + "/draft/" + draftId + "/piles/" + pileId);
        return toCardList(response);
    }

    public static DraftStatus getStatus(String serverUrl, String draftId) throws Exception {
        String response = restClient.get(serverUrl + "/draft/" + draftId + "/status");
        return new GsonBuilder().create().fromJson(response, DraftStatus.class);
    }

    public static List<Card> takePile(String serverUrl, String draftId, int pileId) throws Exception {
        String response = restClient.post(serverUrl + "/draft/" + draftId + "/piles/" + pileId + "/take");
        return toCardList(response);
    }

    public static void skipPile(String serverUrl, String draftId, int pileId) throws Exception {
        restClient.post(serverUrl + "/draft/" + draftId + "/piles/" + pileId + "/skip");
    }

    private static List<Card> toCardList(String data){
        List<Card> cards = new GsonBuilder().create().fromJson(data, new TypeToken<List<Card>>(){}.getType());
        while (cards.remove(null));
        return cards;
    }
}
