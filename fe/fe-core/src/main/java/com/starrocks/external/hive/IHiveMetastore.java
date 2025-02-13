// This file is licensed under the Elastic License 2.0. Copyright 2021-present, StarRocks Inc.

package com.starrocks.external.hive;

import com.starrocks.catalog.Database;
import com.starrocks.catalog.Table;

import java.util.List;
import java.util.Map;

public interface IHiveMetastore {

    List<String> getAllDatabaseNames();

    List<String> getAllTableNames(String dbName);

    Database getDb(String dbName);

    Table getTable(String dbName, String tableName);

    List<String> getPartitionKeys(String dbName, String tableName);

    Partition getPartition(String dbName, String tblName, List<String> partitionValues);

    Map<String, Partition> getPartitionsByNames(String dbName, String tblName, List<String> partitionNames);

    HivePartitionStatistics getTableStatistics(String dbName, String tblName);

    Map<String, HivePartitionStatistics> getPartitionStatistics(Table table, List<String> partitions);
}
