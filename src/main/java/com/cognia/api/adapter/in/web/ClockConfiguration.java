package com.cognia.api.adapter.in.web;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfiguration {
	@Bean public Clock applicationClock(){return Clock.systemUTC();}
}
