package com.volleyball.finder.service.impl;

import com.volleyball.finder.config.JwtConfig;
import com.volleyball.finder.entity.User;
import com.volleyball.finder.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtConfig jwtConfig;

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getJwtExpiration());

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("id", user.getId())
                .claim("nickname", user.getNickname())
                .claim("lineId", user.getLineId())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtConfig.secretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        getClaims(token);
        return true;
    }

    @Override
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtConfig.secretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public <T> T getClaim(String token, String claimName, Class<T> clazz) {
        Claims claims = getClaims(token);
        return claims.get(claimName, clazz);
    }
}