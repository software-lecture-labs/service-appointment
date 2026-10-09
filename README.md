# service-appointment

Otomotiv yetkili servisleri için servis yönetim sistemi. Müşteriler araçları için randevu alır, servis danışmanı iş emri açar, kullanılan parçalar stoktan düşülür ve iş emri kapanırken fatura kesilir.

> Dönem projesi. Java ile katmanlı mimaride geliştirilmektedir.

## Ekip

İş modüllere göre bölünmüştür. Her üye kendi modüllerinin hem backend (API) hem frontend (ekran) tarafını geliştirir.

| Üye | Modüller |
|---|---|
| Gökçe Su Aksoy | Parça (stok), İş Emri, Fatura, iş emri kapatma transaction'ı |
| Sevinç Sude Eker | Müşteri, Araç, Randevu, frontend iskeleti, giriş ve roller |

## Teknolojiler

**Backend:** Java 21, Spring Boot 4, Spring Data JPA, PostgreSQL, Flyway, springdoc (Swagger), Testcontainers
**Frontend:** Spring Boot + Thymeleaf + Bootstrap _(geliştirme aşamasında)_
**Application Server:** Apache Tomcat (WAR deploy)

## Teknoloji seçimleri ve gerekçeleri

Her teknolojiyi projenin gerçek ihtiyacına göre seçtik. Seçtiğimiz her aracın bir maliyeti var. Bu maliyeti karşılamayacak araçları bilerek kullanmadık.

| Teknoloji | Neden seçtik | Maliyeti / dikkat edilmesi gereken |
|---|---|---|
| **Spring Boot** | Java ekosisteminde REST API, veritabanı erişimi, doğrulama ve transaction yönetimini hazır sunuyor. Kurumsal projelerde yaygın kullanılıyor. | Arka planda çok şey otomatik çalıştığı için ne yaptığını bilmeden kullanmak hatalara yol açabilir. Uygulamanın açılış süresi ve bellek kullanımı sade bir Java uygulamasından fazla. |
| **PostgreSQL** (ilişkisel veritabanı) | Verimiz ilişkisel: müşteri → araç → iş emri → kalem → fatura. Stok düşümü ve faturalama gibi işlemlerin ya hep birlikte ya hiç gerçekleşmesi (ACID transaction) gerekiyor. Açık kaynak ve ücretsiz. | Ayrı bir sunucu olarak çalıştırılması, yedeklenmesi ve şemanın yönetilmesi gerekiyor. |
| **Spring Data JPA / Hibernate** | Tekrar eden SQL kodunu azaltıyor, entity'ler üzerinden çalışmayı sağlıyor. | Yanlış kullanılırsa gereksiz sorgular üretebilir (N+1 problemi). Bu yüzden `open-in-view` kapalı, şemayı da Hibernate değil Flyway yönetiyor. |
| **Flyway** | Veritabanı şemasının her değişikliği versiyonlu bir SQL dosyası olarak repoda duruyor. Herkesin veritabanı aynı şemaya sahip oluyor. | Çalıştırılmış bir migration dosyası sonradan değiştirilemez, düzeltme için yeni bir dosya yazmak gerekiyor. İki kişinin aynı versiyon numarasını kullanmaması gerekiyor (bizde Gökçe `V100+`, Sude `V200+`). |
| **Optimistic locking** (`@Version`) | Aynı parçanın stoğunu aynı anda güncelleyen iki işlemden birinin değişikliğinin kaybolmasını engelliyor. Veritabanında kilit tutmadığı için performansı düşürmüyor. | Çakışma olursa işlem hata alır ve tekrar denenmesi gerekir. Çakışmanın sık olduğu yerlerde uygun değil. |
| **Apache Tomcat** (WAR) | Hocanın istediği "Application Server üzerinden yayın" şartını karşılıyor. Hafif, yaygın ve ücretsiz. | WildFly gibi tam bir Jakarta EE sunucusunun sunduğu bazı özellikler (EJB, JMS) yok. Bu projede bunlara ihtiyacımız yok. |
| **Docker / Docker Compose** | Veritabanını tek komutla ve herkesin bilgisayarında aynı sürümle çalıştırıyor. "Bende çalışıyordu" sorununu azaltıyor. | Docker Desktop'ın kurulu ve açık olması gerekiyor, bilgisayarda ek bellek tüketiyor. |
| **Testcontainers** | Entegrasyon testleri sahte bir veritabanında değil, canlıdaki ile aynı PostgreSQL sürümünde çalışıyor. | Testler daha yavaş çalışıyor ve Docker gerektiriyor. |
| **springdoc (Swagger UI)** | API dokümantasyonu koddan otomatik üretiliyor. Frontend ve backend aynı API sözleşmesine bakıyor. | Canlı ortamda herkese açık bırakılırsa API'nin yapısını dışarıya gösterir. Canlıda kapatılması veya korunması gerekir. |
| **Lombok** | Getter ve constructor gibi tekrar eden kodları azaltıyor. | Derleme zamanında kod ürettiği için IDE'de eklentisinin kurulu olması gerekiyor. |
| **Thymeleaf + Bootstrap** | Frontend'i ayrı bir JavaScript projesi kurmadan Java ile yazmamızı sağlıyor. Bootstrap, CSS yazmadan düzgün görünen sayfalar sunuyor. | React gibi tek sayfalık uygulamalar kadar etkileşimli değil. Her işlemde sayfa sunucuda yeniden üretiliyor. |

