package com.cognia.api.adapter.out.persistence;

import com.cognia.api.application.port.out.StudentAccountRepository;
import com.cognia.api.application.service.EmailAlreadyRegisteredException;
import com.cognia.api.domain.model.StudentAccount;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StudentAccountJdbcAdapter implements StudentAccountRepository {
	private final JdbcTemplate jdbc;

	public StudentAccountJdbcAdapter(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Optional<StudentAccount> findByEmail(String email) {
		return jdbc.query("SELECT id, correo, password_hash, nombre_completo, activo FROM estudiante WHERE lower(correo) = ?",
				this::mapAccount, email.trim().toLowerCase(Locale.ROOT)).stream().findFirst();
	}

	@Override
	public Optional<StudentAccount> findById(UUID id) {
		return jdbc.query("SELECT id, correo, password_hash, nombre_completo, activo FROM estudiante WHERE id = ?",
				this::mapAccount, id).stream().findFirst();
	}

	@Override
	public StudentAccount save(StudentAccount account) {
		try {
			jdbc.update("INSERT INTO estudiante (id, nombre_completo, correo, password_hash, activo) VALUES (?, ?, ?, ?, ?)",
					account.id(), account.fullName(), account.email(), account.passwordHash(), account.active());
			return account;
		} catch (DuplicateKeyException exception) {
			throw new EmailAlreadyRegisteredException();
		}
	}

	@Override
	public boolean existsByEmail(String email) {
		return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM estudiante WHERE lower(correo) = ?)",
				Boolean.class, email.trim().toLowerCase(Locale.ROOT)));
	}

	private StudentAccount mapAccount(ResultSet row, int rowNumber) throws SQLException {
		return new StudentAccount(row.getObject("id", UUID.class), row.getString("correo"), row.getString("password_hash"),
				row.getString("nombre_completo"), StudentAccount.Role.STUDENT, row.getBoolean("activo"));
	}
}
