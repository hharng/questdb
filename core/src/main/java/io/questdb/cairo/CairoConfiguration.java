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

package io.questdb.cairo;

import io.questdb.BuildInformation;
import io.questdb.ConfigPropertyKey;
import io.questdb.ConfigPropertyValue;
import io.questdb.FactoryProvider;
import io.questdb.Metrics;
import io.questdb.TelemetryConfiguration;
import io.questdb.VolumeDefinitions;
import io.questdb.cairo.sql.SqlExecutionCircuitBreakerConfiguration;
import io.questdb.cutlass.text.TextConfiguration;
import io.questdb.std.CharSequenceObjHashMap;
import io.questdb.std.FilesFacade;
import io.questdb.std.IOURingFacade;
import io.questdb.std.IOURingFacadeImpl;
import io.questdb.std.NanosecondClock;
import io.questdb.std.NanosecondClockImpl;
import io.questdb.std.ObjObjHashMap;
import io.questdb.std.Rnd;
import io.questdb.std.RostiAllocFacade;
import io.questdb.std.RostiAllocFacadeImpl;
import io.questdb.std.datetime.DateFormat;
import io.questdb.std.datetime.DateLocale;
import io.questdb.std.datetime.TimeZoneRules;
import io.questdb.std.datetime.microtime.MicrosecondClock;
import io.questdb.std.datetime.microtime.MicrosecondClockImpl;
import io.questdb.std.datetime.millitime.MillisecondClock;
import io.questdb.std.datetime.millitime.MillisecondClockImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.LongSupplier;

public interface CairoConfiguration {

    long O_ASYNC = 0x40;
    long O_DIRECT = 0x4000;
    long O_NONE = 0;
    long O_SYNC = 0x80;
    ThreadLocal<Rnd> RANDOM = new ThreadLocal<>();

    boolean attachPartitionCopy();

    default boolean disableColumnPurgeJob() {
        return false;
    }

    boolean enableTestFactories();

    default boolean freeLeakedReaders() {
        return true;
    }

    /**
     * All effective configuration values are seen by the server instance.
     *
     * @return key value pairs of the configuration
     */
    default @Nullable ObjObjHashMap<ConfigPropertyKey, ConfigPropertyValue> getAllPairs() {
        return null;
    }

    boolean getAllowTableRegistrySharedWrite();

    // the '+' is used to prevent overlap with table names
    @NotNull
    default String getArchivedCrashFilePrefix() {
        return "crash+";
    }

    @NotNull
    String getAttachPartitionSuffix();

    DateFormat getBackupDirTimestampFormat();

    int getBackupMkDirMode();

    // null disables backups
    CharSequence getBackupRoot();

    CharSequence getBackupTempDirName();

    int getBinaryEncodingMaxLength();

    int getBindVariablePoolSize();

    @NotNull
    BuildInformation getBuildInformation();

    boolean getCairoSqlLegacyOperatorPrecedence();

    @NotNull
    CharSequence getCheckpointRoot(); // same as root/../.checkpoint

    @NotNull
    SqlExecutionCircuitBreakerConfiguration getCircuitBreakerConfiguration();

    int getColumnIndexerQueueCapacity();

    int getColumnPurgeQueueCapacity();

    long getColumnPurgeRetryDelay();

    long getColumnPurgeRetryDelayLimit();

    double getColumnPurgeRetryDelayMultiplier();

    int getColumnPurgeTaskPoolCapacity();

    default long getCommitLatency() {
        return 30_000_000; // 30s
    }

    int getCommitMode();

    @NotNull
    CharSequence getConfRoot(); // same as root/../conf

    @NotNull
    LongSupplier getCopyIDSupplier();

    int getCopyPoolCapacity();

    int getCountDistinctCapacity();

    double getCountDistinctLoadFactor();

    int getCreateAsSelectRetryCount();

    int getCreateTableColumnModelPoolCapacity();

    long getCreateTableModelBatchSize();

    long getDataAppendPageSize();

    long getDataIndexKeyAppendPageSize();

    long getDataIndexValueAppendPageSize();

    long getDatabaseIdHi();

    long getDatabaseIdLo();

    @NotNull
    CharSequence getDbDirectory(); // env['cairo.root'], defaults to db

