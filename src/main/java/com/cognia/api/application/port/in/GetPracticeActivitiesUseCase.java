package com.cognia.api.application.port.in;
import com.cognia.api.domain.model.Activity;
import java.util.List;
import java.util.UUID;
public interface GetPracticeActivitiesUseCase { List<Activity> getPracticeActivities(UUID conceptId, Integer difficulty); }
