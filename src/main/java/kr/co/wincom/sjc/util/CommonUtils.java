package kr.co.wincom.sjc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

public class CommonUtils {
    // URL 형식 체크
    public static void checkUrl(String url) throws Exception {
        new URL(url).toURI();
    }

    // leftUrl에 맞춰서 rightUrl을 생성함
    public static String makeUrl(String strLeftUrl, String strRightUrl) throws Exception {
        if (strRightUrl.endsWith("/")) {
            strRightUrl = strRightUrl.substring(0, strRightUrl.length() - 1);
        }

        URL leftUrl = new URL(strLeftUrl);
        URL rightUrl = new URL(strRightUrl);

        // http://ip:port 형식으로 입력시
        if (StringUtils.isBlank(rightUrl.getFile())) {
            int port = rightUrl.getPort();
            String strPort = port == -1 ? "" : ":" + port;

            return (rightUrl.getProtocol() + "://" + rightUrl.getHost() + strPort + leftUrl.getFile());
        } else {
            return rightUrl.toString();
        }
    }

    // JSON 데이터를 보기 좋게 만듦
    public static String makeJsonPrettyData(String jsonData) throws Exception {
        JsonElement jsonElement = JsonParser.parseString(jsonData);

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        return gson.toJson(jsonElement);
    }

    // JSON 데이터일 때 ':' 기준으로 오른쪽 데이터는 녹색으로 치환
    public static String replaceColor(String data) throws Exception {
        int idx1 = data.indexOf(":");

        if (idx1 > -1) {
            String firstData = data.substring(0, idx1 + 1).replace(" ", "&nbsp;").replace("<", "&lt;").replace(">", "&gt;");
            String lastData = data.substring(idx1 + 1).replace(" ", "&nbsp;").replace("<", "&lt;").replace(">", "&gt;");
            String replaceLastData = "<font style='color:#298A08;'>" + lastData + "</font>";

            return (firstData + replaceLastData);
        } else {
            return data.replace(" ", "&nbsp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
