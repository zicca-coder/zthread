package com.zicca.zthread.nacos.cloud.example.toolkit;

import com.alibaba.nacos.common.codec.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;

/**
 * 钉钉签名工具类
 * @author zicca
 */
public class DingUtil {
    public static void main(String[] args) throws Exception{

        /**
         * https://oapi.dingtalk.com/robot/send?access_token=ddc75363b5eb98c3c382b94bda9a0ca896f51664f236491c1e3bf98bc3491a7e&timestamp=1757324556632&sign=hA2hndIHjh%2B3oBVChDyrvts5exEtfBcwUjPwkm7ogQ4%3D
         */
        Long timestamp = System.currentTimeMillis();
        String secret = "SEC5254d6db5f4e8fb146f0d9e143795319218dab1b8f4497b6faea3976a7c8e0d4";
        String accessToken = "ddc75363b5eb98c3c382b94bda9a0ca896f51664f236491c1e3bf98bc3491a7e";
        String webhookUrl = "https://oapi.dingtalk.com/robot/send";

        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");

        // 构建完整的请求URL
        String url = webhookUrl + "?access_token=" + accessToken + "&timestamp=" + timestamp + "&sign=" + sign;
        System.out.println("请求URL: " + url);

        // 准备发送的消息内容
        String message = "{\n" +
                "    \"msgtype\": \"text\",\n" +
                "    \"text\": {\n" +
                "        \"content\": \"Hello, 这是一条测试消息\"\n" +
                "    }\n" +
                "}";

        // 发送HTTP POST请求到钉钉机器人
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        try (java.io.OutputStream os = connection.getOutputStream()) {
            byte[] input = message.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("消息发送成功");
        } else {
            System.out.println("消息发送失败，响应码：" + responseCode);
        }
    }
}
