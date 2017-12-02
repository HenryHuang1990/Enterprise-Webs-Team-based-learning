package edu.umsl.java.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.UnavailableException;

import edu.umsl.java.beans.UserBean;
import edu.umsl.java.beans.PageBean;

public class UserDao {

	private Connection connection;

	private PreparedStatement getRecords;
	private PreparedStatement getUserRecords;
	private PreparedStatement addRecord;
	private PreparedStatement deleteRecord;
	private PreparedStatement updateRecord;
	private PreparedStatement getRecords3;
	private PreparedStatement getRecords_lname;
	private PreparedStatement getRecords_fname;
	private PreparedStatement getCount;

	public UserDao() throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");
			getCount = connection.prepareStatement("select count(*) from user where active =1 and role= ? or role= ?");

			// --------------
			getRecords = connection.prepareStatement(
					"Select id, ssoid, fname, lname, active from user where active = 1 and role = ? or role= ? order by id ASC  limit ?, ?");
			getRecords_lname = connection.prepareStatement(
					"Select id, ssoid, fname, lname, active from user where active = 1 and role = ? or role= ? order by lname, fname ASC limit ?, ?");
			getRecords_fname = connection.prepareStatement(
					"Select id, ssoid, fname, lname, active from user where active = 1 and role = ? or role= ? order by fname, lname ASC limit ? ,?");

			// --------------

			getUserRecords = connection.prepareStatement(
					"Select id, ssoid, fname, lname, active, role, email, dept from user where id = ?");
			getRecords3 = connection.prepareStatement(
					"select id, ssoid, fname, lname, active, role, email, dept from user where (ssoid like ? or fname like ? or lname like ?) and (role =? or role=?)");

			addRecord = connection.prepareStatement(
					"insert into user (" + "ssoid, fname, lname, email, role, dept)" + "values (?,?,?,?,?,?)");

