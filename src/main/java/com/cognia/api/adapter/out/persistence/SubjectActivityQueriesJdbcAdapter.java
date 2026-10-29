package com.cognia.api.adapter.out.persistence;

import com.cognia.api.application.port.in.SubjectActivityQueries.Activity;
import com.cognia.api.application.port.in.SubjectActivityQueries.UpcomingReview;
import com.cognia.api.application.port.out.SubjectActivityQueriesPort;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SubjectActivityQueriesJdbcAdapter implements SubjectActivityQueriesPort {
	private final JdbcTemplate jdbc;

	public SubjectActivityQueriesJdbcAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

	@Override
	public List<Activity> history(UUID studentId, UUID subjectId, int limit) {
		String sql = """
				select id, type, concept_id, concept_name, score, passed, activity_date, attempt
				from (
				  select s.id, s.tipo_evaluacion as type, s.concepto_id as concept_id,
				    case when s.tipo_evaluacion='DIAGNOSTICO' then 'Diagnóstico inicial'
				         when s.tipo_evaluacion='EXAMEN_GLOBAL' then 'Examen global'
				         else c.nombre end as concept_name,
				    s.puntaje_obtenido as score, s.aprobada as passed,
				    s.fecha_finalizacion as activity_date, s.intento_numero as attempt
				  from sesion_evaluacion s left join concepto c on c.id=s.concepto_id
				  where s.estudiante_id=? and s.asignatura_id=? and s.estado='COMPLETADA'
				  union all
				  select r.id, 'REPASO', null, 'Repaso',
				    case when r.total_tarjetas_revisadas=0 then null
				         else 100.0*r.total_recordadas/r.total_tarjetas_revisadas end,
				    null, r.fecha_fin, null
				  from sesion_repaso r
				  where r.estudiante_id=? and r.asignatura_id=? and r.fecha_fin is not null
				) activity_history
				order by activity_date desc, id
				limit ?
				""";
		return jdbc.query(sql, this::mapActivity, studentId, subjectId, studentId, subjectId, limit);
	}

	@Override
	public List<UpcomingReview> upcoming(UUID studentId, UUID subjectId, Instant now, int limit) {
		return jdbc.query("""
				select p.id as progress_id, p.concepto_id as concept_id, c.nombre as concept_name,
				 p.fecha_proximo_repaso, p.intervalo_dias, p.fallos_consecutivos
				from progreso_repaso p
				join concepto c on c.id=p.concepto_id
				join tarjeta_repaso t on t.id=p.tarjeta_id and t.concepto_id=p.concepto_id and t.activa=true
				where p.estudiante_id=? and c.asignatura_id=? and p.fecha_proximo_repaso>?
				order by p.fecha_proximo_repaso, p.id
				limit ?
				""", this::mapUpcoming, studentId, subjectId, Timestamp.from(now), limit);
	}

	private Activity mapActivity(ResultSet rs, int row) throws SQLException {
		return new Activity(rs.getObject("id", UUID.class), rs.getString("type"), rs.getObject("concept_id", UUID.class),
			rs.getString("concept_name"), nullableDouble(rs, "score"), nullableBoolean(rs, "passed"),
			rs.getTimestamp("activity_date").toInstant(), nullableInteger(rs, "attempt"));
	}
	private UpcomingReview mapUpcoming(ResultSet rs, int row) throws SQLException {
		return new UpcomingReview(rs.getObject("progress_id", UUID.class), rs.getObject("concept_id", UUID.class),
			rs.getString("concept_name"), rs.getTimestamp("fecha_proximo_repaso").toInstant(),
			rs.getInt("intervalo_dias"), rs.getInt("fallos_consecutivos"));
	}
	private Double nullableDouble(ResultSet rs, String column) throws SQLException {
		double value = rs.getDouble(column);
		return rs.wasNull() ? null : value;
	}
	private Boolean nullableBoolean(ResultSet rs, String column) throws SQLException {
		boolean value = rs.getBoolean(column);
		return rs.wasNull() ? null : value;
	}
	private Integer nullableInteger(ResultSet rs, String column) throws SQLException {
		int value = rs.getInt(column);
		return rs.wasNull() ? null : value;
	}
}
