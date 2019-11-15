import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Formatter;

public class xdata {
    private static String baseURL = "https://api.xdata.tuotoo.com/v1/open";

    public static void main(String[] args) {
        try {
            String token = getToken(
                    "ACCESS_KEY",
                    "ACCESS_SECRET"
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
                System.out.println("Title: " + article.getAsJsonObject().get("Title").getAsString());
                System.out.println("URL: " + article.getAsJsonObject().get("URL").getAsString());
                System.out.println("Datetime: " + article.getAsJsonObject().get("Datetime").getAsInt());
                System.out.println("Author: " + article.getAsJsonObject().get("Author").getAsString());
                System.out.println("Cover: " + article.getAsJsonObject().get("Cover").getAsString());
            }

            articles = getArticlesWithDate("中国移动", "2019-09-01T00:00:00+08:00", "2019-09-10T00:00:00+08:00", token);
            for (JsonElement article : articles) {
                System.out.println("Title: " + article.getAsJsonObject().get("Title").getAsString());
                System.out.println("URL: " + article.getAsJsonObject().get("URL").getAsString());
                System.out.println("Datetime: " + article.getAsJsonObject().get("Datetime").getAsInt());
                System.out.println("Author: " + article.getAsJsonObject().get("Author").getAsString());
                System.out.println("Cover: " + article.getAsJsonObject().get("Cover").getAsString());
            }

            articles = searchArticles("中国移动", 10, token);
            for (JsonElement article : articles) {
                System.out.println("Title: " + article.getAsJsonObject().get("title").getAsString());
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

    private static String URLFormat(String format, String... args) throws UnsupportedEncodingException {
        ArrayList<Object> encodedArgs = new ArrayList<>();
        for (String arg : args) {
            encodedArgs.add(URLEncoder.encode(arg, "UTF-8"));
        }
        return new Formatter().format(format, encodedArgs.toArray()).toString();
    }

    private static JsonObject getToken(String accessKey, String accessSecret) throws IOException {
        return get(URLFormat(
                baseURL + "/token?AccessKey=%s&AccessSecret=%s",
                accessKey, accessSecret
        ), null).getAsJsonObject();
    }

    private static String urlToStatic(String tmpURL, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/url/static?url=%s",
                tmpURL),
                token).getAsString();
    }

    private static JsonObject getReadLike(String url, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/url/likes?url=%s",
                url),
                token).getAsJsonObject();
    }

    private static JsonObject getContent(String url, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/article/content?url=%s",
                url
        ), token).getAsJsonObject();
    }

    private static JsonArray getArticles(String name, int fetchDepth, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/mp/articles?name=%s&fetchDepth=%s",
                name, Integer.toString(fetchDepth)
        ), token).getAsJsonArray();
    }

    private static JsonArray getArticlesWithDate(String name, String start, String end, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/mp/articles?name=%s&start=%s&end=%s",
                name, start, end
        ), token).getAsJsonArray();
    }

    private static JsonArray searchArticles(String keyword, int count, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/article/search?key=%s&count=%s",
                keyword, Integer.toString(count)
        ), token).getAsJsonArray();
    }

    private static JsonObject getMPHistory(String username, int offset, String start, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/mp/history?name=%s&offset=%s&start=%s",
                username, Integer.toString(offset), start
        ), token).getAsJsonObject();
    }

    private static JsonObject searchArticlesByPage(String key, int page, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/article/search/page?key=%s&page=%s",
                key, Integer.toString(page)
        ), token).getAsJsonObject();
    }

    private static JsonObject getMPInfo(String name, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/mp/info?name=%s",
                name
        ), token).getAsJsonObject();
    }

    private static JsonObject searchMP(String key, int page, String token) throws IOException {
        return get(URLFormat(
                baseURL + "/mp/search?key=%s&page=%s",
                key, Integer.toString(page)
        ), token).getAsJsonObject();
    }
}
