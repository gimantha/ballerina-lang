/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jdbc.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ballerinalang.jdbc.Constants;
import org.ballerinalang.jdbc.exceptions.DatabaseException;
import org.ballerinalang.jdbc.exceptions.ErrorGenerator;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.XADataSource;

/**
 * Native SQL Connector.
 *
 * @since 0.8.0
 */
public class SQLDatasource {

    private HikariDataSource hikariDataSource;
    private String peerAddress;
    private String databaseProductName;
    private boolean xaConn;
    private boolean globalDatasource;
    private AtomicInteger clientCounter = new AtomicInteger(0);
    private Lock mutex = new ReentrantLock();
    private boolean poolShutdown = false;

    private SQLDatasource(SQLDatasourceParams sqlDatasourceParams) {
        this.globalDatasource = sqlDatasourceParams.isGlobalDatasource;
        peerAddress = sqlDatasourceParams.jdbcUrl;
        buildDataSource(sqlDatasourceParams);
        try {
            xaConn = isXADataSource();
        } catch (DatabaseException e) {
            throw ErrorGenerator.getSQLDatabaseError(e);
        }
        try (Connection con = getSQLConnection()) {
            databaseProductName = con.getMetaData().getDatabaseProductName().toLowerCase(Locale.ENGLISH);
        } catch (SQLException e) {
            throw ErrorGenerator
                    .getSQLDatabaseError(e, "error while obtaining connection for " + Constants.CONNECTOR_NAME + ", ");
        }
    }

    /**
     * Retrieve the {@link SQLDatasource}} object corresponding to the provided JDBC URL in
     * {@link SQLDatasource.SQLDatasourceParams}.
     * Creates a datasource if it doesn't exist.
     *
     * @param sqlDatasourceParams datasource parameters required to retrieve the JDBC URL for datasource lookup and
     *                            initialization of the newly created datasource if it doesn't exists
     * @param connectionPool The connectionPool object associated with the jdbc client
     * @return The existing or newly created {@link SQLDatasource} object
     */
    public static SQLDatasource retrieveDatasource(SQLDatasource.SQLDatasourceParams sqlDatasourceParams,
                                                   MapValue<String, Object> connectionPool) {
        PoolKey poolKey = new PoolKey(sqlDatasourceParams.jdbcUrl, sqlDatasourceParams.options);
        Map<PoolKey, SQLDatasource> hikariDatasourceMap = SQLDatasourceUtils
                .retrieveDatasourceContainer(connectionPool);
        // map could be null only in a local pool creation scenario
        if (hikariDatasourceMap == null) {
            hikariDatasourceMap = SQLDatasourceUtils.putDatasourceContainer(connectionPool, new ConcurrentHashMap<>());
        }
        SQLDatasource existingSqlDatasource = hikariDatasourceMap.get(poolKey);
        SQLDatasource sqlDatasourceToBeReturned = existingSqlDatasource;
        if (existingSqlDatasource != null) {
            existingSqlDatasource.acquireMutex();
            try {
                if (!existingSqlDatasource.isPoolShutdown()) {
                    existingSqlDatasource.incrementClientCounter();
                } else {
                    sqlDatasourceToBeReturned = hikariDatasourceMap.compute(poolKey,
                            (key, value) -> createAndInitDatasource(sqlDatasourceParams));
                }
            } finally {
                existingSqlDatasource.releaseMutex();
            }
        } else {
            sqlDatasourceToBeReturned = hikariDatasourceMap.computeIfAbsent(poolKey,
                    key -> createAndInitDatasource(sqlDatasourceParams));

        }
        return sqlDatasourceToBeReturned;
    }

    private static SQLDatasource createAndInitDatasource(SQLDatasource.SQLDatasourceParams sqlDatasourceParams) {
        SQLDatasource newSqlDatasource = new SQLDatasource(sqlDatasourceParams);
        newSqlDatasource.incrementClientCounter();
        return newSqlDatasource;
    }

    /**
     * Get the peer address of this datasource. If URL is used, the peer address is the URL. Otherwise, the peer address
     * is "host:port"
     *
     * @return The peer address for this datasource.
     */
    public String getPeerAddress() {
        return peerAddress;
    }

    /**
     * Get the database product name.
     *
     * @return The database product name.
     */
    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public Connection getSQLConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    public boolean isXAConnection() {
        return this.xaConn;
    }

    public XADataSource getXADataSource() throws DatabaseException {
        XADataSource xaDataSource;
        try {
            xaDataSource = hikariDataSource.unwrap(XADataSource.class);
        } catch (SQLException e) {
            throw new DatabaseException("Error while obtaining distributed data source", e);
        }
        return xaDataSource;
    }

    private void closeConnectionPool() {
        hikariDataSource.close();
        poolShutdown = true;
    }

    public boolean isGlobalDatasource() {
        return globalDatasource;
    }

    public boolean isPoolShutdown() {
        return poolShutdown;
    }

    public void incrementClientCounter() {
        clientCounter.incrementAndGet();
    }

