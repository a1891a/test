package com.gec.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Map;

public class JWTUtils {
    /**
     * 生成token  header.payload.singature
     */
    private static final String SING = "XUYAODONG";

    public static String getToken(Map<String, Object> map) {

        Calendar instance = Calendar.getInstance();
        // 默认7天过期
        instance.add(Calendar.DATE, 7);
        //创建jwt builder
        JWTCreator.Builder builder = JWT.create();

        // payload
        map.forEach((k, v) -> {
            builder.withClaim(k, String.valueOf(v));
        });

        String token = builder.withExpiresAt(instance.getTime())  //指定令牌过期时间
                .sign(Algorithm.HMAC256(SING));  // sign
        return token;
    }

    /**
     * 验证token  合法性
     */
    public static String verify(String token) {
        String msg = "";
        // 验证令牌
        try {
            DecodedJWT tokenInfo = getTokenInfo(token);
            msg = "请求成功";
        } catch (SignatureVerificationException e) {
            e.printStackTrace();
            msg = "token签名无效";
        } catch (TokenExpiredException e) {
            e.printStackTrace();
            msg = "token过期";
        } catch (AlgorithmMismatchException e) {
            e.printStackTrace();
            msg = "token算法不一致";
        } catch (Exception e) {
            e.printStackTrace();
            msg = "token无效";
        }
        return msg;
    }

    /**
     * 获取token信息方法
     */
    public static DecodedJWT getTokenInfo(String token) {
        DecodedJWT verify = JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
        return verify;
    }
}
