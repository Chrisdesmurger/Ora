# Ora — Pack multi‑agents par technologies (Claude Code)

## Démarrage
1) Placez ce dossier à la racine de votre repo.
2) Ouvrez VS Code et l’onglet Claude Code.
3) Pour toute demande, parlez à **gateway-tech** (point d’entrée).
   > "Ajouter un écran Programmes paginé + détail vidéo Android & iOS, et l’API /v1/programs."

Le gateway-tech transforme votre intention en tâche structurée et la dépose au **supervisor-tech**, qui planifie et coordonne les agents.

## Contrats
- `contracts/design-tokens.json`, `contracts/user_data_contract.yaml`, `contracts/openapi.yaml`, `contracts/events.yaml`, `contracts/policy.yaml`
Seul le **superviseur** peut approuver des changements de contrats.

## Bus (messagerie inter‑agents)
- `bus/inbox/<agent>/*.json` (ordres reçus) — `bus/outbox/<agent>/*.json` (rapports/demandes)
Format :
{
  "from": "tech-android",
  "to": "tech-backend-firebase",
  "topic": "need:endpoint",
  "payload": { "method": "GET", "path": "/v1/programs", "schema": "ProgramList" },
  "ts": "ISO-8601"
}

## Prompt unique
Utilise **gateway-tech**. La demande est : "<décrire la fonction>". 
Convertis-la en tâche structurée et envoie-la au **supervisor-tech**.
Lance ensuite le pipeline techno jusqu’aux gates verts selon `contracts/policy.yaml`.

## Statuts/Rapports
- `status/pipeline.json` : avancement
- `reports/<agent>/*.md` : décisions, diffs
