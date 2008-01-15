package org.xonix.zlo.search.db;

import org.apache.log4j.Logger;
import org.xonix.zlo.search.dao.Site;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;

/**
 * Author: Vovan
 * Date: 18.11.2007
 * Time: 0:23:01
 */
public final class DbUtils {
    private static final Logger logger = Logger.getLogger(DbUtils.class);

    public static void setParams(PreparedStatement st, Object[] params, VarType[] types) throws DbException {
        if (params.length != types.length)
            throw new IllegalArgumentException("Number of params and types does not match");

        try {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                VarType type = types[i];
                int j = i+1;

                if (param == null) {
                    st.setNull(j, type.getSqlType());
                } else {
                    switch (type) {
                        case STRING:
                            st.setString(j, (String) param);
                            break;

                        case INTEGER:
                            st.setInt(j, (Integer) param);
                            break;

                        case BOOLEAN:
                            st.setBoolean(j, (Boolean) param);
                            break;

                        case DATE:
                            st.setTimestamp(j, new Timestamp(((java.util.Date) param).getTime()));
                            break;

                        default:
                            throw new IllegalArgumentException(
                                    String.format("Unsupported parameter type: %s of parameter: %s",
                                            param.getClass(), param));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    //======================================
    private static DbResult executeSelect(Connection connection, String sqlString, Object[] params, VarType[] types) throws DbException {
        PreparedStatement st;
        try {
            st = connection.prepareStatement(sqlString);
            setParams(st, params, types);
            return new DbResult(connection, st.executeQuery(), st);
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    private static DbResult executeSelect(String jndiDsName, String sqlString, Object[] params, VarType[] types) throws DbException {
        try {
            return executeSelect(ConnectionUtils.getConnection(jndiDsName), sqlString, params, types);
        } catch (NamingException e) {
            throw new DbException(e);
        }
    }

    private static DbResult executeSelect(DataSource ds, String sqlString, Object[] params, VarType[] types) throws DbException {
        return executeSelect(ConnectionUtils.getConnection(ds), sqlString, params, types);
    }

    public static DbResult executeSelect(Site site, String sqlString, Object[] params, VarType[] types) throws DbException {
        DbResult dbResult;
        if (site.DB_VIA_CONTAINER) {
            dbResult = executeSelect(site.JNDI_DS_NAME, sqlString, params, types);
        } else {
            dbResult = executeSelect(site.getDataSource(), sqlString, params, types);
        }
        dbResult.setSite(site);
        return dbResult;
    }

    //--------------------------------------
/*    private static DbResult executeSelect(String jndiDsName, String sqlString) throws DbException {
        return executeSelect(jndiDsName, sqlString, new Object[0], new VarType[0]);
    }

    private static DbResult executeSelect(DataSource ds, String sqlString) throws DbException {
        return executeSelect(ds, sqlString, new Object[0], new VarType[0]);
    }*/

    public static DbResult executeSelect(Site site, String sqlString) throws DbException {
        DbResult dbResult;
        if (site.DB_VIA_CONTAINER) {
            dbResult = executeSelect(site.JNDI_DS_NAME, sqlString, new Object[0], new VarType[0]);
        } else {
            dbResult = executeSelect(site.getDataSource(), sqlString, new Object[0], new VarType[0]);
        }
        dbResult.setSite(site);
        return dbResult;
    }
    /*
     Executes insert, update, delete
     */
    //======================================
    public static void executeUpdate(Connection connection, String sqlString, Object[] params, VarType[] types, Integer expectedResult) throws DbException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement(sqlString);

            setParams(st, params, types);

            int res = st.executeUpdate();
            if (expectedResult != null && res != expectedResult)
                throw new SQLException(String.format("Expected result: %s, actual result: %s", expectedResult, res));
        } catch (SQLException e) {
           throw new DbException(e);
        } finally {
            CloseUtils.close(st, rs);
        }
    }

    public static void executeUpdate(String jndiDsName, String sqlString, Object[] params, VarType[] types, Integer expectedResult) throws DbException {
        try {
            executeUpdate(ConnectionUtils.getConnection(jndiDsName), sqlString, params, types, expectedResult);
        } catch (NamingException e) {
            throw new DbException(e);
        }
    }

    public static void executeUpdate(DataSource ds, String sqlString, Object[] params, VarType[] types, Integer expectedResult) throws DbException {
        executeUpdate(ConnectionUtils.getConnection(ds), sqlString, params, types, expectedResult);
    }

    public static void executeUpdate(Site site, String sqlString, Object[] params, VarType[] types, Integer expectedResult) throws DbException {
        if (site.DB_VIA_CONTAINER) {
            executeUpdate(site.JNDI_DS_NAME, sqlString, params, types, expectedResult);
        } else {
            executeUpdate(site.getDataSource(), sqlString, params, types, expectedResult);
        }
    }
    //--------------------------------------
    public static void executeUpdate(String jndiDsName, String sqlString, Object[] params, VarType[] types) throws DbException {
        executeUpdate(jndiDsName, sqlString, params, types, null);
    }

    public static void executeUpdate(DataSource ds, String sqlString, Object[] params, VarType[] types) throws DbException {
        executeUpdate(ds, sqlString, params, types, null);
    }

    public static void executeUpdate(Site site, String sqlString, Object[] params, VarType[] types) throws DbException {
        if (site.DB_VIA_CONTAINER) {
            executeUpdate(site.JNDI_DS_NAME, sqlString, params, types, null);
        } else {
            executeUpdate(site.getDataSource(), sqlString, params, types, null);
        }
    }
}
