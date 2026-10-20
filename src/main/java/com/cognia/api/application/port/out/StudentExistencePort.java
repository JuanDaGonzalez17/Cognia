package com.cognia.api.application.port.out;
import java.util.UUID;
public interface StudentExistencePort { boolean exists(UUID studentId); }
