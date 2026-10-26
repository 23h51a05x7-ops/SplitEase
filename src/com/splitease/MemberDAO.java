
package com.splitease;

import com.splitease.model.Member;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MemberDAO {

    public void saveMember(Member member) throws SQLException {
        String sql = "INSERT INTO members (name) VALUES (?)";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, member.getName());
        ps.executeUpdate();

        ps.close();
        con.close();
    }
}
