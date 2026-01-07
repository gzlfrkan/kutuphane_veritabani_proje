import javax.swing.*;
import java.awt.*;

public class DashboardPanel {
    public static void show() {
        LoginPanel.mainFrame = new JFrame("Kütüphane Yönetim Sistemi");
        LoginPanel.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LoginPanel.mainFrame.setSize(800, 600);
        LoginPanel.mainFrame.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(4, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JLabel lbl = new JLabel("Hoş geldiniz, " + LoginPanel.currentUserName, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        String[] buttons = { "Üye Yönetimi", "Kitap Yönetimi", "Ödünç Ver", "Kitap Teslim Al", "Ceza Görüntüle",
                "Raporlar", "Dinamik Sorgu", "Çıkış" };
        for (String txt : buttons) {
            JButton btn = new JButton(txt);
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            btn.addActionListener(e -> {
                switch (txt) {
                    case "Üye Yönetimi":
                        UyeYonetimi.show();
                        break;
                    case "Kitap Yönetimi":
                        KitapYonetimi.show();
                        break;
                    case "Ödünç Ver":
                        OduncIslemleri.showOduncVer();
                        break;
                    case "Kitap Teslim Al":
                        OduncIslemleri.showTeslimAl();
                        break;
                    case "Ceza Görüntüle":
                        CezaPanel.show();
                        break;
                    case "Raporlar":
                        RaporPanel.show();
                        break;
                    case "Dinamik Sorgu":
                        DinamikSorgu.show();
                        break;
                    case "Çıkış":
                        LoginPanel.mainFrame.dispose();
                        LoginPanel.show();
                        break;
                }
            });
            panel.add(btn);
        }
        LoginPanel.mainFrame.add(lbl, BorderLayout.NORTH);
        LoginPanel.mainFrame.add(panel, BorderLayout.CENTER);
        LoginPanel.mainFrame.setVisible(true);
    }
}
