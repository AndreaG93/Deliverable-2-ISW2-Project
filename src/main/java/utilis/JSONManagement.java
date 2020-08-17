package utilis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class JSONManagement {

    private JSONManagement() {
    }

    public static JSONObject readJsonFromUrl(String url) {

        JSONObject output = null;

        try (InputStream inputStream = new URL(url).openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String jsonText = readAll(bufferedReader);
            output = new JSONObject(jsonText);

        } catch (Exception e) {

            Logger.getLogger(JSONManagement.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }

    public static String[] extractFieldFromJsonArray(JSONArray input, String jsonField) {

        String[] output = new String[input.length()];

        for (int index = 0; index < input.length(); index++)
            output[index] = input.getJSONObject(index).getString(jsonField);

        return output;
    }

    private static String readAll(Reader reader) {

        StringBuilder stringBuilder = new StringBuilder();

        try {

            int character;
            while ((character = reader.read()) != -1) {
                stringBuilder.append((char) character);
            }

        } catch (Exception e) {

            Logger.getLogger(JSONManagement.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return stringBuilder.toString();
    }
}
