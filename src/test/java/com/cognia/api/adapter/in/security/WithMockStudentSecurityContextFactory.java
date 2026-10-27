package com.cognia.api.adapter.in.security;

import com.cognia.api.adapter.in.web.WithMockStudent;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockStudentSecurityContextFactory implements WithSecurityContextFactory<WithMockStudent> {
	@Override
	public SecurityContext createSecurityContext(WithMockStudent annotation) {
		var context = SecurityContextHolder.createEmptyContext();
		var principal = UUID.fromString(annotation.value()).toString();
		var authentication = new UsernamePasswordAuthenticationToken(principal, null,
				List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
		context.setAuthentication(authentication);
		return context;
	}
}
