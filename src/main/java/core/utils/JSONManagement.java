package core.utils;

import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class JSONManagement {

    public static final Logger logger = Logger.getLogger(JSONManagement.class.getName());

    public static JSONObject readJsonFromUrl(String url) {

        JSONObject output = null;

        try {
            InputStream is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            String jsonText = readAll(rd);
            output = new JSONObject(jsonText);

            is.close();

        } catch (Exception e) {

            logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
