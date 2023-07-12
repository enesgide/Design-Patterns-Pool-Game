package PoolGame.config;

import PoolGame.objects.*;
import PoolGame.GameManager;
import java.util.ArrayList;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PocketReader implements Reader {
    /**
     * Parses the JSON file and builds the pockets.
     *
     * @param path        The path to the JSON file.
     * @param gameManager The game manager.
     */
    public void parse(String path, GameManager gameManager) {
        JSONParser parser = new JSONParser();
        ArrayList<Pocket> pockets = new ArrayList<Pocket>();

        Table table = gameManager.getTable();

        try {
            Object object = parser.parse(new FileReader(path));

            JSONObject jsonObject = (JSONObject) object;

            JSONObject jsonTable = (JSONObject) jsonObject.get("Table");

            JSONArray jsonArray = (JSONArray) jsonTable.get("pockets");

            if (jsonArray == null) {
                table.initialisePockets();
                return;
            }

            for (Object obj: jsonArray) {
                JSONObject jsonPocket = (JSONObject) obj;
                Double radius = (Double) jsonPocket.get("radius");
                Double positionX = (Double) ((JSONObject) jsonPocket.get("position")).get("x");
                Double positionY = (Double) ((JSONObject) jsonPocket.get("position")).get("y");

                Pocket pocket = new Pocket(positionX, positionY, radius);
                pockets.add(pocket);
            }

            table.setPockets(pockets);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}