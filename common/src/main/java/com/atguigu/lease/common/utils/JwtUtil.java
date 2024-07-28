package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static long tokenExpiration =365 * 24 * 60 * 60 * 1000L;
    private static SecretKey tokenSignKey = Keys.hmacShaKeyFor("eMGeGPp0G1lPKEeFgC5XJhyy9a8bX2Gd".getBytes());

    public static String createToken(Long userId, String username) {
        String token = Jwts.builder().
                setSubject("USER_INFO").
                setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)).
                claim("userId", userId).
                claim("username", username).
                signWith(tokenSignKey, SignatureAlgorithm.HS256).
                compact();
        return token;
    }
    public static Claims parseToken(String token){
        if (token==null){
            throw  new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        try{
            JwtParser build = Jwts.parserBuilder().setSigningKey(tokenSignKey).build();
            Jws<Claims> claimsJws = build.parseClaimsJws(token);
            Claims body = claimsJws.getBody();
            return body;
        }
        catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (ClaimJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }

    }

    public static void main(String[] args) {
        System.out.println(createToken(2L, "user"));

    }

}