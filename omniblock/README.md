# OmniBlock — NeoForge 1.21.1 (v1.1.0)

Un Lucky Block che droppa oggetti casuali da **tutti i mod** del tuo modpack, con effetti e incantamenti!

---

## Come compilare (senza installare nulla)

### Opzione A — GitHub Codespaces (gratis, nessuna installazione)
1. Crea un account GitHub gratuito su https://github.com
2. Carica la cartella `omniblock/` su un repository GitHub
3. Clicca **"<> Code" → "Codespaces" → "Create codespace"**
4. Nel terminale che si apre (è già tutto installato):
   ```
   ./gradlew build
   ```
5. Scarica il file `build/libs/omniblock-1.0.0.jar` → copialo in `mods/`

### Opzione B — Compilare su PC (Java 21 già installato)
Se hai Java 21 JDK:
```bash
# Windows (PowerShell nella cartella omniblock/)
.\gradlew.bat build

# Linux / Mac
./gradlew build
```
Il `.jar` finale è in `build/libs/omniblock-1.0.0.jar`.

### Come ottenere Java 21 JDK (se non ce l'hai)
Scarica da: https://adoptium.net/temurin/releases/?version=21
Scegli: **Windows x64 → .msi** → installa → riapri il terminale.

---

## Come installare nel modpack

1. Copia `omniblock-1.0.0.jar` nella cartella `mods/` del tuo modpack NeoForge 1.21.1
2. Avvia il gioco
3. Dai il blocco col comando: `/give @p omniblock:omni_block`

---

## Funzionamento

| Strumento | Effetto |
|---|---|
| Piccone normale | 1 oggetto casuale da tutti i mod |
| Fortune I | Base + 25% probabilità di 1 drop extra |
| Fortune II | Base + 50% probabilità di 2 drop extra |
| Fortune III | Base + 75% probabilità di 3 drop extra |
| Silk Touch | Raccoglie il blocco senza drop |

**Rigenerazione:** 3 secondi dopo la rottura, con effetti e suoni.

**Incantamenti:** ogni drop ha il **10% di probabilità** di essere incantato con un enchantment casuale (livello 5–30). Se l'item non è incantabile normalmente, riceve Unbreaking o Mending.

### Effetti visivi
- **Rottura:** esplosione di particelle dorate + suono Totem
- **Rigenerazione:** scintille End Rod + suono Beacon

---

## Personalizzare i valori

In `OmniBlockBlock.java`:
```java
private static final int REGEN_TICKS = 60;           // 60 = 3 secondi
private static final double FORTUNE_BONUS_PER_LEVEL = 0.25;  // 25% per livello
private static final double ENCHANT_CHANCE = 0.10;   // 10% di essere incantato
```
