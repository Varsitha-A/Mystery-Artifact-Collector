import java.sql.*;
import java.util.*;

public class MysteryArtifactCollector {
    static Scanner sc = new Scanner(System.in);
    static Connection conn;

    public static void main(String[] args) {
        connectDB();
        System.out.println("Welcome to Mystery Artifact Collector!");
        System.out.print("Enter username: ");
        String user = sc.nextLine();
        int userId = getOrCreateUser(user);

        while (true) {
            System.out.println("\n1. Explore");
            System.out.println("2. My Inventory");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            int ch = sc.nextInt();
            sc.nextLine();

            if (ch == 1) explore(userId);
            else if (ch == 2) showInventory(userId);
            else break;
        }
    }

    static void connectDB() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/artifact_db",
                "root",
                "varsitha@2005"
            );
        } catch (Exception e) {}
    }

    static int getOrCreateUser(String username) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM users WHERE username=?"
            );
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

            ps = conn.prepareStatement(
                "INSERT INTO users(username) VALUES(?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, username);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {}
        return -1;
    }

    static void explore(int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM artifacts ORDER BY RAND() LIMIT 1"
            );
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int aid = rs.getInt("id");
                String name = rs.getString("name");
                String rare = rs.getString("rarity");
                String loc = rs.getString("location");

                System.out.println("\nYou travel to: " + loc);
                System.out.println("You found: " + name + " (" + rare + ")");

                ps = conn.prepareStatement(
                    "INSERT INTO inventory(user_id, artifact_id) VALUES(?,?)"
                );
                ps.setInt(1, userId);
                ps.setInt(2, aid);
                ps.executeUpdate();
                System.out.println("Added to inventory!");
            }
        } catch (Exception e) {}
    }

    static void showInventory(int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT artifacts.name, artifacts.rarity, artifacts.location, inventory.collected_at FROM inventory JOIN artifacts ON inventory.artifact_id=artifacts.id WHERE inventory.user_id=?"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\nYour Inventory:");
            int c = 0;
            while (rs.next()) {
                c++;
                System.out.println(c + ". " + rs.getString(1) + " | " + rs.getString(2) + " | " +
                                   rs.getString(3) + " | " + rs.getString(4));
            }
            if (c == 0) System.out.println("Empty.");
        } catch (Exception e) {}
    }
}
