package com.cognia.api.adapter.in.web;

import com.cognia.api.adapter.in.security.WithMockStudentSecurityContextFactory;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockStudentSecurityContextFactory.class)
public @interface WithMockStudent {
	String value();
}
