package com.cognia.api.adapter.out.persistence;
import com.cognia.api.application.port.out.StudentExistencePort; import java.util.UUID; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class StudentExistenceJpaAdapter implements StudentExistencePort { private final JdbcTemplate jdbc; public StudentExistenceJpaAdapter(JdbcTemplate jdbc){this.jdbc=jdbc;} public boolean exists(UUID id){return Boolean.TRUE.equals(jdbc.queryForObject("select exists(select 1 from estudiante where id = ?)",Boolean.class,id));} }
