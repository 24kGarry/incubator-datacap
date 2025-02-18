package io.edurt.datacap.plugin.jdbc.clickhouse.generator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.spi.generator.DataType;

@SuppressFBWarnings(value = {"NM_SAME_SIMPLE_NAME_AS_SUPERCLASS"})
public class CreateColumn
        extends io.edurt.datacap.spi.generator.column.CreateColumn
{
    public static CreateColumn create(String name, DataType type)
    {
        return new CreateColumn(name, type);
    }

    protected CreateColumn(String name, DataType type)
    {
        super(name, type);
    }

    @Override
    protected StringBuilder buildBasicSQL()
    {
        StringBuilder sql = new StringBuilder();
        sql.append("`").append(name).append("` ");

        if (length != null) {
            sql.append(type.withLength(length));
        }
        else if (precision != null) {
            sql.append(type.withPrecision(precision, scale));
        }
        else {
            sql.append(type.getValue());
        }

        if (charset != null) {
            sql.append(" CHARACTER SET ").append(charset);
        }

        if (collate != null) {
            sql.append(" COLLATE ").append(collate);
        }

        if (nullable) {
            sql.append(String.format("Nullable(`%s`)", name));
        }

        if (defaultValue != null) {
            if (defaultValue.equalsIgnoreCase("CURRENT_TIMESTAMP")) {
                sql.append(" DEFAULT ").append(defaultValue);
            }
            else {
                sql.append(" DEFAULT '").append(defaultValue).append("'");
            }
        }

        return sql;
    }
}
