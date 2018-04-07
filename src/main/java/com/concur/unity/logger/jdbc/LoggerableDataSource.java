package com.concur.unity.logger.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * 包含日志的动态数据源
 *
 * @author yongfu.cyf
 * @create 2017-09-16 下午9:06
 **/
public class LoggerableDataSource implements DataSource {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private DataSource targetDataSource;

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = targetDataSource.getConnection();
        if (logger.isDebugEnabled()) {
            return ConnectionLogger.newInstance(connection, logger, 0);
        } else {
            return connection;
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = targetDataSource.getConnection(username, password);
        if (logger.isDebugEnabled()) {
            return ConnectionLogger.newInstance(connection, logger, 0);
        } else {
            return connection;
        }
    }

    public LoggerableDataSource() {
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLoginTimeout(int timeout) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter");
    }

    @Override
    public void setLogWriter(PrintWriter pw) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        } else {
            throw new SQLException("DataSource of type [" + this.getClass().getName() + "] cannot be unwrapped as [" + iface.getName() + "]");
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }


    public void setTargetDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }
}
