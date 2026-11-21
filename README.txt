Mini Me + Twitch (Fabric, клиентский, MC 26.2, Java 25)
Сборка: ./gradlew build -> build/libs/minime-1.0.0.jar

TWITCH (токен обязателен), config/minime.json:
  channel    - чей чат показывать
  twitchNick - ваш логин Twitch
  oauthToken - токен со скоупом chat:read (получить: twitchtokengenerator.com, тип Bot/Chat Token). Никому не показывайте.
После правки конфига нажмите J в игре.

Клавиши: M модель (self/tank/girl/ваши), K скин для "self", N вкл/выкл, J перечитать конфиг.
Скины: положите 64x64 PNG (обычные скины Minecraft) в config/minime/skins/ -> K листает own + ваши файлы.
Свои модели: ресурспак assets/minime/textures/mini/<имя>.png (32x32) + имя в customModels.
