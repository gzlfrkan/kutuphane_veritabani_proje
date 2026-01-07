import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.io.*;

public class DinamikSorgu {
    public static void show() {
        JDialog dlg = new JDialog(LoginPanel.mainFrame, "Dinamik Sorgu", true);
        dlg.setSize(850, 550);
        dlg.setLocationRelativeTo(LoginPanel.mainFrame);
        JPanel filterPanel = new JPanel(new GridLayout(4, 4, 5, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtreler"));
        JTextField txtKitapAdi = new JTextField();
        JTextField txtYazar = new JTextField();
        JComboBox<String> cmbKategori = new JComboBox<>();
        cmbKategori.addItem("");
        JTextField txtYilMin = new JTextField();
        JTextField txtYilMax = new JTextField();
        JCheckBox chkMevcut = new JCheckBox("Sadece Mevcut Kitaplar");
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
        filterPanel.add(new JLabel("Kitap Adı:"));
        filterPanel.add(txtKitapAdi);
        filterPanel.add(new JLabel("Yazar:"));
        filterPanel.add(txtYazar);
        filterPanel.add(new JLabel("Kategori:"));
        filterPanel.add(cmbKategori);
        filterPanel.add(new JLabel("Min Basım Yılı:"));
        filterPanel.add(txtYilMin);
        filterPanel.add(new JLabel("Max Basım Yılı:"));
        filterPanel.add(txtYilMax);
        filterPanel.add(chkMevcut);
        filterPanel.add(new JLabel(""));
        JButton btnSorgula = new JButton("Sorgula");
        JButton btnExcel = new JButton("Excel İndir");
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnSorgula);
        btnPanel.add(btnExcel);
        filterPanel.add(btnPanel);
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "Kitap Adı", "Yazar", "Kategori", "Yayınevi", "Yıl", "Toplam", "Mevcut" }, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        btnSorgula.addActionListener(e -> {
            model.setRowCount(0);
            try {
                StringBuilder sql = new StringBuilder(
                        "SELECT k.KitapID, k.KitapAdi, k.Yazar, c.KategoriAdi, k.Yayinevi, k.BasimYili, k.ToplamAdet, k.MevcutAdet FROM KITAP k LEFT JOIN KATEGORI c ON k.KategoriID=c.KategoriID WHERE 1=1");
                java.util.List<Object> params = new ArrayList<>();
                if (!txtKitapAdi.getText().trim().isEmpty()) {
                    sql.append(" AND k.KitapAdi ILIKE ?");
                    params.add("%" + txtKitapAdi.getText().trim() + "%");
                }
                if (!txtYazar.getText().trim().isEmpty()) {
                    sql.append(" AND k.Yazar ILIKE ?");
                    params.add("%" + txtYazar.getText().trim() + "%");
                }
                String kat = (String) cmbKategori.getSelectedItem();
                if (kat != null && !kat.isEmpty()) {
                    sql.append(" AND k.KategoriID = ?");
                    params.add(kategoriMap.get(kat));
                }
                if (!txtYilMin.getText().trim().isEmpty()) {
                    sql.append(" AND k.BasimYili >= ?");
                    params.add(Integer.parseInt(txtYilMin.getText().trim()));
                }
                if (!txtYilMax.getText().trim().isEmpty()) {
                    sql.append(" AND k.BasimYili <= ?");
                    params.add(Integer.parseInt(txtYilMax.getText().trim()));
                }
                if (chkMevcut.isSelected()) {
                    sql.append(" AND k.MevcutAdet > 0");
                }
                PreparedStatement ps = Database.getConnection().prepareStatement(sql.toString());
                for (int i = 0; i < params.size(); i++) {
                    Object p = params.get(i);
                    if (p instanceof String)
                        ps.setString(i + 1, (String) p);
                    else if (p instanceof Integer)
                        ps.setInt(i + 1, (Integer) p);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getInt(6), rs.getInt(7), rs.getInt(8) });
                }
                rs.close();
                ps.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        });
        btnExcel.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("kitaplar.csv"));
            if (fc.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter pw = new PrintWriter(
                        new OutputStreamWriter(new FileOutputStream(fc.getSelectedFile()), "UTF-8"))) {
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        pw.print(model.getColumnName(i) + (i < model.getColumnCount() - 1 ? ";" : ""));
                    }
                    pw.println();
                    for (int r = 0; r < model.getRowCount(); r++) {
                        for (int c = 0; c < model.getColumnCount(); c++) {
                            pw.print(model.getValueAt(r, c) + (c < model.getColumnCount() - 1 ? ";" : ""));
                        }
                        pw.println();
                    }
                    JOptionPane.showMessageDialog(dlg, "Dosya kaydedildi.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, ex.getMessage());
                }
            }
        });
        JPanel north = new JPanel(new BorderLayout());
        north.add(filterPanel, BorderLayout.CENTER);
        dlg.add(north, BorderLayout.NORTH);
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.setVisible(true);
    }
}