### Bilerek kullanmadıklarımız

| Teknoloji | Neden kullanmadık |
|---|---|
| **Elasticsearch / ELK** | Bu projenin log hacmi küçük. Elasticsearch ayrı bir küme, ciddi bellek ve depolama ister. Bilinçsiz loglama yapılırsa bu limitler hızla aşılabilir ve maliyet artar. Bizim için Spring Boot'un varsayılan loglaması (SLF4J + Logback ile konsol ve dosya) yeterli. Arama ihtiyacını da PostgreSQL sorguları karşılıyor. |
| **Redis (önbellek)** | Veri hacmi ve trafik düşük. Önbellek eklemek, önbellekteki verinin eskimesi (tutarlılık) gibi ek bir sorun getirir. |
| **Kafka / RabbitMQ** | Modüller aynı uygulama içinde ve senkron çalışıyor. Mesaj kuyruğu, ihtiyacımız olmayan bir altyapı ve işletim yükü getirir. |
| **Mikroservis mimarisi** | İki kişilik ekip ve tek bir iş alanı için gereksiz karmaşıklık. Katmanlı bir monolit, transaction'ı tek veritabanında güvenle yönetmemizi sağlıyor. Mikroservislerde aynı işlem dağıtık transaction gerektirirdi. |

## Çalışma düzeni (Git)

```
feature/*  →  develop  →  main
```

- Her iş kendi `feature/...` dalında geliştirilir.
- İş bitince `develop` dalına Pull Request açılır ve diğer üye inceleyip onaylar.
- `develop` test edilip kararlı hale gelince `main`'e birleştirilir. `main` her zaman çalışan sürümdür.
- `main` ve `develop` dallarına doğrudan push yapılmaz.
- Commit'ler küçük ve anlamlıdır. Her commit tek bir değişikliği anlatır (Conventional Commits: `feat:`, `fix:`, `test:`, `docs:`).

## Proje yapısı

```
service-appointment/
├── backend/            Spring Boot REST API
├── frontend/           Web arayüzü
├── docker-compose.yml  Yerel geliştirme ortamı (PostgreSQL)
└── README.md
```

## Mimari

Backend katmanlı mimari ile geliştirilmektedir:

```
controller  →  service (arayüz + implementasyon)  →  repository  →  PostgreSQL
                    │
                 entity / dto / mapper
```

- **controller:** HTTP isteklerini karşılar, yalnızca service arayüzlerine bağımlıdır.
- **service:** İş kuralları ve transaction sınırları burada tanımlanır.
- **repository:** Spring Data JPA ile veri erişimi.
- **exception:** Uygulamaya özel hatalar ve tek tip hata yanıtı.

## Çalıştırma

Gereksinimler: Java 21, Docker. Maven kurmanız gerekmez, projede Maven Wrapper (`mvnw`) bulunur.

```bash
# 1. Veritabanını başlat
docker compose up -d

# 2. Backend'i çalıştır
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

- API dokümantasyonu: http://localhost:8080/swagger-ui.html
- Sağlık kontrolü: http://localhost:8080/actuator/health

Varsayılan veritabanı ayarları `.env.example` dosyasındadır. Değiştirmek için dosyayı `.env` olarak kopyalayın.

## Testler

```bash
cd backend
./mvnw verify
```

Entegrasyon testleri Testcontainers ile gerçek bir PostgreSQL üzerinde çalışır, bu yüzden Docker'ın açık olması gerekir.

## Transactional işlem

_(Geliştirme sürecinde eklenecek: iş emri kapatılırken stok düşümü, fatura oluşturma ve araç durumunun güncellenmesi tek transaction içinde yapılır. Herhangi bir adım başarısız olursa tüm işlem geri alınır.)_
