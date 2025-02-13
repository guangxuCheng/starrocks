// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.analysis;

import com.starrocks.catalog.Column;
import com.starrocks.catalog.ScalarType;
import com.starrocks.qe.ShowResultSetMetaData;
import com.starrocks.sql.ast.AstVisitor;

//SHOW WHITELIST;
public class ShowWhiteListStmt extends ShowStmt {
    private static final ShowResultSetMetaData META_DATA =
            ShowResultSetMetaData.builder()
                    .addColumn(new Column("user_name", ScalarType.createVarchar(20)))
                    .addColumn(new Column("white_list", ScalarType.createVarchar(1000)))
                    .build();

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitShowWhiteListStatement(this, context);
    }

    @Override
    public String toSql() {
        return "SHOW WHITELIST;";
    }
    @Override
    public boolean isSupportNewPlanner() {
        return true;
    }
    @Override
    public ShowResultSetMetaData getMetaData() {
        return META_DATA;
    }
    @Override
    public String toString() {
        return toSql();
    }
}
