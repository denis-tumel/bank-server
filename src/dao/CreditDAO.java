package dao;

import model.Credit;
import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CreditDAO extends EntityDAO<Credit, Integer> {

    private UserDAO userDAO = new UserDAO();

    private static final String SQL_GET_CREDIT_BY_ID =
            "SELECT * FROM creditstable WHERE id = ?";

    private static final String SQL_UPDATE_BANK =
            "UPDATE users SET account = ? WHERE roleID = 1";

    private static final String SQL_UPDATE_CREDIT_USER_ID =
            "UPDATE credit_user SET money = ? WHERE user_id = ? AND credit_id = ?";

    private static final String SQL_GET_ALL =
            "SELECT * FROM creditstable;";

    private static final String SQL_DELETE_CREDIT_USER =
            "DELETE FROM credit_user WHERE credit_id = ? AND user_id = ?";

    private static final String SQL_CREATE_CREDIT =
            "INSERT INTO credit_user (credit_id, user_id, money) VALUES (?,?,?)";

    private static final String SQL_GET_ALL_BY_USER_ID =
            "SELECT creditstable.id, creditstable.money, creditstable.month, creditstable.creditsDecimal, creditstable.type, credit_user.money FROM creditstable JOIN credit_user ON creditstable.id = credit_user.credit_id WHERE credit_user.user_id = ?;";

    @Override
    public List<Credit> getAll() {
        List<Credit> list = new LinkedList<>();
        PreparedStatement ps = getPrepareStatement(SQL_GET_ALL);
        try {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Credit credit = new Credit();

                credit.setId(rs.getInt(1));
                credit.setMoney(rs.getInt(2));
                credit.setMonth(rs.getInt(3));
                credit.setDecimal(rs.getInt(5));
                credit.setName(rs.getString(4));
                credit.setSumma(rs.getInt(2)*100/rs.getInt(5)+rs.getInt(2));

                list.add(credit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePrepareStatement(ps);
        }

        return list;
    }


    @Override
    public boolean update(Credit entity) {
        return false;
    }

    @Override
    public Credit getEntityById(Integer id) {
        Credit credit = new Credit();

        PreparedStatement ps = getPrepareStatement( SQL_GET_CREDIT_BY_ID);

        try {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                credit = new Credit();
                credit.setId(rs.getInt(1));
                credit.setName(rs.getString(5));
                credit.setMoney(rs.getInt(2));
                credit.setMonth(rs.getInt(3));
                credit.setDecimal(rs.getInt(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePrepareStatement(ps);
        }

        return credit;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    public boolean delete(Integer creditId, Integer userId, Integer summa) {
        PreparedStatement st = getPrepareStatement(SQL_DELETE_CREDIT_USER);

        boolean isRemoved = false;

        try {
            st.setInt(1, creditId);
            st.setInt(2, userId);

            int i = st.executeUpdate();

            updateBankAccount(summa);
            isRemoved = i > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isRemoved;
    }

    private void updateBankAccount(Integer summa) {
        User user = userDAO.getAdmin();
        PreparedStatement ps = getPrepareStatement(SQL_UPDATE_BANK);
        try {
            ps.setInt(1, user.getAccount()+summa);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean create(Credit entity) {
        return false;
    }

    public List<Credit> getAllByUserId(String command) {
        List<Credit> listCredit = new ArrayList<>();

        PreparedStatement ps = getPrepareStatement(SQL_GET_ALL_BY_USER_ID);
        try {
            ps.setInt(1, Integer.parseInt(command));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Credit credit = new Credit();

                credit.setId(rs.getInt(1));
                credit.setMoney(rs.getInt(2));
                credit.setMonth(rs.getInt(3));
                credit.setDecimal(rs.getInt(4));
                credit.setName(rs.getString(5));
                credit.setSumma(rs.getInt(2)+(rs.getInt(2)*rs.getInt(4)/100));
                credit.setPayment(rs.getInt(6));

                listCredit.add(credit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePrepareStatement(ps);
        }

        return listCredit;
    }

    public boolean createCreditByUser(int creditID, int userID, int money) {
        PreparedStatement ps = getPrepareStatement(SQL_CREATE_CREDIT);

        try {
            ps.setInt(1, creditID);
            ps.setInt(2, userID);
            ps.setInt(3, money);

            ps.executeUpdate();
            User user = userDAO.getEntityById(userID);
            user.setAccount(user.getAccount()-money);

            userDAO.update(user);

            Credit credit = getEntityById(creditID);

            minusMoneyFromBank(credit.getMoney());

            minusMoneyFromBank(money);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void minusMoneyFromBank(int money) {
        PreparedStatement ps = getPrepareStatement(SQL_UPDATE_BANK);

        User user = userDAO.getAdmin();
        try {
            ps.setInt(1, user.getAccount()-money);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateCreditUser(int userID, int creditID, int sum, int temp) {
        PreparedStatement ps = getPrepareStatement(SQL_UPDATE_CREDIT_USER_ID);
        try {
            ps.setInt(1, sum);
            ps.setInt(2, userID);
            ps.setInt(3, creditID);

            ps.executeUpdate();
            User user = userDAO.getEntityById(userID);
            Integer account = user.getAccount();
            user.setAccount(account-temp);

            userDAO.update(user);

            plusMoneyFromBank(temp);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void plusMoneyFromBank(int temp) {
        PreparedStatement ps = getPrepareStatement(SQL_UPDATE_BANK);

        User user = userDAO.getAdmin();
        try {
            ps.setInt(1, user.getAccount()+temp);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
