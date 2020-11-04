package com.example.demo;

import cn.hutool.core.util.IdUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 腾讯云小程序云直播推、拉流地址生成
 */
@RestController
@RequestMapping(value = "/push")
public class Test {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 云直播后台->域名管理->推流配置->主key 339d2694e2730b883ea5f88ef12ab6b7
     */
    private static final String pushKey = "339d2694e2730b883ea5f88ef12ab6b7";

    /**
     * 推流域名
     */
    private static final String pushDomain = "116966.livepush.myqcloud.com";

    /**
     * 拉流域名
     */
    private static final String pullDomain = "pullDomain";

    private static final String AppName = "live";

    @GetMapping(value = "/generate")
    public String generatePush() {
        LocalDateTime localDateTime =  LocalDateTime.now();
        long nowTime = localDateTime.toEpochSecond(ZoneOffset.of("+8"));

        String streamName = IdUtil.getSnowflake(3,5).nextIdStr();
        String safeUrl = getSafeUrl(pushKey, streamName, nowTime + 12*60*60L);

        /**
         * 推流地址
         */
        System.out.println("rtmp://"+pushDomain+"/"+AppName+"/"+streamName+"?"+safeUrl);

        /**
         * 4种拉流地址格式 分别为rtmp、FLV、M3U8、UDP
         */
        System.out.println("rtmp://"+pullDomain+"/"+AppName+"/"+streamName+"?"+safeUrl);
        System.out.println("http://"+pullDomain+"/"+AppName+"/"+streamName+".flv?"+safeUrl);
        System.out.println("http://"+pullDomain+"/"+AppName+"/"+streamName+".m3u8?"+safeUrl);
        System.out.println("webrtc://"+pullDomain+"/"+AppName+"/"+streamName+"?"+safeUrl);
        return safeUrl;
    }


    /**
     * @param key 主key
     * @param streamName 流id，自己生成的唯一标识
     * @param txTime 有效期
     * @return
     */
    private static String getSafeUrl(String key, String streamName, long txTime) {
        String input = new StringBuilder().append(key).append(streamName).append(Long.toHexString(txTime).toUpperCase()).toString();

        String txSecret = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            txSecret  = byteArrayToHexString(messageDigest.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return txSecret == null ? "" :
                new StringBuilder().append("txSecret=").append(txSecret).append("&").append("txTime=")
                    .append(Long.toHexString(txTime).toUpperCase()). toString();
    }


    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }
}
