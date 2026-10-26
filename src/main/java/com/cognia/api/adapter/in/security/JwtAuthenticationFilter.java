package com.cognia.api.adapter.in.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenService tokens;

	public JwtAuthenticationFilter(JwtTokenService tokens) {
		this.tokens = tokens;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		var authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
			try {
				var identity = tokens.parse(authorization.substring(7).trim());
				var authentication = new UsernamePasswordAuthenticationToken(identity.studentId().toString(), null,
						List.of(new SimpleGrantedAuthority("ROLE_" + identity.role().name())));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | IllegalArgumentException exception) {
				SecurityContextHolder.clearContext();
			}
		}
		chain.doFilter(request, response);
	}
}