			deleteRecord = connection.prepareStatement("update user set active = 0 where id = ?");
			updateRecord = connection.prepareStatement(
					"update user	set ssoid= ?, fname=?, lname=? , role=? , email=?, dept=? , active=? where id = ?");

		} catch (Exception exception) {
			exception.printStackTrace();
			throw new UnavailableException(exception.getMessage());
		}

	}

	public void updateUser(String ssoid, String fname, String lname, String id, String role, String email, String dept,
			String active) throws SQLException {

		try {
			updateRecord.setString(1, ssoid);
			updateRecord.setString(2, fname);
			updateRecord.setString(3, lname);
			updateRecord.setInt(4, Integer.parseInt(role));
			updateRecord.setString(5, email);
			updateRecord.setString(6, dept);
			updateRecord.setInt(7, Integer.parseInt(active));
			updateRecord.setInt(8, Integer.parseInt(id));

			updateRecord.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void deleteUser(String id) throws SQLException {

		try {
			deleteRecord.setString(1, id);
			deleteRecord.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public PageBean getCount(int role1, int role2) throws SQLException {

		PageBean pagebean = new PageBean();
		if (role1 == role2)
			role2 = 9;

		try {
			getCount.setInt(1, role1);
			getCount.setInt(2, role2);
			ResultSet rs = getCount.executeQuery();
			rs.next();
			pagebean.setTotalRecords(rs.getInt(1));

			double temp = Math.ceil((double) pagebean.getTotalRecords() / pagebean.getRecordsPerPage());
			pagebean.setTotalPages((int) temp);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pagebean;
	}

	public List<UserBean> getUserSearchList(String ssoid, String fname, String lname, int role) throws SQLException {

		int role_a = 0;
		int role_b = 1;

		if (role == 1) {
			role_a = 2;
			role_b = 9;
		}

		List<UserBean> userList = new ArrayList<UserBean>();
		getRecords3.setString(1, ssoid);
		getRecords3.setString(2, fname);
		getRecords3.setString(3, lname);
		getRecords3.setInt(4, role_a);
		getRecords3.setInt(5, role_b);

		ResultSet rs = getRecords3.executeQuery();
		while (rs.next()) {
			UserBean user = new UserBean();
			user.setId(rs.getString(1));
			user.setSsoid(rs.getString(2));
			user.setFname(rs.getString(3));
			user.setLname(rs.getString(4));
			user.setActive(Integer.parseInt(rs.getString(5)));
			user.setRole(Integer.parseInt(rs.getString(6)));
			user.setEmail(rs.getString(7));
			user.setDept(rs.getString(8));
			userList.add(user);
		}
		return userList;

	}

	public UserBean getUserSearch(String ssoid, String fname, String lname) throws SQLException {

		UserBean user = new UserBean();
		getRecords3.setString(1, ssoid);
		getRecords3.setString(2, fname);
		getRecords3.setString(3, lname);
		ResultSet rs = getRecords3.executeQuery();
		rs.next();
		int row = rs.getRow();

		if (row > 0) {
			user.setSsoid(rs.getString(2));
			user.setFname(rs.getString(3));
			user.setLname(rs.getString(4));
		} else {
			user.setSsoid("");
			user.setFname("");
			user.setLname("");
		}

		return user;
	}

	public UserBean getUser(String id) throws SQLException {

		UserBean user = new UserBean();

		getUserRecords.setInt(1, Integer.parseInt(id));
		ResultSet rs = getUserRecords.executeQuery();
		rs.next();
		user.setSsoid(rs.getString(2));
		user.setFname(rs.getString(3));
		user.setLname(rs.getString(4));
		user.setActive(Integer.parseInt(rs.getString(5)));
		user.setRole(Integer.parseInt(rs.getString(6)));
		user.setEmail(rs.getString(7));
		user.setDept(rs.getString(8));

		return user;
	}

	// --------------------------------------------------------
	public List<UserBean> getUserList(int role1, int role2, int start, int end) throws SQLException {
		List<UserBean> userList = new ArrayList<UserBean>();
		if (role1 == role2)
			role2 = 9;
		getRecords.setInt(1, role1);
		getRecords.setInt(2, role2);
		getRecords.setInt(3, start);
		getRecords.setInt(4, end);
		ResultSet results = getRecords.executeQuery();

		while (results.next()) {
			UserBean user = new UserBean();
			user.setId(results.getString(1));
			user.setSsoid(results.getString(2));
			user.setFname(results.getString(3));
			user.setLname(results.getString(4));

			userList.add(user);

		}
		return userList;
	}

	public List<UserBean> getUserListLname(int role1, int role2, int start, int end) throws SQLException {
		List<UserBean> userList = new ArrayList<UserBean>();
		getRecords_lname.setInt(1, role1);
		getRecords_lname.setInt(2, role2);
		getRecords_lname.setInt(3, start);
		getRecords_lname.setInt(4, end);
		ResultSet results = getRecords_lname.executeQuery();

		while (results.next()) {
			UserBean user = new UserBean();
			user.setId(results.getString(1));
			user.setSsoid(results.getString(2));
			user.setFname(results.getString(3));
			user.setLname(results.getString(4));

			userList.add(user);

		}
		return userList;
	}

	public List<UserBean> getUserListFname(int role1, int role2, int start, int end) throws SQLException {
		List<UserBean> userList = new ArrayList<UserBean>();
		getRecords_fname.setInt(1, role1);
		getRecords_fname.setInt(2, role2);
		getRecords_fname.setInt(3, start);
		getRecords_fname.setInt(4, end);

		ResultSet results = getRecords_fname.executeQuery();

		while (results.next()) {
			UserBean user = new UserBean();
			user.setId(results.getString(1));
			user.setSsoid(results.getString(2));
			user.setFname(results.getString(3));
			user.setLname(results.getString(4));

			userList.add(user);

		}
		return userList;
	}
	// ----------------------------------------

	public int addUserBulk(UserBean user) throws SQLException {

		int err = 0;
		try {
			addRecord.setString(1, user.getSsoid());
			addRecord.setString(2, user.getFname());
			addRecord.setString(3, user.getLname());
			addRecord.setString(4, user.getEmail());
			addRecord.setInt(5, user.getRole());
			addRecord.setString(6, user.getDept());
			addRecord.executeUpdate();

		} catch (SQLException sqlException) {
			System.out.println("Error: Duplicate Entry");
			err++;
		}

		return err;

	}

	public void addUser(UserBean user) throws SQLException {

		
			addRecord.setString(1, user.getSsoid());
			addRecord.setString(2, user.getFname());
			addRecord.setString(3, user.getLname());
			addRecord.setString(4, user.getEmail());
			addRecord.setInt(5, user.getRole());
			addRecord.setString(6, user.getDept());
			addRecord.executeUpdate();
	}

	protected void finalize() {
		try {
			getCount.close();
			getRecords.close();
			getRecords_lname.close();
			getRecords_fname.close();
			getUserRecords.close();
			getRecords3.close();
			addRecord.close();
			deleteRecord.close();
			updateRecord.close();
			connection.close();
		} catch (SQLException sqlException) {
			System.out.println("Error: Duplicate Entry");
			// sqlException.printStackTrace();

		}
	}

}
