package com.gooeygames.draftable.client.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CubeLoader {
    public static String getCubeDataFromFile(String filepath){
        try {
            return "[" + new String(Files.readAllBytes(Paths.get(filepath))) + "]";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
