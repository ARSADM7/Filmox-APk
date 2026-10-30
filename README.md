# Filmox

App Android de filmes, séries, animes, doramas e novelas — catálogo e vídeos da fonte Hyper (hyper.hyperapps.site).

**Código-fonte: 1.4** (versionCode 5; APK publicado ainda é 1.3) · Pacote: `com.filmox.app` · Android 6.0+ (minSdk 23)

## Funcionalidades

- Início com destaque rotativo (Assistir agora / Detalhes), Lançamentos, Filmes populares, Séries populares, Animes, Doramas e Novelas
- Continuar a assistir: o leitor guarda a posição e retoma de onde parou (página própria com tempo restante)
- Páginas de Filmes/Séries/Animes com filtros [Género] [Ano] [Ordenação], banner de destaque e grelha de 4 colunas
- Géneros em mosaicos com imagem (27 géneros)
- Detalhe completo: capa, ano, nota, duração, classificação etária, qualidade/áudio, sinopse expansível, elenco com fotos e personagem, trailer (YouTube), semelhantes
- Leitor de vídeo nativo com renovação automática do URL assinado
- Downloads offline em `Download/FILMOX/Filmes/` e `Download/FILMOX/Séries/Nome/Temporada N/` no Android 10+; progresso e remoção na página Downloads; opção "só por Wi-Fi" nas Definições
- Sem conta obrigatória: favoritos, histórico e downloads guardados localmente no dispositivo
- Favoritos e histórico guardados no telemóvel

## Código

Java nativo **sem AndroidX** (só framework Android), compilado com pipeline manual
(aapt2 → javac → d8 → zipalign → apksigner), Android SDK 34.

- `app/src/com/filmox/app/` — código-fonte (19 classes)
  - `Hyper.java` — cliente/parser da fonte (páginas, detalhe, episódios, watch com URL assinado)
  - `MainActivity`, `GridActivity`, `DetailActivity`, `PlayerActivity`, `SearchActivity`, `GenresActivity`
  - `DownloadsActivity`/`Dl` — downloads offline; `ContinueActivity`/`Hist` — progresso de visualização
  - `Fav` — favoritos locais; `Img` — carregador de imagens com cache; `Ui` — tema/estilos
- `app/AndroidManifest.xml` — manifesto (versão, permissões: INTERNET + ACCESS_NETWORK_STATE; não pede acesso amplo ao armazenamento)
- `test/TestMain.java` — testes do parser corridos em JVM contra páginas reais

## Segurança

A chave de assinatura da app (`filmox.keystore`) e as credenciais de build **não** estão neste
repositório — ficam guardadas em privado. Sem elas, o código compila mas as actualizações
não podem ser assinadas com a mesma chave.

## Instalar

O APK publicado actualmente é `Filmox-1.3.apk` (código-fonte 1.4 ainda aguarda novo build). Descarrega-o apenas para testar a versão publicada e instala no Android
(pode ser preciso permitir "instalar de fontes desconhecidas").
