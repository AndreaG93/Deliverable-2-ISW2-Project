package utilis.common;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class JSONManagement {

    public static JSONObject readJsonFromUrl(String url) {

        JSONObject output = null;

        try {
            InputStream is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            String jsonText = readAll(rd);
            output = new JSONObject(jsonText);

            is.close();

        } catch (Exception e) {

            Logger.getLogger(JSONManagement.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }

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
