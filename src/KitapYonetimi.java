import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class KitapYonetimi {
    public static void show() {
        JDialog dlg = new JDialog(LoginPanel.mainFrame, "Kitap Yönetimi", true);
        dlg.setSize(800, 550);
        dlg.setLocationRelativeTo(LoginPanel.mainFrame);
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "Kitap Adı", "Yazar", "Kategori", "Yayınevi", "Yıl", "Toplam", "Mevcut" }, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtArama = new JTextField(15);
        JButton btnAra = new JButton("Ara");
        topPanel.add(new JLabel("Arama:"));
        topPanel.add(txtArama);
        topPanel.add(btnAra);
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        JTextField txtAd = new JTextField();
        JTextField txtYazar = new JTextField();
        JComboBox<String> cmbKategori = new JComboBox<>();
        JTextField txtYayinevi = new JTextField();
        JTextField txtYil = new JTextField();
        JTextField txtAdet = new JTextField();
        formPanel.add(new JLabel("Kitap Adı:"));
        formPanel.add(txtAd);
        formPanel.add(new JLabel("Yazar:"));
        formPanel.add(txtYazar);
        formPanel.add(new JLabel("Kategori:"));
        formPanel.add(cmbKategori);
        formPanel.add(new JLabel("Yayınevi:"));
        formPanel.add(txtYayinevi);
        formPanel.add(new JLabel("Basım Yılı:"));
        formPanel.add(txtYil);
        formPanel.add(new JLabel("Adet:"));
        formPanel.add(txtAdet);
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnEkle = new JButton("Ekle");
        JButton btnGuncelle = new JButton("Güncelle");
        JButton btnSil = new JButton("Sil");
        btnPanel.add(btnEkle);
        btnPanel.add(btnGuncelle);
        btnPanel.add(btnSil);
        Map<String, Integer> kategoriMap = new HashMap<>();
        try {
            ResultSet rs = Database.getConnection().createStatement().executeQuery("SELECT * FROM KATEGORI");
            while (rs.next()) {
                cmbKategori.addItem(rs.getString("KategoriAdi"));
                kategoriMap.put(rs.getString("KategoriAdi"), rs.getInt("KategoriID"));
            }
            rs.close();
        } catch (SQLException ex) {
        }
        Runnable loadData = () -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT k.*, c.KategoriAdi FROM KITAP k LEFT JOIN KATEGORI c ON k.KategoriID=c.KategoriID";
                String arama = txtArama.getText().trim();
                if (!arama.isEmpty())
                    sql += " WHERE k.KitapAdi ILIKE ? OR k.Yazar ILIKE ?";
                PreparedStatement ps = Database.getConnection().prepareStatement(sql);
                if (!arama.isEmpty()) {
                    ps.setString(1, "%" + arama + "%");
                    ps.setString(2, "%" + arama + "%");
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    model.addRow(new Object[] { rs.getInt("KitapID"), rs.getString("KitapAdi"), rs.getString("Yazar"),
                            rs.getString("KategoriAdi"), rs.getString("Yayinevi"), rs.getInt("BasimYili"),
                            rs.getInt("ToplamAdet"), rs.getInt("MevcutAdet") });
                rs.close();
                ps.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        };
        loadData.run();
        btnAra.addActionListener(e -> loadData.run());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtAd.setText((String) model.getValueAt(row, 1));
                txtYazar.setText((String) model.getValueAt(row, 2));
                cmbKategori.setSelectedItem(model.getValueAt(row, 3));
                txtYayinevi.setText((String) model.getValueAt(row, 4));
                txtYil.setText(String.valueOf(model.getValueAt(row, 5)));
                txtAdet.setText(String.valueOf(model.getValueAt(row, 6)));
            }
        });
        btnEkle.addActionListener(e -> {
            try {
                int adet = Integer.parseInt(txtAdet.getText());
                int katID = kategoriMap.get((String) cmbKategori.getSelectedItem());
                PreparedStatement ps = Database.getConnection().prepareStatement(
                        "INSERT INTO KITAP (KitapAdi, Yazar, KategoriID, Yayinevi, BasimYili, ToplamAdet, MevcutAdet) VALUES (?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, txtAd.getText());
                ps.setString(2, txtYazar.getText());
                ps.setInt(3, katID);
                ps.setString(4, txtYayinevi.getText());
                ps.setInt(5, Integer.parseInt(txtYil.getText()));
                ps.setInt(6, adet);
                ps.setInt(7, adet);
                ps.executeUpdate();
                ps.close();
                loadData.run();
                JOptionPane.showMessageDialog(dlg, "Kitap eklendi.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        });
        btnGuncelle.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Bir kitap seçin.");
                return;
            }
            try {
                int katID = kategoriMap.get((String) cmbKategori.getSelectedItem());
                PreparedStatement ps = Database.getConnection().prepareStatement(
                        "UPDATE KITAP SET KitapAdi=?, Yazar=?, KategoriID=?, Yayinevi=?, BasimYili=?, ToplamAdet=? WHERE KitapID=?");
                ps.setString(1, txtAd.getText());
                ps.setString(2, txtYazar.getText());
                ps.setInt(3, katID);
                ps.setString(4, txtYayinevi.getText());
                ps.setInt(5, Integer.parseInt(txtYil.getText()));
                ps.setInt(6, Integer.parseInt(txtAdet.getText()));
                ps.setInt(7, (Integer) model.getValueAt(row, 0));
                ps.executeUpdate();
                ps.close();
                loadData.run();
                JOptionPane.showMessageDialog(dlg, "Kitap güncellendi.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        });
        btnSil.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Bir kitap seçin.");
                return;
            }
            try {
                PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM KITAP WHERE KitapID=?");
                ps.setInt(1, (Integer) model.getValueAt(row, 0));
                ps.executeUpdate();
                ps.close();
                loadData.run();
                JOptionPane.showMessageDialog(dlg, "Kitap silindi.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        });
        JPanel south = new JPanel(new BorderLayout());
        south.add(formPanel, BorderLayout.CENTER);
        south.add(btnPanel, BorderLayout.SOUTH);
        dlg.add(topPanel, BorderLayout.NORTH);
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