    @NotNull
    DateLocale getDefaultDateLocale();

    int getDefaultSeqPartTxnCount();

    boolean getDefaultSymbolCacheFlag();

    int getDefaultSymbolCapacity();

    int getDetachedMkDirMode();

    int getDoubleToStrCastScale();

    default Map<String, String> getEnv() {
        return System.getenv();
    }

    int getExplainPoolCapacity();

    @NotNull
    FactoryProvider getFactoryProvider();

    int getFileOperationRetryCount();

    @NotNull
    FilesFacade getFilesFacade();

    int getFloatToStrCastScale();

    long getGroupByAllocatorDefaultChunkSize();

    long getGroupByAllocatorMaxChunkSize();

    int getGroupByMapCapacity();

    int getGroupByMergeShardQueueCapacity();

    int getGroupByPoolCapacity();

    long getGroupByPresizeMaxCapacity();

    long getGroupByPresizeMaxHeapSize();

    int getGroupByShardingThreshold();

    @NotNull
    default IOURingFacade getIOURingFacade() {
        return IOURingFacadeImpl.INSTANCE;
    }

    long getIdleCheckInterval();

    int getInactiveReaderMaxOpenPartitions();

    long getInactiveReaderTTL();

    long getInactiveWalWriterTTL();

    long getInactiveWriterTTL();

    int getIndexValueBlockSize();

    long getInsertModelBatchSize();

    int getInsertModelPoolCapacity();

    int getLatestByQueueCapacity();

    @NotNull
    CharSequence getLegacyCheckpointRoot(); // same as root/../snapshot

    boolean getLogLevelVerbose();

    boolean getLogSqlQueryProgressExe();

    DateFormat getLogTimestampFormat();

    String getLogTimestampTimezone();

    DateLocale getLogTimestampTimezoneLocale();

    TimeZoneRules getLogTimestampTimezoneRules();

    int getMaxCrashFiles();

    int getMaxFileNameLength();

    int getMaxSqlRecompileAttempts();

    int getMaxSwapFileCount();

    int getMaxSymbolNotEqualsCount();

    int getMaxUncommittedRows();

    int getMetadataPoolCapacity();

    @NotNull
    default MicrosecondClock getMicrosecondClock() {
        return MicrosecondClockImpl.INSTANCE;
    }

    @NotNull
    default MillisecondClock getMillisecondClock() {
        return MillisecondClockImpl.INSTANCE;
    }

    long getMiscAppendPageSize();

    int getMkDirMode();

    @NotNull
    default NanosecondClock getNanosecondClock() {
        return NanosecondClockImpl.INSTANCE;
    }

    int getO3CallbackQueueCapacity();

    int getO3ColumnMemorySize();

    int getO3CopyQueueCapacity();

    int getO3LagCalculationWindowsSize();

    default double getO3LagDecreaseFactor() {
        return 0.5;
    }

    default double getO3LagIncreaseFactor() {
        return 1.5;
    }

    int getO3LastPartitionMaxSplits();

    /**
     * Default commit lag in microseconds for new tables. This value
     * can be overridden with 'create table' statement.
     *
     * @return upper bound of "commit lag" in micros
     */
    long getO3MaxLag();

    int getO3MemMaxPages();

    long getO3MinLag();

    int getO3OpenColumnQueueCapacity();

    int getO3PartitionQueueCapacity();

    int getO3PurgeDiscoveryQueueCapacity();

    // the '+' is used to prevent overlap with table names
    @NotNull
    default String getOGCrashFilePrefix() {
        return "hs_err_pid+";
    }

    int getPageFrameReduceColumnListCapacity();

    int getPageFrameReduceQueueCapacity();

    int getPageFrameReduceRowIdListCapacity();

    int getPageFrameReduceShardCount();

    int getParallelIndexThreshold();

    int getPartitionEncoderParquetCompressionCodec();

    int getPartitionEncoderParquetCompressionLevel();

    int getPartitionEncoderParquetDataPageSize();

    int getPartitionEncoderParquetRowGroupSize();

    int getPartitionEncoderParquetVersion();

    long getPartitionO3SplitMinSize();

    int getPartitionPurgeListCapacity();

