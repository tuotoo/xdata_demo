import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class xdata {
    private static String baseURL = "https://xdata.tuotoo.org/v1";

    public static void main(String[] args) {
        try {
            String token = getToken(
                    "ACCESS_KEY",
                    "ACCESS_TOKEN"
            ).get("AccessToken").getAsString();
            System.out.println("token: " + token);

            String tmpURL = "https://mp.weixin.qq.com/s?src=11&timestamp=1565600401&ver=1785&signature=1-NXhUZTV2euAR6HhFXH3kp8Kj4QhJfVDOA5nkx2wCxVMR7Hi*bOUq7OpyqkqhT8DIwAj0Gpx9XJbxMZZjLtvg78YV*yFZ6hPvEBLKjUEHxOkq9tqzqRpKHC0fJAKzRA&new=1";
            String url = urlToStatic(tmpURL, token);
            System.out.println("url to static: " + url);

            JsonObject readLike = getReadLike(url, token);
            System.out.println("read: " + readLike.get("read_num").getAsInt() +
                    " like: " + readLike.get("like_num").getAsInt());

            JsonObject content = getContent(url, token);
            System.out.println("user_name: " + content.get("user_name").getAsString() +
                    "\n nick_name: " + content.get("nick_name").getAsString() +
                    "\n round_head_img: " + content.get("round_head_img").getAsString() +
                    "\n title: " + content.get("title").getAsString() +
                    "\n desc: " + content.get("desc").getAsString() +
                    "\n signature: " + content.get("signature").getAsString() +
                    "\n content_noencode: " + content.get("content_noencode").getAsString()
            );

            JsonArray articles = getArticles("中国移动", 10, token);
            for (JsonElement article : articles) {
                System.out.println("Title: "+article.getAsJsonObject().get("Title").getAsString());
                System.out.println("URL: "+article.getAsJsonObject().get("URL").getAsString());
                System.out.println("Datetime: "+article.getAsJsonObject().get("Datetime").getAsInt());
                System.out.println("Author: "+article.getAsJsonObject().get("Author").getAsString());
                System.out.println("Cover: "+article.getAsJsonObject().get("Cover").getAsString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonElement get(String addr, String token) throws IOException {
        URL url = new URL(addr);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        if (token != null) {
            connection.setRequestProperty("Authorization", token);
        }
        connection.setRequestMethod("GET");
        connection.connect();
        if (connection.getResponseCode() != 200) {
            throw new IOException(connection.getResponseMessage());
        }
        common result = new Gson().fromJson(
                new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8
                ),
                common.class);
        if (result.code != 0) {
            throw new IOException(result.message);
        }
        return result.data;
    }

    private static JsonObject getToken(String accessKey, String accessToken) throws IOException {
        return get(String.format(
                baseURL + "/open/token?AccessKey=%s&AccessSecret=%s",
                URLEncoder.encode(accessKey, "UTF-8"), URLEncoder.encode(accessToken, "UTF-8")), null).getAsJsonObject();
    }

    private static String urlToStatic(String tmpURL, String token) throws IOException {
        return get(String.format(
                baseURL + "/open/url/static?url=%s", URLEncoder.encode(tmpURL, "UTF-8")),
                token).getAsString();
    }

    private static JsonObject getReadLike(String url, String token) throws IOException {
        return get(String.format(
                baseURL + "/open/url/likes?url=%s", URLEncoder.encode(url, "UTF-8")),
                token).getAsJsonObject();
    }

    private static JsonObject getContent(String url, String token) throws IOException {
        return get(String.format(
                baseURL + "/open/article/content?url=%s", URLEncoder.encode(url, "UTF-8")),
                token).getAsJsonObject();
    }

    private static JsonArray getArticles(String name, int count, String token) throws IOException {
        return get(String.format(
                baseURL + "/open/mp/articles?name=%s&count=%s",
                URLEncoder.encode(name, "UTF-8"),
                URLEncoder.encode(Integer.toString(count), "UTF-8")
                ),
                token).getAsJsonArray();
    }
}
