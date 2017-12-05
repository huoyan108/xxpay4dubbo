package org.xxpay.dubbo.dao.listener;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.xxpay.common.util.JdbcBean;
import org.xxpay.common.util.ResultBean;
import org.xxpay.common.util.StringUtil;


@Repository
public class DataDao {
	public static Log log = LogFactory.getLog(DataDao.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public ResultBean getJdbcData(final JdbcBean jdbcBean) {
		long starttime = System.currentTimeMillis();
		final ResultBean resultBean = new ResultBean();
		jdbcTemplate.execute(new CallableStatementCreator() {
			public CallableStatement createCallableStatement(
					final Connection con) throws SQLException {
				CallableStatement cs = con.prepareCall(getProc(jdbcBean));
				return getParam(jdbcBean, cs);
			}
		}, new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs)
					throws SQLException, DataAccessException {
				cs.execute();
				getOutPut(resultBean, jdbcBean, cs);
				return cs;
			}
		});
		long endtime = System.currentTimeMillis();
		log.info("存储过程:" + jdbcBean.getProc() + " 执行时间:"
				+ (endtime - starttime));
		return resultBean;
	}

	/**
	 * 动态获取存储过程名称和参数
	 * 
	 * @param map
	 * @return
	 */
	private String getProc(JdbcBean jdbcBean) {
		int length = jdbcBean.getParam().size();
		final StringBuffer proc = new StringBuffer();
		proc.append("{call ");
		proc.append(jdbcBean.getProc() + " ( ");
		for (int i = 0; i < length; i++) {
			proc.append("?");
			if (i != length - 1) {
				proc.append(",");
			}
		}
		proc.append(")}");
		return proc.toString();
	}

	/**
	 * 输入参数注册
	 * 
	 * @param map
	 * @param cs
	 * @return
	 * @throws SQLException
	 */
	private CallableStatement getParam(JdbcBean jdbcBean, CallableStatement cs)
			throws SQLException {
		Map<String, Object> paramMap = jdbcBean.getParam();
		int i = 1;
		for (Map.Entry<String, Object> m : paramMap.entrySet()) {
			if (m.getKey().toLowerCase().startsWith("output")) {
				// 输出参数
				getCs(i, StringUtil.getNotNullStr(m.getValue()), cs, true);
			} else {
				// 输入参数
				getCs(i, StringUtil.getNotNullStr(m.getValue()), cs, false);
			}
			i++;
		}
		return cs;
	}

	private void getCs(int i, Object param, CallableStatement cs, boolean output)
			throws SQLException {
		if (param instanceof Integer) {
			int value = ((Integer) param).intValue();
			if (output) {
				cs.registerOutParameter(i, java.sql.Types.INTEGER);
			} else {
				cs.setInt(i, value);
			}
		} else if (param instanceof String) {
			String s = (String) param;
			if (output) {
				cs.registerOutParameter(i, java.sql.Types.VARCHAR);
			} else {
				cs.setString(i, s);
			}
		} else if (param instanceof Double) {
			double d = ((Double) param).doubleValue();
			if (output) {
				cs.registerOutParameter(i, java.sql.Types.DOUBLE);
			} else {
				cs.setDouble(i, d);
			}
		} else if (param instanceof Float) {
			float f = ((Float) param).floatValue();
			if (output) {
				cs.registerOutParameter(i, java.sql.Types.FLOAT);
			} else {
				cs.setDouble(i, f);
			}
		} else if (param instanceof Long) {
			long l = ((Long) param).longValue();
			if (output) {
				cs.registerOutParameter(i, java.sql.Types.LONGVARCHAR);
			} else {
				cs.setLong(i, l);
			}
		} else if (param instanceof Boolean) {
			boolean b = ((Boolean) param).booleanValue();
			if (output) {
				cs.registerOutParameter(i, java.sql.Types.BOOLEAN);
			} else {
				cs.setBoolean(i, b);
			}
		} else if (param instanceof Date) {
			Date d = (Date) param;
			if (output) {
				cs.registerOutParameter(i, java.sql.Types.DATE);
			} else {
				cs.setDate(i, d);
			}
		} else if (param instanceof BigInteger) {
			Integer d = Integer.parseInt(String.valueOf(param));
			if (output) {
				cs.registerOutParameter(i, java.sql.Types.INTEGER);
			} else {
				cs.setInt(i, d);
			}
		}
	}

	/**
	 * 获取返回值（返回多结果集）
	 * 
	 * @param param
	 * @param cs
	 * @throws SQLException
	 */
	private ResultBean getOutPut(ResultBean resultBean, JdbcBean jdbcBean,
			CallableStatement cs) throws SQLException {
		Map<String, Object> paramMap = jdbcBean.getParam();
		Map resultMap = new HashMap();
		List list = new ArrayList();
		int k = 0;
		ResultSet resultSet = null;
		if (cs != null) {
			resultSet = cs.getResultSet();
			if (null != resultSet) {
				list = getResultSet(resultSet);
				resultMap.put(k, list);

				while (cs.getMoreResults()) {
					k++;
					resultSet = cs.getResultSet();
					resultMap.put(k, getResultSet(resultSet));

				}
			}
		}
		cs.getUpdateCount();
		if (!cs.getMoreResults()) {
			int n = 1;
			for (Map.Entry<String, Object> m : paramMap.entrySet()) {
				if (m.getKey().toLowerCase().startsWith("output")) {
					paramMap.put(m.getKey(),
							StringUtil.getNotNullStr(cs.getObject(n)));
				}
				n++;
			}
		}
		if (null != resultSet) {
			resultSet.close();
		}
		resultBean.setOutput(paramMap);
		resultBean.setMorList(resultMap);
		resultBean.setList(list);
		return resultBean;
	}

	/**
	 * 方法功能说明：将取出的结果集ResultSet对象组装成 List<--Map<--(columnName:columnValue),
	 * 每一个map对应一条记录，map长度 == column数量 修改：日期 by 修改者 修改内容：
	 * 
	 * @参数： @param rs
	 * @参数： @return
	 * @return Map
	 * @throws
	 */
	private List getResultSet(ResultSet rs) throws SQLException {
		List list = new ArrayList();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			// 每循环一次遍历出来1条记录，记录对应的所有列值存放在map中(columnName:columnValue)
			while (rs.next()) {
				Map map = new HashMap();
				int columnCount = rsmd.getColumnCount();
				for (int i = 0; i < columnCount; i++) {
					String columnName = rsmd.getColumnName(i + 1);
					map.put(columnName,
							StringUtil.getNotNullStr(rs.getString(i + 1)));
				}
				list.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private List getMoreResultSet(ResultSet rs) throws SQLException {
		List list = new ArrayList();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			// 每循环一次遍历出来1条记录，记录对应的所有列值存放在map中(columnName:columnValue)
			Map map = new HashMap();
			int columnCount = rsmd.getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				String columnName = rsmd.getColumnName(i + 1);
				map.put(columnName,
						StringUtil.getNotNullStr(rs.getString(i + 1)));
			}
			list.add(map);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public void update(String sql, Object args) {
		jdbcTemplate.update(sql, args);
	}

	/**
	 * 获取结果集
	 * 
	 * @param sql
	 * @param param
	 * @return
	 */
	public List getList(String sql, final String param[]) {
		final List list = new ArrayList();
		jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet result) throws SQLException {
				Map resultMap = new HashMap();
				for (int i = 0; i < param.length; i++) {
					String key = param[i];
					resultMap.put(key, result.getString(key));
				}
				list.add(resultMap);
			}
		});
		return list;
	}
}
