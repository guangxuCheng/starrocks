// This file is licensed under the Elastic License 2.0. Copyright 2021-present, StarRocks Inc.
package com.starrocks.sql.analyzer;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.starrocks.analysis.DdlStmt;
import com.starrocks.common.AnalysisException;
import com.starrocks.common.Pair;
import com.starrocks.qe.ConnectContext;
import com.starrocks.sql.ast.AlterSystemStmt;
import com.starrocks.sql.ast.AstVisitor;
import com.starrocks.sql.ast.BackendClause;
import com.starrocks.sql.ast.ComputeNodeClause;
import com.starrocks.sql.ast.FrontendClause;
import com.starrocks.sql.ast.ModifyBackendAddressClause;
import com.starrocks.sql.ast.ModifyBrokerClause;
import com.starrocks.sql.ast.ModifyFrontendAddressClause;
import com.starrocks.system.SystemInfoService;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class AlterSystemStmtAnalyzer {

    public static void analyze(DdlStmt ddlStmt, ConnectContext session) {
        new AlterSystemStmtAnalyzer.AlterSystemStmtAnalyzerVisitor().analyze((AlterSystemStmt) ddlStmt, session);
    }

    static class AlterSystemStmtAnalyzerVisitor extends AstVisitor<Void, ConnectContext> {
        public void analyze(AlterSystemStmt statement, ConnectContext session) {
            visit(statement.getAlterClause(), session);
        }

        @Override
        public Void visitComputeNodeClause(ComputeNodeClause computeNodeClause, ConnectContext context) {
            try {
                for (String hostPort : computeNodeClause.getHostPorts()) {
                    Pair<String, Integer> pair = SystemInfoService.validateHostAndPort(hostPort);
                    computeNodeClause.getHostPortPairs().add(pair);
                }
                Preconditions.checkState(!computeNodeClause.getHostPortPairs().isEmpty());
            } catch (AnalysisException e) {
                throw new SemanticException("compute node host or port is wrong!");
            }
            return null;
        }

        @Override
        public Void visitBackendClause(BackendClause backendClause, ConnectContext context) {
            try {
                for (String hostPort : backendClause.getHostPorts()) {
                    Pair<String, Integer> pair = SystemInfoService.validateHostAndPort(hostPort);
                    backendClause.getHostPortPairs().add(pair);
                }
                Preconditions.checkState(!backendClause.getHostPortPairs().isEmpty());
            } catch (AnalysisException e) {
                throw new SemanticException("backend host or port is wrong!");
            }
            return null;
        }

        @Override
        public Void visitFrontendClause(FrontendClause frontendClause, ConnectContext context) {
            try {
                Pair<String, Integer> pair = SystemInfoService.validateHostAndPort(frontendClause.getHostPort());
                frontendClause.setHost(pair.first);
                frontendClause.setPort(pair.second);
                Preconditions.checkState(!Strings.isNullOrEmpty(frontendClause.getHost()));
            } catch (AnalysisException e) {
                throw new SemanticException("frontend host or port is wrong!");
            }
            return null;
        }

        @Override
        public Void visitModifyFrontendHostClause(ModifyFrontendAddressClause clause, ConnectContext context) {
            checkModifyHostClause(clause.getSrcHost(), clause.getDestHost());
            return null;
        }

        @Override
        public Void visitModifyBackendHostClause(ModifyBackendAddressClause clause, ConnectContext context) {
            checkModifyHostClause(clause.getSrcHost(), clause.getDestHost());
            return null;
        }

        private void checkModifyHostClause(String srcHost, String destHost) {
            try {
                boolean srcHostIsIP = InetAddressValidator.getInstance().isValidInet4Address(srcHost);
                boolean destHostIsIP = InetAddressValidator.getInstance().isValidInet4Address(destHost);
                if (srcHostIsIP && destHostIsIP) {
                    throw new SemanticException("Can't change ip to ip");
                }
                // If can't get an ip through the srcHost/destHost, will throw UnknownHostException
                if (!srcHostIsIP) {
                    InetAddress.getByName(srcHost);
                }
                if (!destHostIsIP) {
                    InetAddress.getByName(destHost);                    
                }
            } catch (UnknownHostException e) {
                throw new SemanticException("unknown host " + e.getMessage());
            }
        }

        @Override
        public Void visitModifyBrokerClause(ModifyBrokerClause clause, ConnectContext context) {
            validateBrokerName(clause);
            try {
                if (clause.getOp() != ModifyBrokerClause.ModifyOp.OP_DROP_ALL) {
                    for (String hostPort : clause.getHostPorts()) {
                        Pair<String, Integer> pair = SystemInfoService.validateHostAndPort(hostPort);
                        clause.getHostPortPairs().add(pair);
                    }
                    Preconditions.checkState(!clause.getHostPortPairs().isEmpty());
                }
            } catch (AnalysisException e) {
                throw new SemanticException("broker host or port is wrong!");
            }
            return null;
        }

        private void validateBrokerName(ModifyBrokerClause clause) {
            try {
                clause.validateBrokerName();
            } catch (AnalysisException e) {
                throw new SemanticException(e.getMessage());
            }
        }
    }
}
