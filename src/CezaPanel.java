import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class CezaPanel {
    public static void show() {
        JDialog dlg = new JDialog(LoginPanel.mainFrame, "Ceza Görüntüle", true);
        dlg.setSize(700, 450);
        dlg.setLocationRelativeTo(LoginPanel.mainFrame);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cmbUye = new JComboBox<>();
        cmbUye.addItem("Tüm Üyeler");
        Map<String, Integer> uyeMap = new HashMap<>();
        try {
            ResultSet rs = Database.getConnection().createStatement()
                    .executeQuery("SELECT UyeID, Ad || ' ' || Soyad as AdSoyad FROM UYE");
            while (rs.next()) {
                String ad = rs.getString(2);
                cmbUye.addItem(ad);
                uyeMap.put(ad, rs.getInt(1));
            }
            rs.close();
        } catch (SQLException ex) {
        }
        JButton btnFiltre = new JButton("Filtrele");
        topPanel.add(new JLabel("Üye:"));
        topPanel.add(cmbUye);
        topPanel.add(btnFiltre);
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "CezaID", "Üye", "Kitap", "Gecikme Gün", "Tutar", "Tarih" }, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        JLabel lblToplam = new JLabel("Toplam Borç: 0 TL");
        Runnable loadData = () -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT c.CezaID, u.Ad || ' ' || u.Soyad as Uye, k.KitapAdi, c.GecikmeGunu, c.Tutar, c.OlusturmaTarihi FROM CEZA c JOIN UYE u ON c.UyeID=u.UyeID JOIN ODUNC o ON c.OduncID=o.OduncID JOIN KITAP k ON o.KitapID=k.KitapID";
                String selected = (String) cmbUye.getSelectedItem();
                if (!selected.equals("Tüm Üyeler"))
                    sql += " WHERE c.UyeID=" + uyeMap.get(selected);
                ResultSet rs = Database.getConnection().createStatement().executeQuery(sql);
                double toplam = 0;
                while (rs.next()) {
                    model.addRow(new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4),
                            rs.getBigDecimal(5), rs.getDate(6) });
                    toplam += rs.getDouble(5);
                }
                rs.close();
                lblToplam.setText("Toplam Borç: " + toplam + " TL");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        };
        loadData.run();
        btnFiltre.addActionListener(e -> loadData.run());
        dlg.add(topPanel, BorderLayout.NORTH);
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.add(lblToplam, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
