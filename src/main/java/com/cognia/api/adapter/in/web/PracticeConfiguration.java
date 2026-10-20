package com.cognia.api.adapter.in.web;
import com.cognia.api.domain.service.ActivityCorrectionService; import org.springframework.context.annotation.*;
@Configuration public class PracticeConfiguration { @Bean ActivityCorrectionService activityCorrectionService(){return new ActivityCorrectionService();} }
