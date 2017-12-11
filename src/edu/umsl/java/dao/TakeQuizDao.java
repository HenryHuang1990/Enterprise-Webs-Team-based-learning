package edu.umsl.java.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import edu.umsl.java.beans.QuizBean;
import edu.umsl.java.util.ReadProperties;

public class TakeQuizDao {

private Connection connection;
	
	public TakeQuizDao() {
		ReadProperties.loadPropertiesFile();
		try {
			Class.forName(ReadProperties.getDbDriver());
			connection = DriverManager.getConnection(ReadProperties.getDbUrl(), ReadProperties.getDbUser(),
					ReadProperties.getDbPswd());
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public JsonArrayBuilder getActiveQuizList(String studentid, String timestamp) {
		JsonArrayBuilder jsonArry = Json.createArrayBuilder();
		String query = 	"SELECT q.id, q.courseid, q.number, q.time_limit, s.token, s.groupid, s.relnid"
		+ " FROM quiz q INNER JOIN sgroup_quiz s" 
		+ " ON q.id=s.quizid AND s.isgroupquiz=0 AND q.deleted=0 AND (s.studentid='"+studentid+"'" 
		+ " AND q.start_time<='"+timestamp+"' AND q.end_time>='"+timestamp+"');"; 
		try {
			PreparedStatement stmt = connection.prepareStatement(query);
			ResultSet res = stmt.executeQuery();
			while(res.next()) {
				jsonArry.add(Json.createObjectBuilder()
						.add("quizid", res.getInt("id"))
						.add("courseid", res.getInt("courseid"))
						.add("quiznumber", res.getInt("number"))
						.add("time_limit", res.getInt("time_limit"))
						.add("token", res.getString("token"))
						.add("groupid", res.getInt("groupid"))
						.add("relnid", res.getInt("relnid"))
						);
			}
			res.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return jsonArry;
	}
	
	public String getGroupNumber(int groupid) {
		String groupNumber="";
		String query = "SELECT groupnumber FROM sgroup WHERE groupid="+groupid+";";
		try {
			ResultSet res = connection.prepareStatement(query).executeQuery();
			if(res.next()) {
				groupNumber = res.getString("groupnumber");
			}	
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return groupNumber;
	}
	
	public JsonArrayBuilder getActiveGroupQuizList(String studentid, String timestamp) {
		JsonArrayBuilder jsonArry = Json.createArrayBuilder();
		try {
			String query = 	"SELECT q.id, q.courseid, q.number, q.time_limit, s.token, s.groupid, s.relnid"
					+ " FROM quiz q INNER JOIN sgroup_quiz s" 
					+ " ON q.id=s.quizid AND s.isgroupquiz=1 AND q.deleted=0 AND (s.studentid='"+studentid+"'" 
					+ " AND q.start_time<='"+timestamp+"' AND q.end_time>='"+timestamp+"');"; 
						
			String token = "UNAVBL";
			PreparedStatement stmt = connection.prepareStatement(query);
			ResultSet res = stmt.executeQuery();
			while(res.next()) {
				String groupnumber = getGroupNumber(res.getInt("groupid"));
				
				String stdNotCmpltdIds = getStudentsNotCompletedQuizInGroup (res.getInt("groupid"), res.getInt("id"));
				if(stdNotCmpltdIds.length()>1) {
					token = "UNAVBL";
				}
				else {
					token =  res.getString("token");
				}
				jsonArry.add(Json.createObjectBuilder()
						.add("quizid", res.getInt("id"))
						.add("courseid", res.getInt("courseid"))
						.add("quiznumber", res.getInt("number"))
						.add("time_limit", res.getInt("time_limit"))
						.add("groupid", res.getInt("groupid"))
						.add("relnid", res.getInt("relnid"))
						.add("token", token)
						.add("stdNotCmpltdIds", stdNotCmpltdIds)
						.add("groupnumber", groupnumber)
						);
			}
			res.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return jsonArry;
	}
	
	public String getStudentsNotCompletedQuizInGroup (int groupid, int quizid) {
		String result = "";
		String query = "SELECT studentid FROM sgroup_quiz WHERE groupid="+groupid+" AND quizid="+quizid+" AND NOT(token='CMPLTD')";
		try {
			ResultSet res = connection.prepareStatement(query).executeQuery();
			while(res.next()) {
				result += res.getString("studentid") +",";
			}	
		}catch(SQLException e) {
			e.printStackTrace();
		}
		if (result.length()>1) {
			result = result.substring(0, result.lastIndexOf(","));
		}
		return result;
	}
	public JsonObjectBuilder getQuizWithId(int id, int questionNumber, int relnid) {
		JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		QuizBean quiz = new QuizBean();
				
		try {
			String query = "SELECT id, courseid, number, time_limit "
					+ "FROM quiz "
					+ "WHERE deleted=0 AND id='"+id+"'";
			
			ResultSet res = connection.prepareStatement(query).executeQuery();
			res.next();
			
			quiz.setQuizid(res.getInt("id"));
			quiz.setQuiznumber(res.getInt("number"));
			quiz.setTime_limit(res.getInt("time_limit"));
			quiz.setCurrQuest(questionNumber);
			
			query = "SELECT COUNT(*) totalCount FROM quest_quiz WHERE quizid="+id+";";
			res = connection.prepareStatement(query).executeQuery();
			res.next();
			quiz.setTotalQuest(res.getInt("totalCount"));
			
			query = "SELECT * FROM ( " + 
					"SELECT @row_number:=@row_number+1 AS row_number, id, content, opt_a, opt_b, opt_c, opt_d FROM question, " + 
					"(SELECT @row_number:=0) AS t WHERE id in (SELECT relnid FROM quest_quiz qq WHERE quizid="+id+" AND deleted=0) " + 
					"ORDER BY id ) r WHERE r.row_number="+questionNumber+";";
			res = connection.prepareStatement(query).executeQuery();
			res.next();
			quiz.setQuestionid(res.getInt("id"));
			quiz.setQuestion(res.getString("content"));
			
			JsonArrayBuilder j_opts = Json.createArrayBuilder()
					.add(res.getString("opt_a"))
					.add(res.getString("opt_b"))
					.add(res.getString("opt_c"))
					.add(res.getString("opt_d"));
			
			//check if already answered
			query = "SELECT answer FROM answer_quiz "
					+ "WHERE sgroup_quiz_id="+relnid+" AND questid="+quiz.getQuestionid()+";";
			res = connection.prepareStatement(query).executeQuery();
			if(res.next()) {
				quiz.setAnswer(res.getInt("answer"));
			}
			else {
				quiz.setAnswer(-1);
			}
			
			jsonObject.add("quizid", quiz.getQuizid())
				.add("quiznumber", quiz.getQuiznumber())
				.add("time_limit", quiz.getTime_limit())
				.add("currQuest", quiz.getCurrQuest())
				.add("totalQuest", quiz.getTotalQuest())
				.add("questid", quiz.getQuestionid())				
				.add("quest", quiz.getQuestion())
				.add("answer", quiz.getAnswer())
				.add("options", j_opts);
			
			res.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public boolean checkTokenValidation(String studentid, int groupid, int quizid, String token) {
		String query="SELECT token FROM sgroup_quiz "
				+ "WHERE studentid='"+studentid+"' AND groupid='"+groupid+"' AND quizid='"+quizid+"';";
		try {
			ResultSet rs = connection.prepareStatement(query).executeQuery();
			if(rs.next() && rs.getString("token").equals(token)) {
				return true;
			}
			rs.close();
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}

	public void submitAnswer(int relnid, int questid, int selectedOption) {
		
		int score=0;
		String query = "SELECT COUNT(*) FROM question q WHERE q.id='"+questid+"' AND q.answer='"+selectedOption+"';";
		try {
			ResultSet rs = connection.prepareStatement(query).executeQuery();
			rs.next();
			if(rs.getInt(1)!=0) {
				score=1;
			}

			query= "DELETE FROM answer_quiz WHERE sgroup_quiz_id="+relnid+" AND questid="+questid+";";
			connection.prepareStatement(query).executeUpdate();
			
			query= "INSERT INTO answer_quiz(sgroup_quiz_id, questid, answer, score) VALUES ("
					+ "'"+relnid+"', '"+questid+"', '"+selectedOption+"',  '"+score+"');";
			connection.prepareStatement(query).executeUpdate();
			
			rs.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void finishQuiz(String studentid, int groupid, int quizid) {
		String query = "UPDATE sgroup_quiz SET token='CMPLTD' "
				+ "WHERE studentid='"+studentid+"' AND groupid='"+groupid+"' AND quizid='"+quizid+"';";
		try {
			connection.prepareStatement(query).executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void clearAnswersForThisQuiz(int relnid) {
		String query = "DELETE FROM answer_quiz WHERE sgroup_quiz_id="+relnid+";";
		try {
			connection.prepareStatement(query).executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isGroupLeader(String studentid) {
		String query = "SELECT * FROM sgroup WHERE groupleader='"+studentid+"'";
		try {
			ResultSet rs = connection.prepareStatement(query).executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