    int getQueryCacheEventQueueCapacity();

    int getQueryRegistryPoolSize();

    @NotNull
    default Rnd getRandom() {
        Rnd rnd = RANDOM.get();
        if (rnd == null) {
            RANDOM.set(rnd = new Rnd(
                            getNanosecondClock().getTicks(),
                            getMicrosecondClock().getTicks()
                    )
            );
        }
        return rnd;
    }

    int getReaderPoolMaxSegments();

    int getRenameTableModelPoolCapacity();

    int getRepeatMigrationsFromVersion();

    int getRndFunctionMemoryMaxPages();

    int getRndFunctionMemoryPageSize();

    @NotNull
    String getRoot(); // some folder with suffix env['cairo.root'] e.g. /.../db

    @NotNull
    default RostiAllocFacade getRostiAllocFacade() {
        return RostiAllocFacadeImpl.INSTANCE;
    }

    boolean getSampleByDefaultAlignmentCalendar();

    int getSampleByIndexSearchPageSize();

    long getSequencerCheckInterval();

    /**
     * Returns database instance id. The instance id is used by the snapshot recovery mechanism:
     * on database start the id is compared with the id stored in the checkpoint, if any. If the ids
     * are different, snapshot recovery is being triggered.
     *
     * @return instance id.
     */
    @NotNull
    CharSequence getSnapshotInstanceId();

    long getSpinLockTimeout();

    int getSqlAsOfJoinLookAhead();

    int getSqlCharacterStoreCapacity();

    int getSqlCharacterStoreSequencePoolCapacity();

    int getSqlColumnPoolCapacity();

    int getSqlCompilerPoolCapacity();

    int getSqlCopyBufferSize();

    // null or empty input root disables "copy" sql
    CharSequence getSqlCopyInputRoot();

    CharSequence getSqlCopyInputWorkRoot();

    int getSqlCopyLogRetentionDays();

    long getSqlCopyMaxIndexChunkSize();

    int getSqlCopyQueueCapacity();

    int getSqlDistinctTimestampKeyCapacity();

    double getSqlDistinctTimestampLoadFactor();

    int getSqlExpressionPoolCapacity();

    double getSqlFastMapLoadFactor();

    int getSqlHashJoinLightValueMaxPages();

    int getSqlHashJoinLightValuePageSize();

    int getSqlHashJoinValueMaxPages();

    int getSqlHashJoinValuePageSize();

    int getSqlJitBindVarsMemoryMaxPages();

    int getSqlJitBindVarsMemoryPageSize();

    int getSqlJitIRMemoryMaxPages();

    int getSqlJitIRMemoryPageSize();

    int getSqlJitMode();

    int getSqlJitPageAddressCacheThreshold();

    int getSqlJoinContextPoolCapacity();

    int getSqlJoinMetadataMaxResizes();

    /**
     * These holds table metadata, which is usually quite small. 16K page should be adequate.
     *
     * @return memory page size
     */
    int getSqlJoinMetadataPageSize();

    long getSqlLatestByRowCount();

    int getSqlLexerPoolCapacity();

    int getSqlMapMaxPages();

    int getSqlMapMaxResizes();

    int getSqlMaxNegativeLimit();

    int getSqlModelPoolCapacity();

    int getSqlOrderByRadixSortThreshold();

    int getSqlPageFrameMaxRows();

    int getSqlPageFrameMinRows();

    int getSqlParallelWorkStealingThreshold();

    int getSqlParquetFrameCacheCapacity();

    int getSqlSmallMapKeyCapacity();

    long getSqlSmallMapPageSize();

    int getSqlSortKeyMaxPages();

    long getSqlSortKeyPageSize();

    int getSqlSortLightValueMaxPages();

    long getSqlSortLightValuePageSize();

    int getSqlSortValueMaxPages();

    int getSqlSortValuePageSize();

    int getSqlUnorderedMapMaxEntrySize();

    int getSqlWindowInitialRangeBufferSize();

    int getSqlWindowMaxRecursion();

    int getSqlWindowRowIdMaxPages();

    int getSqlWindowRowIdPageSize();

    int getSqlWindowStoreMaxPages();

    int getSqlWindowStorePageSize();

