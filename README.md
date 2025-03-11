# 🔍 HashScout

## 📋 Descripció
HashScout és una eina de gestió de fitxers amb interfície gràfica desenvolupada en JavaFX. Aquesta aplicació et permet generar hashes de fitxers, cercar text en fitxers dins de directoris i organitzar automàticament fitxers per categories.

![HashScout Logo](https://via.placeholder.com/800x400?text=HashScout+Logo)

## ✨ Característiques Principals

### 🔐 Generador de Hash
- Suport per a múltiples algorismes: MD5, SHA-1, SHA-256, SHA-512
- Visualització clara del resultat del hash
- Indicador de progrés durant el processament

### 🔎 Cercador de Text
- Cerca de contingut en múltiples fitxers
- Mostra el número de línia exacte on es troba cada coincidència
- Resultats detallats amb context de les coincidències

### 📁 Organitzador de Fitxers
- Organització automàtica de fitxers per tipus
- Categorització intel·ligent basada en extensions
- Registre detallat del procés d'organització
- Resum estadístic dels fitxers organitzats

## 🚀 Instal·lació i Ús

### Requisits Previs
- Java 17 o superior
- Maven (per compilar des del codi font)

### Instal·lació
1. Clona aquest repositori:
```bash
git clone https://github.com/mgrl39/hashscout.git
```

2. Navega al directori del projecte:
```bash
cd hashscout
```

3. Compila el projecte amb Maven:
```bash
mvn clean javafx:run
```
## 📸 Captures de Pantalla

### Generador de Hash
![HashScout - Generador de Hash](https://via.placeholder.com/800x450?text=HashScout+-+Generador+de+Hash)

### Cercador de Text
![HashScout - Cercador de Text](https://via.placeholder.com/800x450?text=HashScout+-+Cercador+de+Text)

### Organitzador de Fitxers
![HashScout - Organitzador de Fitxers](https://via.placeholder.com/800x450?text=HashScout+-+Organitzador+de+Fitxers)

## 🛠️ Tecnologies Utilitzades
- **Java**: Llenguatge de programació principal
- **JavaFX**: Framework per a la interfície gràfica
- **BootstrapFX**: Estils Bootstrap per a JavaFX
- **Maven**: Gestió de dependències i build

## 💡 Guia d'Ús

### Generador de Hash
1. Selecciona el tipus de hash al desplegable (MD5, SHA-1, SHA-256, SHA-512)
2. Fes clic a "Select File" per escollir el fitxer
3. Prem "Generate Hash" per calcular el hash del fitxer seleccionat
4. El resultat es mostrarà a l'àrea de text inferior

### Cercador de Text
1. Introdueix el terme de cerca al camp de text
2. Fes clic a "Select Folder" per escollir la carpeta on cercar
3. Prem "Search" per iniciar la cerca
4. Els resultats mostraran el nom del fitxer, número de línia i el text trobat

### Organitzador de Fitxers
1. Fes clic a "Select Folder to Organize"
2. Selecciona la carpeta que vols organizer
3. Els fitxers es categoritzaran automàticament en subcarpetes:
   - 📷 imatges (jpg, jpeg, png, gif, etc.)
   - 📄 documents (doc, pdf, txt, etc.)
   - 🎵 àudio (mp3, wav, etc.)
   - 🎬 vídeo (mp4, avi, etc.)
   - 💾 comprimits (zip, rar, etc.)
   - 💻 codi (java, py, js, etc.)
   - ⚙️ config (json, xml, yaml, etc.)
   - 🔧 executables (exe, msi, etc.)
   - 📦 altres (altres tipus de fitxers)

## 📈 Indicadors de Progrés
Totes les operacions compten amb indicadors de progrés que proporcionen retroalimentació visual durant el processament, especialment útil per a operacions amb fitxers grans o nombrosos.

## 🔄 Estat del Projecte
El projecte està en desenvolupament actiu i s'afegeixen noves característiques regularment.

## 📝 Llicència
Aquest projecte està llicenciat sota la Llicència MIT - consulta el fitxer LICENSE per a més detalls.

## 👤 Autor
- **mgrl39** - [Perfil de GitHub](https://github.com/mgrl39)

## 🌟 Contribuir
Les contribucions són benvingudes. Si vols contribuir:
1. Fes fork del projecte
2. Crea una branca per a la teva característica
3. Envia un pull request

---

*Aquest projecte ha estat desenvolupat com a part d'un exercici educatiu i pot ser utilitzat lliurement per a usos personals i educatius.*