    public void decrementClientCounterAndAttemptPoolShutdown() {
        acquireMutex();
        if (!poolShutdown) {
            if (clientCounter.decrementAndGet() == 0) {
                closeConnectionPool();
            }
        }
        releaseMutex();
    }

    public void releaseMutex() {
        mutex.unlock();
    }

    public void acquireMutex() {
        mutex.lock();
    }

    /**
     * Clarification on behavior with parameters.
     * 1. Adding an invalid param in the JDBC URL. This will result in an error getting returned
     * eg: jdbc:Client testDB = new({
     * url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT;INVALID_PARAM=-1",
     * username: "SA",
     * password: ""
     * });
     * 2. Providing an invalid param with dataSourceClassName provided. This will result in an error
     * returned because when hikaricp tries to call setINVALID_PARAM method on the given datasource
     * class name, it will fail since there is no such method
     * eg: jdbc:Client testDB = new({
     * url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
     * username: "SA",
     * password: "",
     * poolOptions: { dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" },
     * dbOptions: { "INVALID_PARAM": -1 }
     * });
     * 3. Providing an invalid param WITHOUT dataSourceClassName provided. This may not return any error.
     * Because this will result in the INVALID_PARAM being passed to Driver.Connect which may not recognize
     * it as an invalid parameter.
     * eg: jdbc:Client testDB = new({
     * url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
     * username: "SA",
     * password: "",
     * dbOptions: { "INVALID_PARAM": -1 }
     * });
     *
     * @param sqlDatasourceParams This includes the configuration for the datasource to be built.
     */
    private void buildDataSource(SQLDatasourceParams sqlDatasourceParams) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(sqlDatasourceParams.jdbcUrl);
            config.setUsername(sqlDatasourceParams.username);
            config.setPassword(sqlDatasourceParams.password);
            config.setDataSourceClassName(sqlDatasourceParams.driver);
            if (sqlDatasourceParams.connectionPool != null) {
                int maxOpenConn = sqlDatasourceParams.connectionPool.
                        getIntValue(Constants.Options.MAX_OPEN_CONNECTIONS).intValue();
                if (maxOpenConn < 0) {
                    config.setMaximumPoolSize(maxOpenConn);
                }

                Object connLifeTimeSec = sqlDatasourceParams.connectionPool
                        .get(Constants.Options.MAX_CONNECTION_LIFE_TIME_SECONDS);
                if (connLifeTimeSec instanceof DecimalValue) {
                    DecimalValue connLifeTime = (DecimalValue) connLifeTimeSec;
                    if (connLifeTime.floatValue() > 0) {
                        long connLifeTimeMS = Double.valueOf(connLifeTime.floatValue() * 1000).longValue();
                        config.setMaxLifetime(connLifeTimeMS);
                    }
                }

                int minIdleConnections = sqlDatasourceParams.connectionPool
                        .getIntValue(Constants.Options.MIN_IDLE_CONNECTIONS).intValue();
                if (minIdleConnections < 0) {
                    config.setMinimumIdle(minIdleConnections);
                }
            }
            if (sqlDatasourceParams.options != null) {
                sqlDatasourceParams.options.entrySet().forEach(entry -> {
                    if (SQLDatasourceUtils.isSupportedDbOptionType(entry.getValue())) {
                        config.addDataSourceProperty(entry.getKey(), entry.getValue());
                    } else {
                        throw ErrorGenerator.getSQLApplicationError("unsupported type " + entry.getKey()
                                + " for the db option");
                    }
                });
            }
            hikariDataSource = new HikariDataSource(config);
            Runtime.getRuntime().addShutdownHook(new Thread(this::closeConnectionPool));
        } catch (Throwable t) {
            String message = "error in sql connector configuration: " + t.getMessage();
            if (t.getCause() != null) {
                message += ":" + t.getCause().getMessage();
            }
            throw ErrorGenerator.getSQLApplicationError(message);
        }
    }

    private boolean isXADataSource() throws DatabaseException {
        try {
            return hikariDataSource.isWrapperFor(XADataSource.class);
        } catch (SQLException e) {
            throw new DatabaseException("error while checking distributed data source: ", e);
        }
    }

    /**
     * This class encapsulates the parameters required for the initialization of {@code SQLDatasource} class.
     */
    public static class SQLDatasourceParams {
        private MapValue<String, Object> connectionPool;
        private String jdbcUrl;
        private String username;
        private String password;
        private String driver;
        private boolean isGlobalDatasource;
        private MapValue<String, Object> options;

        public SQLDatasourceParams() {
        }

        public SQLDatasourceParams setConnectionPool(MapValue<String, Object> connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        public SQLDatasourceParams setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public SQLDatasourceParams setUsername(String username) {
            this.username = username;
            return this;
        }

        public SQLDatasourceParams setPassword(String password) {
            this.password = password;
            return this;
        }

        public SQLDatasourceParams setGlobalDatasource(boolean globalDatasource) {
            isGlobalDatasource = globalDatasource;
            return this;
        }

        public SQLDatasourceParams setDriver(String driver) {
            this.driver = driver;
            return this;
        }

        public SQLDatasourceParams setOptions(MapValue<String, Object> options) {
            this.options = options;
            return this;
        }
    }
}