    int getSqlWindowTreeKeyMaxPages();

    int getSqlWindowTreeKeyPageSize();

    int getStrFunctionMaxBufferLength();

    long getSystemDataAppendPageSize();

    int getSystemO3ColumnMemorySize();

    @NotNull
    CharSequence getSystemTableNamePrefix();

    long getSystemWalDataAppendPageSize();

    long getSystemWalEventAppendPageSize();

    long getTableRegistryAutoReloadFrequency();

    int getTableRegistryCompactionThreshold();

    @NotNull
    TelemetryConfiguration getTelemetryConfiguration();

    CharSequence getTempRenamePendingTablePrefix();

    @NotNull
    TextConfiguration getTextConfiguration();

    int getTxnScoreboardEntryCount();

    int getVectorAggregateQueueCapacity();

    @NotNull
    VolumeDefinitions getVolumeDefinitions();

    int getWalApplyLookAheadTransactionCount();

    long getWalApplyTableTimeQuota();

    long getWalDataAppendPageSize();

    boolean getWalEnabledDefault();

    long getWalEventAppendPageSize();

    double getWalLagRowsMultiplier();

    long getWalMaxLagSize();

    int getWalMaxLagTxnCount();

    int getWalMaxSegmentFileDescriptorsCache();

    long getWalPurgeInterval();

    default int getWalPurgeWaitBeforeDelete() {
        return 0;
    }

    int getWalRecreateDistressedSequencerAttempts();

    /**
     * If after a commit a WAL segment has more than this number of rows, roll the next transaction onto a new segment.
     * <p>
     *
     * @see #getWalSegmentRolloverSize()
     */
    long getWalSegmentRolloverRowCount();

    /**
     * If after a commit a WAL segment is larger than this size, roll the next transaction onto a new segment.
     * <p>
     *
     * @see #getWalSegmentRolloverRowCount()
     */
    long getWalSegmentRolloverSize();

    int getWalTxnNotificationQueueCapacity();

    int getWalWriterPoolMaxSegments();

    int getWindowColumnPoolCapacity();

    int getWithClauseModelPoolCapacity();

    long getWorkStealTimeoutNanos();

    long getWriterAsyncCommandBusyWaitTimeout();

    long getWriterAsyncCommandMaxTimeout();

    int getWriterCommandQueueCapacity();

    long getWriterCommandQueueSlotSize();

    long getWriterFileOpenOpts();

    int getWriterTickRowsCountMod();

    /**
     * A flag to enable/disable checkpoint recovery mechanism. Defaults to {@code true}.
     *
     * @return enable/disable flag for recovering from the checkpoint
     */
    boolean isCheckpointRecoveryEnabled();

    boolean isDevModeEnabled();

    boolean isGroupByPresizeEnabled();

    boolean isIOURingEnabled();

    boolean isMultiKeyDedupEnabled();

    boolean isO3QuickSortEnabled();

    boolean isParallelIndexingEnabled();

    boolean isPartitionEncoderParquetStatisticsEnabled();

    boolean isPartitionO3OverwriteControlEnabled();

    boolean isReadOnlyInstance();

    boolean isSqlJitDebugEnabled();

    boolean isSqlOrderBySortEnabled();

    boolean isSqlParallelFilterEnabled();

    boolean isSqlParallelFilterPreTouchEnabled();

    boolean isSqlParallelGroupByEnabled();

    boolean isTableTypeConversionEnabled();

    boolean isWalApplyEnabled();

    boolean isWalSupported();

    boolean isWriterMixedIOEnabled();

    /**
     * This is a flag to enable/disable making table directory names different to table names for non-WAL tables.
     * When it is enabled directory name of table TRADE becomes TRADE~, so that ~ sign is added at the end.
     * The flag is enabled in tests and disabled in released code for backward compatibility. Tests verify that
     * we do not rely on the fact that table directory name is the same as table name.
     *
     * @return true if mangling of directory names for non-WAL tables is enabled, false otherwise.
     */
    boolean mangleTableDirNames();

    default void populateSettings(CharSequenceObjHashMap<CharSequence> settings) {
    }

    boolean useFastAsOfJoin();

    Metrics getMetrics();
}
