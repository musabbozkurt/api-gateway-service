#!/bin/bash

# Maksimum aynı anda çalışacak proje sayısı (İşlemci çekirdek sayınıza göre ayarlayın)
MAX_JOBS=10

# Arka planda çalışan işleri takip etmek için fonksiyon
run_parallel() {
    local dir="$1"

    echo "[BAŞLADI] $dir derleniyor..."

    # Alt klasöre geç, çalıştır ve çıktıları klasör adıyla sessizce logla
    # (Hata durumunda terminale basılacak)
    cd "$dir"
    if ./mvnw clean install > build.log 2>&1; then
        echo "[BAŞARILI] $dir başarıyla tamamlandı."
        rm build.log
    else
        echo "[HATA] $dir derlemesi başarısız oldu! Detaylar için: ${dir}build.log"
    fi
    cd ..
}

# Tüm alt klasörleri dön
for dir in */; do
    if [ -d "$dir" ] && [ -f "${dir}mvnw" ]; then

        # Aktif iş sayısı limit sınırındaysa, birinin bitmesini bekle
        while [ $(jobs -r | wc -l) -ge $MAX_JOBS ]; do
            sleep 1
        done

        # Fonksiyonu arka planda (&) çalıştır
        run_parallel "$dir" &
    fi
done

# Tüm arka plan işlerinin bitmesini bekle
wait
echo "Tüm paralel derleme işlemleri tamamlandı!"
