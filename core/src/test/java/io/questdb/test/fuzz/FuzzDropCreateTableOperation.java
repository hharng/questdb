/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.test.fuzz;

import io.questdb.cairo.*;
import io.questdb.cairo.security.AllowAllSecurityContext;
import io.questdb.cairo.sql.RecordMetadata;
import io.questdb.cairo.sql.TableRecordMetadata;
import io.questdb.cairo.vm.Vm;
import io.questdb.cairo.vm.api.MemoryMARW;
import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.std.Chars;
import io.questdb.std.Os;
import io.questdb.std.Rnd;
import io.questdb.std.str.Path;

public class FuzzDropCreateTableOperation implements FuzzTransactionOperation {
    static final Log LOG = LogFactory.getLog(FuzzDropCreateTableOperation.class);
    private boolean dedupTsColumn;

    @Override
    public boolean apply(Rnd rnd, CairoEngine engine, TableWriterAPI tableWriter, int virtualTimestampIndex) {
        TableToken tableToken = tableWriter.getTableToken();
        String tableName = tableToken.getTableName();
        boolean isWal = engine.isWalTable(tableToken);
        int timestampIndex = tableWriter.getMetadata().getTimestampIndex();
        RecordMetadata copyDenseMeta = denseMetaCopy(tableWriter.getMetadata(), timestampIndex);

        try (MemoryMARW vm = Vm.getCMARWInstance(); Path path = new Path()) {
            engine.releaseInactive();
            while (true) {
                try {
                    LOG.info().$("dropping table ").$(tableToken.getDirName()).$();
                    engine.dropTable(path, tableToken);
                    break;
                } catch (CairoException ignore) {
                    if (!isWal) {
                        engine.releaseInactive();
                        Os.sleep(50);
                    } else {
                        Os.pause();
                    }
                }
            }

            engine.createTable(
                    AllowAllSecurityContext.INSTANCE,
                    vm,
                    path,
                    false,
                    new TableStructMetadataAdapter(
                            tableName,
                            isWal,
                            copyDenseMeta,
                            engine.getConfiguration(),
                            PartitionBy.DAY,
                            copyDenseMeta.getTimestampIndex()
                    ),
                    false
            );
        }
        return true;
    }

    public void setDedupEnable(boolean dedupTsColumn) {
        this.dedupTsColumn = dedupTsColumn;
    }

    private RecordMetadata denseMetaCopy(TableRecordMetadata metadata, int timestampIndex) {
        GenericRecordMetadata newMeta = new GenericRecordMetadata();
        int tsIndex = -1;
        int denseIndex = 0;
        int tableReaderIndex = 0;
        for (int i = 0; i < metadata.getColumnCount(); i++) {
            int type = metadata.getColumnType(i);
            if (type > 0) {
                CharSequence name = metadata.getColumnName(i);

                newMeta.add(
                        new TableColumnMetadata(
                                Chars.toString(name),
                                type,
                                metadata.isColumnIndexed(i),
                                metadata.getIndexValueBlockCapacity(i),
                                true,
                                null,
                                denseIndex++,
                                i == timestampIndex && dedupTsColumn && metadata.getTableToken().isWal()
                        )
                );

                if (i == timestampIndex) {
                    tsIndex = newMeta.getColumnCount() - 1;
                }
            }
        }
        newMeta.setTimestampIndex(tsIndex);
        return newMeta;
    }
}
