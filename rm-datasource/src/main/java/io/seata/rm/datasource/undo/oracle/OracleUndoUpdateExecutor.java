/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.rm.datasource.undo.oracle;

import io.seata.common.exception.ShouldNeverHappenException;
import io.seata.rm.datasource.ColumnUtils;
import io.seata.rm.datasource.sql.struct.Field;
import io.seata.rm.datasource.sql.struct.Row;
import io.seata.rm.datasource.sql.struct.TableRecords;
import io.seata.rm.datasource.undo.AbstractUndoExecutor;
import io.seata.rm.datasource.undo.SQLUndoLog;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type oracle undo update executor.
 *
 * @author ccg
 */
public class OracleUndoUpdateExecutor extends AbstractUndoExecutor {

    /**
     * UPDATE a SET x = ?, y = ?, z = ? WHERE pk = ?
     */
    private static final String UPDATE_SQL_TEMPLATE = "UPDATE %s SET %s WHERE %s = ?";

    @Override
    protected String buildUndoSQL() {
        TableRecords beforeImage = sqlUndoLog.getBeforeImage();
        List<Row> beforeImageRows = beforeImage.getRows();
        if (beforeImageRows == null || beforeImageRows.size() == 0) {
            throw new ShouldNeverHappenException("Invalid UNDO LOG"); // TODO
        }

        Row row = beforeImageRows.get(0);
        Field pkField = row.primaryKeys().get(0);
        List<Field> nonPkFields = row.nonPrimaryKeys();
        String updateColumns = nonPkFields.stream()
            .map(field -> ColumnUtils.addEscape(field.getName(), ColumnUtils.Escape.STANDARD) + " = ?")
            .collect(Collectors.joining(", "));
        return String.format(UPDATE_SQL_TEMPLATE, ColumnUtils.addEscape(sqlUndoLog.getTableName(), ColumnUtils.Escape.STANDARD),
            updateColumns, ColumnUtils.addEscape(pkField.getName(), ColumnUtils.Escape.STANDARD));
    }

    /**
     * Instantiates a new My sql undo update executor.
     *
     * @param sqlUndoLog the sql undo log
     */
    public OracleUndoUpdateExecutor(SQLUndoLog sqlUndoLog) {
        super(sqlUndoLog);
    }

    @Override
    protected TableRecords getUndoRows() {
        return sqlUndoLog.getBeforeImage();
    }
}
