package io.g740.components.dlock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;

/**
 * @author bbottong
 */
@Repository
public class DlockRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DlockDo selectForUpdate(String bizType, Connection connection) {
        String sql = "select create_timestamp, last_update_timestamp, timeout_in_minutes" +
                " from to_dlock where biz_type = ?" +
                " for update";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, bizType);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                java.util.Date createTimestamp = resultSet.getDate("create_timestamp");
                java.util.Date lastUpdateTimestamp = resultSet.getDate("last_update_timestamp");
                Integer timeoutInMinutes = resultSet.getInt("timeout_in_minutes");

                DlockDo dlockDo = new DlockDo();
                dlockDo.setCreateTimestamp(createTimestamp);
                dlockDo.setLastUpdateTimestamp(lastUpdateTimestamp);
                dlockDo.setBizType(bizType);
                dlockDo.setTimeoutInMinutes(timeoutInMinutes);

                return dlockDo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (preparedStatement != null){
                try {
                    preparedStatement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            // 不要close connection
        }

        return null;
    }

    public int update(String bizType, Connection connection) {
        String sql = "update to_dlock set" +
                " last_update_timestamp=now()" +
                " where biz_type=?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, bizType);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (preparedStatement != null){
                try {
                    preparedStatement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            // 不要close connection
        }

        return 0;
    }

    public boolean create(DlockDo dlockDo, Connection connection) {
        String sql = "insert into to_dlock(create_timestamp, last_update_timestamp, biz_type, timeout_in_minutes) " +
                "values(?, ?, ?, ?)";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, dlockDo.getCreateTimestamp());
            preparedStatement.setObject(2, dlockDo.getLastUpdateTimestamp());
            preparedStatement.setObject(3, dlockDo.getBizType());
            preparedStatement.setObject(4, dlockDo.getTimeoutInMinutes());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (preparedStatement != null){
                try {
                    preparedStatement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            // 不要close connection
        }

        return false;
    }
}
