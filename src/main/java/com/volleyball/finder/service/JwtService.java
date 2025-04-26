package com.volleyball.finder.service;

import com.volleyball.finder.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateToken(User user);

    boolean validateToken(String token);

    Claims getClaims(String token);

    <T> T getClaim(String token, String claimName, Class<T> clazz);
}