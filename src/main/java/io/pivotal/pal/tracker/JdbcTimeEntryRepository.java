package io.pivotal.pal.tracker;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private JdbcTemplate jdbcTemplate;


    public JdbcTimeEntryRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry inTimeEntry) {
        KeyHolder holder= new GeneratedKeyHolder();
       // Number key = holder.getKey();
        PreparedStatementCreator psc = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement statement = con.prepareStatement("INSERT INTO time_entries (project_id, user_id, date, hours) VALUES (?,?,?,?)",RETURN_GENERATED_KEYS);
                statement.setLong(1, inTimeEntry.getProjectId());
                statement.setLong(2, inTimeEntry.getUserId());
                statement.setDate(3, Date.valueOf(inTimeEntry.getDate()));
                statement.setInt(4, inTimeEntry.getHours());
                return statement;
            }
        };

        jdbcTemplate.update(psc,holder);
        return find(holder.getKey().longValue());
    }

    @Override
    public TimeEntry find(long id) {
        return jdbcTemplate.query("SELECT * FROM time_entries WHERE id=?", new Object[]{id}, extractor);
    }

    @Override
    public List<TimeEntry> list() {
        return jdbcTemplate.query("SELECT * FROM time_entries", mapper);
    }

    @Override
    public TimeEntry update(long id, TimeEntry newTimeEntry) {
        jdbcTemplate.update("UPDATE time_entries SET project_id=?, user_id=?, date=?, hours=? WHERE id=?",
                newTimeEntry.getProjectId(),
                newTimeEntry.getUserId(),
                newTimeEntry.getDate(),
                newTimeEntry.getHours(),
                id);
        return find(id);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM time_entries WHERE id=?", id);
    }

    private final RowMapper<TimeEntry> mapper = (rs, rowNum) -> new TimeEntry(
            rs.getLong("id"),
            rs.getLong("project_id"),
            rs.getLong("user_id"),
            rs.getDate("date").toLocalDate(),
            rs.getInt("hours")
    );

    private final ResultSetExtractor<TimeEntry> extractor =
            (rs) -> rs.next() ? mapper.mapRow(rs, 1) : null;
}
