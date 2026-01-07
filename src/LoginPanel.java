import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPanel {
    public static int currentUserID;
    public static String currentUserName;
    public static JFrame mainFrame;

    public static void show() {
        JFrame frame = new JFrame("Kütüphane Giriş");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 200);
        frame.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Kullanıcı Adı:"), c);
        c.gridx = 1;
        JTextField txtUser = new JTextField(15);
        panel.add(txtUser, c);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Şifre:"), c);
        c.gridx = 1;
        JPasswordField txtPass = new JPasswordField(15);
        panel.add(txtPass, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        JButton btnLogin = new JButton("Giriş");
        panel.add(btnLogin, c);
        btnLogin.addActionListener(e -> {
            try {
                Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT KullaniciID, AdSoyad FROM KULLANICI WHERE KullaniciAdi=? AND Sifre=?");
                ps.setString(1, txtUser.getText());
                ps.setString(2, new String(txtPass.getPassword()));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentUserID = rs.getInt(1);
                    currentUserName = rs.getString(2);
                    frame.dispose();
                    DashboardPanel.show();
                } else {
                    JOptionPane.showMessageDialog(frame, "Hatalı kullanıcı adı veya şifre!");
                }
                rs.close();
                ps.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Veritabanı hatası: " + ex.getMessage());
            }
        });
        frame.add(panel);
        frame.setVisible(true);
    }
}
