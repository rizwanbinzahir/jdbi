package org.jdbi.v3.core.statement;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.NotImplementedException;
import org.jdbi.v3.core.Time;
import org.jdbi.v3.core.rule.DatabaseRule;
import org.jdbi.v3.core.rule.SqliteDatabaseRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlStatementTest {
    private static final String CREATE_TABLE = "create table foo(bar int primary key not null)";
    private static final ZonedDateTime START = LocalDate.of(2018, Month.JANUARY, 1).atTime(LocalTime.of(12, 0, 0)).atZone(ZoneOffset.UTC);
    private static final AtomicInteger CLOCK_COUNTER = new AtomicInteger(0);

    @Rule
    public DatabaseRule db = new SqliteDatabaseRule();

    @Before
    public void before() {
        Clock clock = Mockito.mock(Clock.class);
        Mockito.when(clock.instant()).thenAnswer(invocation -> START.plusHours(CLOCK_COUNTER.getAndIncrement()).toInstant());
        db.getJdbi().getConfig(Time.class).setClock(clock);
    }

    @Test
    public void testTiming() {
        CountDownLatch logs = new AtomicBoolean(false);

        db.getJdbi()
            .configure(SqlStatements.class, sql -> sql.setSqlLogger(new SqlLogger() {
                @Override
                public void logBeforeExecution(StatementContext context) {
                    context.getExecutionMoment()
                }

                @Override
                public void logAfterExecution(StatementContext context) {
                    context.getCompletionMoment()
                }

                @Override
                public void logException(StatementContext context, SQLException ex) {
                    context.getExceptionMoment()
                }
            }))
            .useHandle(h -> h.createUpdate(CREATE_TABLE).execute());

        assertThat(loggerCalled).isTrue();
    }
}
