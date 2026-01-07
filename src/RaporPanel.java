import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class RaporPanel {
    public static void show() {
        JDialog dlg = new JDialog(LoginPanel.mainFrame, "Raporlar", true);
        dlg.setSize(800, 550);
        dlg.setLocationRelativeTo(LoginPanel.mainFrame);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ödünç Raporu", createOduncRaporPanel());
        tabs.addTab("Geciken Kitaplar", createGecikenPanel());
        tabs.addTab("En Çok Ödünç Alınan", createEnCokPanel());
        dlg.add(tabs);
        dlg.setVisible(true);
    }

    private static JPanel createOduncRaporPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtBaslangic = new JTextField(10);
        JTextField txtBitis = new JTextField(10);
        JButton btnRapor = new JButton("Rapor Al");
        filterPanel.add(new JLabel("Başlangıç (YYYY-MM-DD):"));
        filterPanel.add(txtBaslangic);
        filterPanel.add(new JLabel("Bitiş:"));
        filterPanel.add(txtBitis);
        filterPanel.add(btnRapor);
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Üye", "Kitap", "Ödünç Tarihi", "Teslim Tarihi", "Durum" }, 0);
        JTable table = new JTable(model);
        btnRapor.addActionListener(e -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT u.Ad || ' ' || u.Soyad, k.KitapAdi, o.OduncTarihi, o.TeslimTarihi, CASE WHEN o.TeslimTarihi IS NULL THEN 'Aktif' ELSE 'Teslim Edildi' END FROM ODUNC o JOIN UYE u ON o.UyeID=u.UyeID JOIN KITAP k ON o.KitapID=k.KitapID WHERE o.OduncTarihi BETWEEN ? AND ?";
                PreparedStatement ps = Database.getConnection().prepareStatement(sql);
                ps.setDate(1, java.sql.Date.valueOf(txtBaslangic.getText()));
                ps.setDate(2, java.sql.Date.valueOf(txtBitis.getText()));
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    model.addRow(new Object[] { rs.getString(1), rs.getString(2), rs.getDate(3), rs.getDate(4),
                            rs.getString(5) });
                rs.close();
                ps.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createGecikenPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Üye", "Kitap", "Ödünç Tarihi", "Son Teslim", "Gecikme Gün" }, 0);
        JTable table = new JTable(model);
        JButton btnYenile = new JButton("Yenile");
        Runnable load = () -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT u.Ad || ' ' || u.Soyad, k.KitapAdi, o.OduncTarihi, o.SonTeslimTarihi, CURRENT_DATE - o.SonTeslimTarihi as Gecikme FROM ODUNC o JOIN UYE u ON o.UyeID=u.UyeID JOIN KITAP k ON o.KitapID=k.KitapID WHERE o.TeslimTarihi IS NULL AND o.SonTeslimTarihi < CURRENT_DATE";
                ResultSet rs = Database.getConnection().createStatement().executeQuery(sql);
                while (rs.next())
                    model.addRow(new Object[] { rs.getString(1), rs.getString(2), rs.getDate(3), rs.getDate(4),
                            rs.getInt(5) });
                rs.close();
            } catch (SQLException ex) {
            }
        };
        load.run();
        btnYenile.addActionListener(e -> load.run());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnYenile, BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel createEnCokPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtBaslangic = new JTextField(10);
        JTextField txtBitis = new JTextField(10);
        JButton btnRapor = new JButton("Rapor Al");
        filterPanel.add(new JLabel("Başlangıç (YYYY-MM-DD):"));
        filterPanel.add(txtBaslangic);
        filterPanel.add(new JLabel("Bitiş:"));
        filterPanel.add(txtBitis);
        filterPanel.add(btnRapor);
        DefaultTableModel model = new DefaultTableModel(new String[] { "Kitap Adı", "Yazar", "Ödünç Sayısı" }, 0);
        JTable table = new JTable(model);
        btnRapor.addActionListener(e -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT k.KitapAdi, k.Yazar, COUNT(*) as Sayi FROM ODUNC o JOIN KITAP k ON o.KitapID=k.KitapID WHERE o.OduncTarihi BETWEEN ? AND ? GROUP BY k.KitapID, k.KitapAdi, k.Yazar ORDER BY Sayi DESC";
                PreparedStatement ps = Database.getConnection().prepareStatement(sql);
                ps.setDate(1, java.sql.Date.valueOf(txtBaslangic.getText()));
                ps.setDate(2, java.sql.Date.valueOf(txtBitis.getText()));
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    model.addRow(new Object[] { rs.getString(1), rs.getString(2), rs.getInt(3) });
                rs.close();
                ps.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}
