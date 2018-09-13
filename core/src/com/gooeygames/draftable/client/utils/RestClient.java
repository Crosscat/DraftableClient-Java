package com.gooeygames.draftable.client.utils;

import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RestClient {

    public String get(String url, Map<String, String> params) throws Exception {
        return request("GET", url.replace("//", "/").replace(":/", "://"), params);
    }

    public String get(String url) throws Exception {
        return get(url, new HashMap<>());
    }

    public String post(String url, Map<String, String> params) throws Exception {
        return request("POST", url.replace("//", "/").replace(":/", "://"), params);
    }

    public String post(String url) throws Exception {
        return post(url, new HashMap<>());
    }

    private String request(String method, String url, Map<String, String> params) throws Exception {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod(method);
            con.setDoOutput(true);
            if (params.size() > 0){
                con.setRequestProperty("Content-Type", "application/json");
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.writeBytes(new GsonBuilder().create().toJson(params)
                        .replace("\\\"", "\"")
                        .replace("\"[", "[")
                        .replace("]\"", "]")
                        .replace("\\r\\n", "")
                        .replace(",]", "]"));
                out.flush();
                out.close();
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            int responseCode = con.getResponseCode();
            con.disconnect();

            if (responseCode != 200){
                throw new Exception("Bad response from server: " + content.toString());
            }

            return content.toString();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
