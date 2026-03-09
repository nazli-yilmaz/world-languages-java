import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;

import com.google.gson.*;

public class DunyaDilleriUygulamasi {

    static String dosyaAdi = "diller.txt";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int secim;

        do {
            System.out.println("\n=== Dünya Dilleri Bilgi Sistemi ===");
            System.out.println("1. Veri Çek (API'den)");
            System.out.println("2. Listele");
            System.out.println("3. Güncelle");
            System.out.println("4. Sil");
            System.out.println("5. Çıkış");
            System.out.print("Seçim yapınız: ");
            secim = Integer.parseInt(scanner.nextLine());

            switch (secim) {
                case 1 -> veriCek();
                case 2 -> listele(scanner);
                case 3 -> guncelle(scanner);
                case 4 -> sil(scanner);
                case 5 -> System.out.println("Çıkılıyor...");
                default -> System.out.println("Geçersiz seçim.");
            }

        } while (secim != 5);
    }

    public static void veriCek() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            // ✅ API çağrısı: Sadece gerekli alanları istiyoruz
            String url = "https://restcountries.com/v3.1/all?fields=name,languages,region,population";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonArray countries = JsonParser.parseString(response.body()).getAsJsonArray();
            BufferedWriter writer = new BufferedWriter(new FileWriter(dosyaAdi));

            int sayac = 0;
            for (JsonElement e : countries) {
                if (sayac >= 30)
                    break;

                JsonObject country = e.getAsJsonObject();
                String ulke = country.getAsJsonObject("name").get("common").getAsString();
                String bolge = country.has("region") ? country.get("region").getAsString() : "Bilinmiyor";
                long nufus = country.has("population") ? country.get("population").getAsLong() : 0;

                // diller JSON objesi → "languages": { "tur": "Turkish", "eng": "English" }
                String diller = "Yok";
                if (country.has("languages")) {
                    JsonObject langObj = country.getAsJsonObject("languages");
                    List<String> dilListesi = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                        dilListesi.add(entry.getValue().getAsString());
                    }
                    diller = String.join(",", dilListesi);
                }

                // txt dosyasına yazılacak satır: Ülke;Diller;Bölge;Nüfus
                String satir = ulke + ";" + diller + ";" + bolge + ";" + nufus;
                writer.write(satir + "\n");
                sayac++;
            }

            writer.close();
            System.out.println("✅ Veriler başarıyla çekildi ve dosyaya yazıldı: " + dosyaAdi);

        } catch (Exception e) {
            System.out.println("Hata oluştu: " + e.getMessage());
        }
    }

    public static void listele(Scanner scanner) throws IOException {
        System.out.println("\nListeleme Seçenekleri:");
        System.out.println("a. Belirli dil konuşulan ülkeler");
        System.out.println("b. Belirli bölgedeki ülkeler");
        System.out.println("c. Tümünü listele");
        System.out.print("Seçiminiz: ");
        String altSecim = scanner.nextLine();

        BufferedReader reader = new BufferedReader(new FileReader(dosyaAdi));
        String satir;
        List<String> liste = new ArrayList<>();

        switch (altSecim) {
            case "a" -> {
                System.out.print("Dil adı girin: ");
                String dil = scanner.nextLine().toLowerCase();
                while ((satir = reader.readLine()) != null) {
                    if (satir.toLowerCase().contains(dil)) {
                        liste.add(satir);
                    }
                }
            }
            case "b" -> {
                System.out.print("Bölge adı girin: ");
                String bolge = scanner.nextLine().toLowerCase();
                while ((satir = reader.readLine()) != null) {
                    String[] parcalar = satir.split(";");
                    if (parcalar.length >= 3 && parcalar[2].toLowerCase().contains(bolge)) {
                        liste.add(satir);
                    }
                }
            }
            case "c" -> {
                while ((satir = reader.readLine()) != null) {
                    liste.add(satir);
                }
            }
            default -> System.out.println("Geçersiz alt seçim.");
        }

        reader.close();

        System.out.println("\n--- Listeleme Sonucu ---");
        for (String s : liste) {
            System.out.println(s);
        }
    }

    public static void guncelle(Scanner scanner) throws IOException {
        System.out.print("Güncellenecek ülke adını girin: ");
        String arananUlke = scanner.nextLine().toLowerCase();

        List<String> yeniSatirlar = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(dosyaAdi));
        String satir;

        while ((satir = reader.readLine()) != null) {
            String[] parcalar = satir.split(";");
            if (parcalar[0].toLowerCase().equals(arananUlke)) {
                System.out.println("Bulundu: " + satir);
                System.out.print("Yeni dil(ler): ");
                String yeniDiller = scanner.nextLine();
                satir = parcalar[0] + ";" + yeniDiller + ";" + parcalar[2] + ";" + parcalar[3];
            }
            yeniSatirlar.add(satir);
        }

        reader.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(dosyaAdi));
        for (String s : yeniSatirlar) {
            writer.write(s + "\n");
        }
        writer.close();

        System.out.println("✅ Güncelleme tamamlandı.");
    }

    public static void sil(Scanner scanner) throws IOException {
        System.out.print("Silinecek ülke adını girin: ");
        String arananUlke = scanner.nextLine().toLowerCase();

        List<String> yeniSatirlar = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(dosyaAdi));
        String satir;

        while ((satir = reader.readLine()) != null) {
            String[] parcalar = satir.split(";");
            if (parcalar[0].toLowerCase().equals(arananUlke)) {
                System.out.println("Bulundu: " + satir);
                System.out.print("Silinsin mi (e/h)? ");
                String cevap = scanner.nextLine();
                if (!cevap.equalsIgnoreCase("e")) {
                    yeniSatirlar.add(satir);
                }
            } else {
                yeniSatirlar.add(satir);
            }
        }

        reader.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(dosyaAdi));
        for (String s : yeniSatirlar) {
            writer.write(s + "\n");
        }
        writer.close();

        System.out.println("✅ Silme işlemi tamamlandı.");
    }
}
