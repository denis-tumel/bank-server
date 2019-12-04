package dao;

import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class UserDAO extends EntityDAO<User, Integer> {

    private static final String SQL_UPDATE_USER_BY_ID =
            "UPDATE users SET roleID = ?, login = ?, password = ?, name = ?, account = ? WHERE id = ?";

    private static final String SQL_GET_USER_BY_ID =
            "SELECT users.id, users.name, users.login, users.password, role.name, users.account FROM users JOIN role ON users.roleID = role.id WHERE users.id = ?";

    private static final String SQL_CREATE_USER =
            "INSERT INTO users (roleID, login, password, name, account) VALUES (?,?,?,?,?)";

    private static final String SQL_DELETE_USER_BY_ID =
            "DELETE FROM users WHERE id = ?";

    private static final String SQL_DELETE_CREDIT =
            "DELETE FROM credit_user WHERE user_id = ?";

    private static final String SQL_GET_ALL =
            "SELECT users.id, users.name, users.login, users.password, role.name, users.account FROM users JOIN role ON users.roleID = role.id;";

    private static final String SQL_BLOCK =
            "UPDATE users SET roleID = ? WHERE id = ?";

    private static final String SQL_GET_USER_ADMIN =
            "SELECT * FROM users WHERE roleID = 1";

    public User getAdmin(){
        PreparedStatement ps = getPrepareStatement(SQL_GET_USER_ADMIN);
        User user = null;
        try {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                user = new User();
                user.setId(rs.getInt(1));
                user.setName(rs.getString(2));
                user.setLogin(rs.getString(3));
                user.setPassword(rs.getString(4));
                user.setRole("admin");
                user.setAccount(rs.getInt(6));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePrepareStatement(ps);
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> list = new LinkedList<>();
        PreparedStatement ps = getPrepareStatement(SQL_GET_ALL);
        try {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt(1));
                user.setName(rs.getString(2));
                user.setLogin(rs.getString(3));
                user.setPassword(rs.getString(4));
                user.setRole(rs.getString("role.name"));
                user.setAccount(rs.getInt(6));

                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePrepareStatement(ps);
        }

        return list;
    }

    @Override
    public User getEntityById(Integer id) {
        User user = new User();

        PreparedStatement ps = getPrepareStatement(SQL_GET_USER_BY_ID);

        try {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                user = new User();
                user.setId(rs.getInt(1));
                user.setName(rs.getString(2));
                user.setLogin(rs.getString(3));
                user.setPassword(rs.getString(4));
                user.setRole(rs.getString("role.name"));
                user.setAccount(rs.getInt(6));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePrepareStatement(ps);
        }

        return user;
    }

    @Override
    public boolean delete(Integer id) {
        PreparedStatement st = getPrepareStatement(SQL_DELETE_USER_BY_ID);

        boolean isRemoved = false;

        try {
            st.setInt(1, id);

            deleteCredit(id);

            int i = st.executeUpdate();
            isRemoved = i > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isRemoved;
    }

    @Override
    public boolean update(User user) {
        PreparedStatement ps = getPrepareStatement(SQL_UPDATE_USER_BY_ID);
        try {
            choseRole(user, ps);
            ps.setInt(6, user.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void choseRole(User user, PreparedStatement ps) throws SQLException {
        int role = 0;
        switch (user.getRole()){
            case "user":
                role = 2;
                break;
            case "admin":
                role = 1;
                break;
            case "worker":
                role = 3;
                break;
        }
        ps.setInt(1, role);
        ps.setString(2, user.getLogin());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getName());
        ps.setInt(5, user.getAccount());
    }

    @Override
    public boolean create(User user) {
        PreparedStatement ps = getPrepareStatement(SQL_CREATE_USER);
        try {
            choseRole(user, ps);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void block(int id) {
        PreparedStatement ps = getPrepareStatement(SQL_BLOCK);
        try {
            ps.setInt(1, 3);
            ps.setInt(2, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unlock(int id) {
        PreparedStatement ps = getPrepareStatement(SQL_BLOCK);
        try {
            ps.setInt(1, 2);
            ps.setInt(2, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteCredit(Integer id) {
        PreparedStatement st = getPrepareStatement(SQL_DELETE_CREDIT);

        try {
            st.setInt(1, id);
            int i = st.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
