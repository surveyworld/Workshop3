package pl.coderslab.utils;

import org.mindrot.jbcrypt.BCrypt;
import pl.coderslab.utils.DbUtil;
import pl.coderslab.utils.User;

import java.sql.*;
import java.util.Arrays;

public class UserDao {

    private static final String CREATE_USER_QUERY = "INSERT INTO users(username, email, password) VALUES(?,?,?)";

    private static final String SELECT_ID = "SELECT * FROM users WHERE id=?;";


    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public User create(User user) {
        try (Connection conn = DbUtil.getConnection()) {
            PreparedStatement statement =
                    conn.prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getEmail());
            statement.setString(3, hashPassword(user.getPassword()));
            statement.executeUpdate();
            //Pobieramy wstawiony do bazy identyfikator, a następnie ustawiamy id obiektu user.
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                user.setId(resultSet.getInt(1));
            }
            return user;
        } catch (SQLException e) { //  pierwotny kod    } catch (SQLException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User read(int readId) throws SQLException {
        User user = new User();
        try (Connection conn = DbUtil.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(SELECT_ID.replace("?", String.valueOf(readId)));
            ResultSet wynik = statement.executeQuery();
            while (wynik.next()) {
                user.setId(wynik.getInt("id"));
                user.setUserName(wynik.getString("username"));
                user.setEmail(wynik.getString("email"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user.getId() != 0) {
            return user;
        } else {
            System.out.println("W bazie nie ma obieku o id=" + readId);
            return null;
        }
    }

    public static final String UPDATE = "UPDATE users SET username=?, email=?, password=?";

    public void update(User user) throws SQLException {
        User updaed = read(user.getId());
        try (Connection conn = DbUtil.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(UPDATE);
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getEmail());
            statement.setString(3, hashPassword(user.getPassword()));
            statement.executeUpdate();

        }
    }

    public static final String DELETE = "DELETE FROM users WHERE id=?";

    public void delete(int userId) throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(DELETE);
            statement.setString(1, String.valueOf(userId));
            statement.executeUpdate();
        }
    }

    public  User[] users = new User[0];

    private static User[] addToArray(User u, User[] users) {
        User[] tmpUsers = Arrays.copyOf(users, users.length + 1); // Tworzymy kopię tablicy powiększoną o 1.
        tmpUsers[users.length] = u; // Dodajemy obiekt na ostatniej pozycji.
        return tmpUsers; // Zwracamy nową tablicę.
    }

    public static final String ALL = "SELECT * FROM users";


    public  void findAll() throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(ALL);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                User user = new User();
                user.setId(result.getInt("id"));
                user.setUserName(result.getString("username"));
                user.setEmail(result.getString("email"));
                users=addToArray(user, users);


            }
        }

    }


}
