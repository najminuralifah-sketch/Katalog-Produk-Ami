package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import data.ProdukRepository;
import model.Produk;

public class KatalogController {

    @FXML private TextField namaField;
    @FXML private TextField hargaField;
    @FXML private ComboBox<String> fotoBox;
    @FXML private Label pesanLabel;
    @FXML private Label jumlahLabel;
    @FXML private TilePane katalogPane;
    @FXML private Button simpanButton;

    private final ProdukRepository repository = new ProdukRepository();
    private long produkDipilihId = -1;

    @FXML
    private void initialize() {
        fotoBox.getItems().addAll("roti.png", "susu.png", "kopi.png");
        fotoBox.setValue("roti.png");
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }

    private void showData() throws SQLException {
        ArrayList<Produk> daftar = repository.getAll();
        katalogPane.getChildren().clear();
        for (Produk produk : daftar) {
            katalogPane.getChildren().add(createProductCard(produk));
        }
        jumlahLabel.setText("Jumlah produk: " + daftar.size());
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);

        StackPane bingkai = new StackPane();
        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        URL lokasi = getClass().getResource("/image/" + produk.getFoto());
        if (lokasi != null) {
            Image gambar = new Image(lokasi.toExternalForm(), 170, 110, true, true);
            if (!gambar.isError()) {
                fotoView.setImage(gambar);
                bingkai.getChildren().add(fotoView);
            } else {
                bingkai.getChildren().add(new Label("Gambar tidak terbaca"));
            }
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");
        Label hargaLabel = new Label("Rp" + produk.getHarga());
        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");

        Button editButton = new Button("Update");
        editButton.setOnAction(event -> pilihProdukUntukDiedit(produk));
        Button hapusButton = new Button("Delete");
        hapusButton.setOnAction(event -> hapusProduk(produk));

        javafx.scene.layout.HBox aksi = new javafx.scene.layout.HBox(8, editButton, hapusButton);
        VBox kartu = new VBox(8, bingkai, namaLabel, hargaLabel, idLabel, aksi);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);
        kartu.getStyleClass().add("kartu-produk");
        return kartu;
    }

    private void pilihProdukUntukDiedit(Produk produk) {
        produkDipilihId = produk.getId();
        namaField.setText(produk.getNama());
        hargaField.setText(String.valueOf(produk.getHarga()));
        fotoBox.setValue(produk.getFoto());
        simpanButton.setText("Update Produk");
        pesanLabel.setText("Sedang mengubah produk ID: " + produk.getId());
    }

    @FXML
    private void saveProduct() {
        try {
            String nama = namaField.getText();
            long harga = Long.parseLong(hargaField.getText().trim());
            Produk produk = new Produk(
                    produkDipilihId == -1 ? 0 : produkDipilihId,
                    nama, harga, fotoBox.getValue());

            if (produkDipilihId == -1) {
                repository.add(produk);
                pesanLabel.setText("Produk berhasil ditambahkan.");
            } else {
                repository.update(produk);
                pesanLabel.setText("Produk berhasil diperbarui.");
            }
            resetForm();
            showData();
        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus angka bulat, contoh 12000.");
        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());
        } catch (SQLException e) {
            pesanLabel.setText("Gagal menyimpan perubahan. Lihat Output.");
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelEdit() {
        resetForm();
        pesanLabel.setText("Mode edit dibatalkan.");
    }

    private void resetForm() {
        produkDipilihId = -1;
        namaField.clear();
        hargaField.clear();
        fotoBox.setValue("roti.png");
        simpanButton.setText("Simpan");
    }

    private void hapusProduk(Produk produk) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Delete");
        konfirmasi.setHeaderText("Hapus produk " + produk.getNama() + "?");
        konfirmasi.setContentText("Data produk ini akan dihapus dari katalog.");
        if (konfirmasi.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                == javafx.scene.control.ButtonType.OK) {
            try {
                repository.delete(produk.getId());
                if (produkDipilihId == produk.getId()) {
                    resetForm();
                }
                showData();
                pesanLabel.setText("Produk berhasil dihapus.");
            } catch (SQLException e) {
                pesanLabel.setText("Gagal menghapus produk. Lihat Output.");
                e.printStackTrace();
            }
        }
    }
}
