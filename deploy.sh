#!/bin/bash

# Loyiha papkasiga o'tamiz
cd "$(dirname "$0")"

echo "🚀 Deployment boshlandi..."

# 1. Yangi kodni tortamiz
echo "📥 Kod yangilanmoqda (git pull)..."
git pull origin main

# 2. Konteynerlarni to'xtatamiz
echo "🛑 Eski konteynerlar to'xtatilmoqda..."
docker-compose down

# 3. Yangitdan build qilib ishga tushiramiz
echo "🏗️ Yangi konteynerlar qurilmoqda va ishga tushirilmoqda..."
docker-compose up --build -d

# 4. Keraksiz (eski) imajlarni tozalaymiz
echo "🧹 Eskirgan ma'lumotlar tozalanmoqda..."
docker image prune -f

echo "✅ Deployment muvaffaqiyatli yakunlandi!"
