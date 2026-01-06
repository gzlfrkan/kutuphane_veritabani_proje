import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Date;

public class Main {
    static Connection conn;
    static int currentUserID;
    static String currentUserName;
    static JFrame mainFrame;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> showLogin());
    }

    static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            String url = "jdbc:postgresql://localhost:5432/kutuphane";
            String user = "postgres";
            String password = "cool";
            conn = DriverManager.getConnection(url, user, password);
        }
        return conn;
    }

    static void showLogin() {
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
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT KullaniciID, AdSoyad FROM KULLANICI WHERE KullaniciAdi=? AND Sifre=?");
                ps.setString(1, txtUser.getText());
                ps.setString(2, new String(txtPass.getPassword()));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentUserID = rs.getInt(1);
                    currentUserName = rs.getString(2);
                    frame.dispose();
                    showDashboard();
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

    static void showDashboard() {
        mainFrame = new JFrame("Kütüphane Yönetim Sistemi");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(4, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JLabel lbl = new JLabel("Hoş geldiniz, " + currentUserName, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        String[] buttons = { "Üye Yönetimi", "Kitap Yönetimi", "Ödünç Ver", "Kitap Teslim Al", "Ceza Görüntüle",
                "Raporlar", "Dinamik Sorgu", "Çıkış" };
        for (String txt : buttons) {
            JButton btn = new JButton(txt);
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            btn.addActionListener(e -> {
                switch (txt) {
                    case "Üye Yönetimi":
                        showUyeYonetimi();
                        break;
                    case "Kitap Yönetimi":
                        showKitapYonetimi();
                        break;
                    case "Ödünç Ver":
                        showOduncVer();
                        break;
                    case "Kitap Teslim Al":
                        showTeslimAl();
                        break;
                    case "Ceza Görüntüle":
                        showCeza();
                        break;
                    case "Raporlar":
                        showRaporlar();
                        break;
                    case "Dinamik Sorgu":
                        showDinamikSorgu();
                        break;
                    case "Çıkış":
                        mainFrame.dispose();
                        showLogin();
                        break;
                }
            });
            panel.add(btn);
        }
        mainFrame.add(lbl, BorderLayout.NORTH);
        mainFrame.add(panel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }

    static void showUyeYonetimi() {
        JDialog dlg = new JDialog(mainFrame, "Üye Yönetimi", true);
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(mainFrame);
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
                PreparedStatement ps = getConnection().prepareStatement(sql);
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
                PreparedStatement ps = getConnection()
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
                PreparedStatement ps = getConnection()
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
                PreparedStatement ps = getConnection().prepareStatement("DELETE FROM UYE WHERE UyeID=?");
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

    static void showKitapYonetimi() {
        JDialog dlg = new JDialog(mainFrame, "Kitap Yönetimi", true);
        dlg.setSize(800, 550);
        dlg.setLocationRelativeTo(mainFrame);
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
            ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM KATEGORI");
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
                PreparedStatement ps = getConnection().prepareStatement(sql);
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
                PreparedStatement ps = getConnection().prepareStatement(
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
                PreparedStatement ps = getConnection().prepareStatement(
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
                PreparedStatement ps = getConnection().prepareStatement("DELETE FROM KITAP WHERE KitapID=?");
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

    static void showOduncVer() {
        JDialog dlg = new JDialog(mainFrame, "Ödünç Ver", true);
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(mainFrame);
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
            ResultSet rs = getConnection().createStatement().executeQuery("SELECT UyeID, Ad, Soyad, Email FROM UYE");
            while (rs.next())
                uyeModel.addRow(new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4) });
            rs.close();
            rs = getConnection().createStatement()
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
                PreparedStatement ps = getConnection().prepareStatement("SELECT sp_YeniOduncVer(?, ?, ?)");
                ps.setInt(1, uyeID);
                ps.setInt(2, kitapID);
                ps.setInt(3, currentUserID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String sonuc = rs.getString(1);
                    JOptionPane.showMessageDialog(dlg, sonuc);
                    if (sonuc.startsWith("BASARILI")) {
                        kitapModel.setRowCount(0);
                        ResultSet rs2 = getConnection().createStatement().executeQuery(
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

    static void showTeslimAl() {
        JDialog dlg = new JDialog(mainFrame, "Kitap Teslim Al", true);
        dlg.setSize(750, 450);
        dlg.setLocationRelativeTo(mainFrame);
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ÖdünçID", "Üye", "Kitap", "Ödünç Tarihi", "Son Teslim" }, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        JButton btnTeslim = new JButton("Teslim Al");
        Runnable loadData = () -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT o.OduncID, u.Ad || ' ' || u.Soyad as Uye, k.KitapAdi, o.OduncTarihi, o.SonTeslimTarihi FROM ODUNC o JOIN UYE u ON o.UyeID=u.UyeID JOIN KITAP k ON o.KitapID=k.KitapID WHERE o.TeslimTarihi IS NULL";
                ResultSet rs = getConnection().createStatement().executeQuery(sql);
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
                PreparedStatement ps = getConnection().prepareStatement("SELECT sp_KitapTeslimAl(?, CURRENT_DATE)");
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

    static void showCeza() {
        JDialog dlg = new JDialog(mainFrame, "Ceza Görüntüle", true);
        dlg.setSize(700, 450);
        dlg.setLocationRelativeTo(mainFrame);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cmbUye = new JComboBox<>();
        cmbUye.addItem("Tüm Üyeler");
        Map<String, Integer> uyeMap = new HashMap<>();
        try {
            ResultSet rs = getConnection().createStatement()
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
                ResultSet rs = getConnection().createStatement().executeQuery(sql);
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

    static void showRaporlar() {
        JDialog dlg = new JDialog(mainFrame, "Raporlar", true);
        dlg.setSize(800, 550);
        dlg.setLocationRelativeTo(mainFrame);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ödünç Raporu", createOduncRaporPanel());
        tabs.addTab("Geciken Kitaplar", createGecikenPanel());
        tabs.addTab("En Çok Ödünç Alınan", createEnCokPanel());
        dlg.add(tabs);
        dlg.setVisible(true);
    }

    static JPanel createOduncRaporPanel() {
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
                PreparedStatement ps = getConnection().prepareStatement(sql);
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

    static JPanel createGecikenPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Üye", "Kitap", "Ödünç Tarihi", "Son Teslim", "Gecikme Gün" }, 0);
        JTable table = new JTable(model);
        JButton btnYenile = new JButton("Yenile");
        Runnable load = () -> {
            model.setRowCount(0);
            try {
                String sql = "SELECT u.Ad || ' ' || u.Soyad, k.KitapAdi, o.OduncTarihi, o.SonTeslimTarihi, CURRENT_DATE - o.SonTeslimTarihi as Gecikme FROM ODUNC o JOIN UYE u ON o.UyeID=u.UyeID JOIN KITAP k ON o.KitapID=k.KitapID WHERE o.TeslimTarihi IS NULL AND o.SonTeslimTarihi < CURRENT_DATE";
                ResultSet rs = getConnection().createStatement().executeQuery(sql);
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

    static JPanel createEnCokPanel() {
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
                PreparedStatement ps = getConnection().prepareStatement(sql);
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

    static void showDinamikSorgu() {
        JDialog dlg = new JDialog(mainFrame, "Dinamik Sorgu", true);
        dlg.setSize(850, 550);
        dlg.setLocationRelativeTo(mainFrame);
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
            ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM KATEGORI");
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
                PreparedStatement ps = getConnection().prepareStatement(sql.toString());
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
