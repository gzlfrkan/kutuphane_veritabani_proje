import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class OduncIslemleri {
    public static void showOduncVer() {
        JDialog dlg = new JDialog(LoginPanel.mainFrame, "Ödünç Ver", true);
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(LoginPanel.mainFrame);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel uyePanel = new JPanel(new BorderLayout());
        uyePanel.setBorder(BorderFactory.createTitledBorder("Üye Seç"));
        DefaultTableModel uyeModel = new DefaultTableModel(new String[] { "ID", "Ad", "Soyad", "Email" }, 0);
        JTable uyeTable = new JTable(uyeModel);
        uyePanel.add(new JScrollPane(uyeTable), BorderLayout.CENTER);
        JPanel kitapPanel = new JPanel(new BorderLayout());
        kitapPanel.setBorder(BorderFactory.createTitledBorder("Kitap Seç"));
        DefaultTableModel kitapModel = new DefaultTableModel(new String[] { "ID", "Kitap Adı", "Yazar", "Mevcut" }, 0);
        JTable kitapTable = new JTable(kitapModel);
        kitapPanel.add(new JScrollPane(kitapTable), BorderLayout.CENTER);
        JButton btnOduncVer = new JButton("Ödünç Ver");
        try {
            ResultSet rs = Database.getConnection().createStatement()
                    .executeQuery("SELECT UyeID, Ad, Soyad, Email FROM UYE");
            while (rs.next())
                uyeModel.addRow(new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4) });
            rs.close();
            rs = Database.getConnection().createStatement()
                    .executeQuery("SELECT KitapID, KitapAdi, Yazar, MevcutAdet FROM KITAP WHERE MevcutAdet > 0");
            while (rs.next())
                kitapModel.addRow(new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4) });
            rs.close();
        } catch (SQLException ex) {
        }
        btnOduncVer.addActionListener(e -> {
            int uyeRow = uyeTable.getSelectedRow();
            int kitapRow = kitapTable.getSelectedRow();
            if (uyeRow < 0 || kitapRow < 0) {
                JOptionPane.showMessageDialog(dlg, "Üye ve kitap seçin.");
                return;
            }
            int uyeID = (Integer) uyeModel.getValueAt(uyeRow, 0);
            int kitapID = (Integer) kitapModel.getValueAt(kitapRow, 0);
            try {
                PreparedStatement ps = Database.getConnection().prepareStatement("SELECT sp_YeniOduncVer(?, ?, ?)");
                ps.setInt(1, uyeID);
                ps.setInt(2, kitapID);
                ps.setInt(3, LoginPanel.currentUserID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String sonuc = rs.getString(1);
                    JOptionPane.showMessageDialog(dlg, sonuc);
                    if (sonuc.startsWith("BASARILI")) {
                        kitapModel.setRowCount(0);
                        ResultSet rs2 = Database.getConnection().createStatement().executeQuery(
                                "SELECT KitapID, KitapAdi, Yazar, MevcutAdet FROM KITAP WHERE MevcutAdet > 0");
                        while (rs2.next())
                            kitapModel.addRow(
                                    new Object[] { rs2.getInt(1), rs2.getString(2), rs2.getString(3), rs2.getInt(4) });
                        rs2.close();
                    }
                }
                rs.close();
                ps.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        });
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, uyePanel, kitapPanel);
        split.setDividerLocation(300);
        panel.add(split, BorderLayout.CENTER);
        panel.add(btnOduncVer, BorderLayout.SOUTH);
        dlg.add(panel);
        dlg.setVisible(true);
    }

    public static void showTeslimAl() {
        JDialog dlg = new JDialog(LoginPanel.mainFrame, "Kitap Teslim Al", true);
        dlg.setSize(750, 450);
        dlg.setLocationRelativeTo(LoginPanel.mainFrame);
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ÖdünçID", "Üye", "Kitap", "Ödünç Tarihi", "Son Teslim" }, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        JButton btnTeslim = new JButton("Teslim Al");
        Runnable loadData = () -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT o.OduncID, u.Ad || ' ' || u.Soyad as Uye, k.KitapAdi, o.OduncTarihi, o.SonTeslimTarihi FROM ODUNC o JOIN UYE u ON o.UyeID=u.UyeID JOIN KITAP k ON o.KitapID=k.KitapID WHERE o.TeslimTarihi IS NULL";
                ResultSet rs = Database.getConnection().createStatement().executeQuery(sql);
                while (rs.next())
                    model.addRow(new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDate(4),
                            rs.getDate(5) });
                rs.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        };
        loadData.run();
        btnTeslim.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Bir kayıt seçin.");
                return;
            }
            int oduncID = (Integer) model.getValueAt(row, 0);
            try {
                PreparedStatement ps = Database.getConnection()
                        .prepareStatement("SELECT sp_KitapTeslimAl(?, CURRENT_DATE)");
                ps.setInt(1, oduncID);
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    JOptionPane.showMessageDialog(dlg, rs.getString(1));
                rs.close();
                ps.close();
                loadData.run();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        });
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.add(btnTeslim, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
