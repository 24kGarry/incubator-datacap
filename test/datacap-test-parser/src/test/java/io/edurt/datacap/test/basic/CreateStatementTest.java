package io.edurt.datacap.test.basic;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.sql.SQLParser;
import io.edurt.datacap.sql.node.option.TableOption;
import io.edurt.datacap.sql.statement.CreateTableStatement;
import io.edurt.datacap.sql.statement.SQLStatement;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

@SuppressFBWarnings(value = {"JUA_DONT_ASSERT_INSTANCEOF_IN_TESTS"})
public class CreateStatementTest
{
    @Test
    public void testFlinkSql()
    {
        String sql = "CREATE TABLE source_table (\n" +
                "  COL1 INT,\n" +
                "  COL2 TIMESTAMP(0),\n" +
                "  COL3 VARCHAR(14),\n" +
                "  COL4 CHAR(1),\n" +
                "  PRIMARY KEY (COL1) NOT ENFORCED\n" +
                ") WITH\n" +
                "  (\n" +
                "    'connector' = 'mysql',\n" +
                "    'hostname' = '127.0.0.1',\n" +
                "    'port' = '3306',\n" +
                "    'username' = 'root',\n" +
                "    'password' = 'root',\n" +
                "    'database' = 'default',\n" +
                "    'table' = 'example'\n" +
                "  );\n" +
                "\n" +
                "\n" +
                "CREATE TABLE transform_table (\n" +
                "  COL1 INT,\n" +
                "  COL2 TIMESTAMP(0),\n" +
                "  COL3 VARCHAR(14),\n" +
                "  COL4 CHAR(1),\n" +
                "  PRIMARY KEY (COL1) NOT ENFORCED\n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                "  'topic' = 'sink-topic',\n" +
                "  'properties.bootstrap.servers' = '127.0.0.1:9092',\n" +
                "  'properties.group.id' = 'sink-consumer-group',\n" +
                "  'format' = 'debezium-json',\n" +
                "  'properties.request.timeout.ms' = '120000',\n" +
                "  'properties.session.timeout.ms' = '90000'\n" +
                ");\n" +
                "\n" +
                "INSERT INTO sink_table\n" +
                "SELECT\n" +
                "  COL1,\n" +
                "  COL2,\n" +
                "  COL3,\n" +
                "  COL4\n" +
                "FROM\n" +
                "  transform_table;";
        SQLStatement stmt = SQLParser.parse(sql);

        assertTrue(stmt instanceof CreateTableStatement);
        CreateTableStatement statement = (CreateTableStatement) stmt;

        List<TableOption> options = statement.getOptions();
        System.out.println(options);
    }
}
