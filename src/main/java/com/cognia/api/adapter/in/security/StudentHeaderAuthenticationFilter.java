package com.cognia.api.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Development and test compatibility only; it is registered only for those profiles. */
public class StudentHeaderAuthenticationFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		var hasBearerHeader = request.getHeader("Authorization") != null;
		var currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
		if (!hasBearerHeader && (currentAuthentication == null || !currentAuthentication.isAuthenticated())) {
			var studentId = request.getHeader("X-Student-Id");
			if (studentId != null) {
				try {
					var authentication = new UsernamePasswordAuthenticationToken(UUID.fromString(studentId).toString(), null,
							List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				} catch (IllegalArgumentException exception) {
					SecurityContextHolder.clearContext();
				}
			}
		}
		chain.doFilter(request, response);
	}
}
