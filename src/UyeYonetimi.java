import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class UyeYonetimi {
    public static void show() {
        JDialog dlg = new JDialog(LoginPanel.mainFrame, "Üye Yönetimi", true);
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(LoginPanel.mainFrame);
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "Ad", "Soyad", "Telefon", "Email", "Borç" }, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtArama = new JTextField(15);
        JButton btnAra = new JButton("Ara");
        topPanel.add(new JLabel("Arama:"));
        topPanel.add(txtArama);
        topPanel.add(btnAra);
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        JTextField txtAd = new JTextField();
        JTextField txtSoyad = new JTextField();
        JTextField txtTel = new JTextField();
        JTextField txtEmail = new JTextField();
        formPanel.add(new JLabel("Ad:"));
        formPanel.add(txtAd);
        formPanel.add(new JLabel("Soyad:"));
        formPanel.add(txtSoyad);
        formPanel.add(new JLabel("Telefon:"));
        formPanel.add(txtTel);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(txtEmail);
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnEkle = new JButton("Ekle");
        JButton btnGuncelle = new JButton("Güncelle");
        JButton btnSil = new JButton("Sil");
        btnPanel.add(btnEkle);
        btnPanel.add(btnGuncelle);
        btnPanel.add(btnSil);
        Runnable loadData = () -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT * FROM UYE";
                String arama = txtArama.getText().trim();
                if (!arama.isEmpty())
                    sql += " WHERE Ad ILIKE ? OR Soyad ILIKE ? OR Email ILIKE ?";
                PreparedStatement ps = Database.getConnection().prepareStatement(sql);
                if (!arama.isEmpty()) {
                    ps.setString(1, "%" + arama + "%");
                    ps.setString(2, "%" + arama + "%");
                    ps.setString(3, "%" + arama + "%");
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    model.addRow(new Object[] { rs.getInt("UyeID"), rs.getString("Ad"), rs.getString("Soyad"),
                            rs.getString("Telefon"), rs.getString("Email"), rs.getBigDecimal("ToplamBorc") });
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
                txtSoyad.setText((String) model.getValueAt(row, 2));
                txtTel.setText((String) model.getValueAt(row, 3));
                txtEmail.setText((String) model.getValueAt(row, 4));
            }
        });
        btnEkle.addActionListener(e -> {
            try {
                PreparedStatement ps = Database.getConnection()
                        .prepareStatement("INSERT INTO UYE (Ad, Soyad, Telefon, Email) VALUES (?, ?, ?, ?)");
                ps.setString(1, txtAd.getText());
                ps.setString(2, txtSoyad.getText());
                ps.setString(3, txtTel.getText());
                ps.setString(4, txtEmail.getText());
                ps.executeUpdate();
                ps.close();
                loadData.run();
                JOptionPane.showMessageDialog(dlg, "Üye eklendi.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        });
        btnGuncelle.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Bir üye seçin.");
                return;
            }
            try {
                PreparedStatement ps = Database.getConnection()
                        .prepareStatement("UPDATE UYE SET Ad=?, Soyad=?, Telefon=?, Email=? WHERE UyeID=?");
                ps.setString(1, txtAd.getText());
                ps.setString(2, txtSoyad.getText());
                ps.setString(3, txtTel.getText());
                ps.setString(4, txtEmail.getText());
                ps.setInt(5, (Integer) model.getValueAt(row, 0));
                ps.executeUpdate();
                ps.close();
                loadData.run();
                JOptionPane.showMessageDialog(dlg, "Üye güncellendi.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        });
        btnSil.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Bir üye seçin.");
                return;
            }
            int id = (Integer) model.getValueAt(row, 0);
            try {
                PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM UYE WHERE UyeID=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                ps.close();
                loadData.run();
                JOptionPane.showMessageDialog(dlg, "Üye silindi.");
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
